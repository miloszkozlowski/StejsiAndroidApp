package pl.mihome.stejsiapp.activities;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.OkHttpResponseAndJSONObjectRequestListener;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import app.StejsiApplication;
import model.Tip;
import model.Token;
import model.TrainingStatus;
import model.User;
import model.UserStats;
import model.recyclerViews.MainViewElement;
import model.recyclerViews.TrainingForPresenceConfirmation;
import model.recyclerViews.TrainingForScheduleConfirmation;
import okhttp3.Response;
import pl.mihome.stejsiapp.R;

public class MainPageActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private Token currentToken;
    private User currentUser;
    private String currentTime;
    private List<Tip> currentTipsList;


    private MaterialToolbar toolbar;
    private RecyclerView mainRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;

    private BottomAppBar bottomAppBar;

    private List<MainViewElement> mainViewItems;

    private StejsiApplication app;


    @Override
    public void onBackPressed() {
        DialogInterface.OnClickListener exitListener = (dialog, which) -> finishAffinity();
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MainPageActivity.this);
        builder.setTitle(R.string.exit)
                .setCancelable(true)
                .setIcon(R.drawable.ic_exit_black_16dp)
                .setMessage(R.string.exit_confirm_info)
                .setPositiveButton(R.string.exit_confirm, exitListener)
                .setNegativeButton(R.string.exit_cancel, null);
        builder.show();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page_view);
        mainViewItems = new ArrayList<>();
        app = (StejsiApplication) getApplication();
        initMainView();

    }


    @Override
    protected void onResume() {
        super.onResume();

        app = (StejsiApplication) getApplication();
        currentUser = app.loadStoredDataUser();
        currentToken = app.loadStoredDataToken();
        currentTime = app.loadStoredDataCurrentTime();
        currentTipsList = app.loadStoredDataTips();

        loadItemsForView();

        MainViewAdapter mainViewAdapter = new MainViewAdapter(getApplicationContext(), mainViewItems, app, findViewById(R.id.mainPageList), bottomAppBar);
        mainRecyclerView.setAdapter(mainViewAdapter);
        bottomAppBar.setBadges();
        toolbar.setSubtitle(currentUser.getName() + " " + currentUser.getSurname());
    }


    private void initMainView() {
        initMenus();
        mainRecyclerView = findViewById(R.id.mainPageList);
        swipeRefreshLayout = findViewById(R.id.swipeLayout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setProgressViewOffset(true, 100, 200);
        initMainListView();
    }

    private void initMenus() {
        toolbar = findViewById(R.id.topAppBar);
        toolbar.setOnMenuItemClickListener(item -> {
            Intent intent;
            switch (item.getItemId()) {
                case R.id.profileMenuBtn:
                    intent = new Intent(MainPageActivity.this, UserActivity.class);
                    startActivity(intent);
                    return true;
                case R.id.refreshMenuBtn:
                    swipeRefreshLayout.setRefreshing(true);
                    refreshRecyclerViewData();
                    return true;
                case R.id.aboutMenuBtn:
                    intent = new Intent(MainPageActivity.this, AboutApp.class);
                    startActivity(intent);
                    return true;
                default:
                    return false;
            }
        });
        bottomAppBar = new BottomAppBar(MainPageActivity.this, app);
    }

    private void initMainListView() {
        RecyclerView.LayoutManager mainViewManager = new LinearLayoutManager(this);
        mainRecyclerView.setLayoutManager(mainViewManager);
        mainRecyclerView.hasFixedSize();
    }


    private void loadItemsForView() {
        mainViewItems.clear();
        if (!currentTipsList.isEmpty()) {
            Optional<Tip> newestTip = currentTipsList.stream().max(Comparator.comparing(Tip::getWhenCreated));
            newestTip.ifPresent(tip -> mainViewItems.add(tip));
        }
        mainViewItems.addAll(currentUser.getTrainingPackages().stream()
                .flatMap(p -> p.getTrainings().stream())
                .filter(t -> t.getStatus(currentTime).equals(TrainingStatus.PRESENCE_TO_CONFIRM))
                .map(TrainingForPresenceConfirmation::new)
                .sorted(Comparator.comparing(TrainingForPresenceConfirmation::getScheduledFor))
                .collect(Collectors.toList()));
        mainViewItems.addAll(currentUser.getTrainingPackages().stream()
                .flatMap(p -> p.getTrainings().stream())
                .filter(t -> t.getStatus(currentTime).equals(TrainingStatus.SCHEDULE_TO_CONFIRM))
                .map(TrainingForScheduleConfirmation::new)
                .sorted(Comparator.comparing(TrainingForScheduleConfirmation::getScheduledFor))
                .collect(Collectors.toList()));
    }

    @Override
    public void onRefresh() {
        refreshRecyclerViewData();
    }


    @SuppressLint("HardwareIds")
    public void refreshRecyclerViewData() {
        AndroidNetworking.get(StartActivity.WEB_SERVER_URL + "/userinput/userdata")
                .addHeaders("token", currentToken.getTokenString())
                .addHeaders("deviceId", Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID))
                .build()
                .getAsOkHttpResponseAndJSONObject(new OkHttpResponseAndJSONObjectRequestListener() {
                    @Override
                    public void onResponse(Response okHttpResponse, JSONObject response) {
                        ObjectMapper objectMapper = new ObjectMapper();
                        try {
                            currentUser = objectMapper.readValue(response.getString("user"), User.class);
                            currentUser.setStats(objectMapper.readValue(response.getString("stats"), UserStats.class));
                            currentTipsList = objectMapper.readValue(response.getString("tips"), new TypeReference<List<Tip>>() {});
                        } catch (JSONException | JsonProcessingException ex) {
                            ex.printStackTrace();
                            Snackbar snackbar = Snackbar.make(findViewById(R.id.mainPageList), R.string.error_no_server_connection, BaseTransientBottomBar.LENGTH_LONG);
                            snackbar.setAction(R.string.ok, v -> snackbar.dismiss());
                            snackbar.show();
                        }
                        currentTime = okHttpResponse.header(LoaderActivity.CURRENT_TIME_HEADER);
                        loadItemsForView();
                        app.saveAllStoredData(currentUser, currentToken, currentTime, currentTipsList);
                        mainRecyclerView.setAdapter(new MainViewAdapter(getApplicationContext(), mainViewItems, app, findViewById(R.id.mainPageList), bottomAppBar));
                        bottomAppBar.setBadges();
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onError(ANError anError) {
                        if (anError.getErrorCode() == 401) {
                            app.loggedOut(MainPageActivity.this);
                        } else {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }
                });
    }

}
