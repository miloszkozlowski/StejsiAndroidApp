package pl.mihome.stejsiapp.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.OkHttpResponseAndJSONObjectRequestListener;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.appbar.MaterialToolbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import app.StejsiApplication;
import model.MainBundleBuilder;
import model.Tip;
import model.Token;
import model.TrainingPackage;
import model.User;
import model.UserStats;
import okhttp3.Response;
import pl.mihome.stejsiapp.R;

public class TrainingsActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private SwipeRefreshLayout swipeRefreshLayout;
    private BottomAppBar bottomAppBar;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter recyclerAdapter;
    private RecyclerView.LayoutManager recyclerLayoutManager;
    private MaterialToolbar toolbar;
    private List<TrainingPackage> trainingPackages;

    private User currentUser;
    private Token currentToken;
    private String currentTime;
    private Bundle bundleOfTips;
    private List<Tip> currentTipsList;
    private Locale currentLocale;

    private StejsiApplication app;
    private Bundle mainBundle;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trainings_page_view);
        app = (StejsiApplication)getApplication();

        initViews();

    }

    @Override
    protected void onResume() {
        super.onResume();

        mainBundle = app.getMainBundle();
        currentUser = (User)mainBundle.getSerializable(LoaderActivity.USER);
        currentToken = (Token)mainBundle.getSerializable(StartActivity.TOKEN);
        currentTime = mainBundle.getString(LoaderActivity.CURRENT_TIME_HEADER);

        trainingPackages = generateListForView();
        recyclerAdapter = new TrainingViewAdapter(trainingPackages, mainBundle, bottomAppBar);
        recyclerView.setAdapter(new TrainingViewAdapter(trainingPackages, app.getMainBundle(), bottomAppBar));

        bottomAppBar.setBadges();
    }



    private void initViews() {
        recyclerView = findViewById(R.id.TrainingList);
        recyclerLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(recyclerLayoutManager);
        recyclerView.setHasFixedSize(true);
        swipeRefreshLayout = findViewById(R.id.swipeLayout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setProgressViewOffset(true, 100, 200);
        initMenus();
    }


    private void initMenus() {
        toolbar = findViewById(R.id.topAppBar);
        bottomAppBar = new BottomAppBar(TrainingsActivity.this, app);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent;
                switch (item.getItemId()) {
                    case R.id.profileMenuBtn:
                        intent = new Intent(TrainingsActivity.this, UserActivity.class);
                        startActivity(intent);
                    case R.id.refreshMenuBtn:
                        swipeRefreshLayout.setRefreshing(true);
                        refreshRecyclerView();
                        return true;
                    case R.id.aboutMenuBtn:
                        intent = new Intent(TrainingsActivity.this, AboutApp.class);
                        startActivity(intent);
                        return true;

                    default:
                        return false;
                }
            }
        });
    }

    private List<TrainingPackage> generateListForView() {

        Comparator<TrainingPackage> comparator = new Comparator<TrainingPackage>() {
            @Override
            public int compare(TrainingPackage o1, TrainingPackage o2) {
                int comparingUsed = Boolean.compare(o2.isCurrentlyUsed(), o1.isCurrentlyUsed());
                if(comparingUsed != 0) {
                    return comparingUsed;
                }
                int comparingLastTraining = o2.getLastTraining().compareTo(o1.getLastTraining());
                if(comparingLastTraining != 0) {
                    return -comparingLastTraining;
                }
                return o1.getId().compareTo(o2.getId());
            }
        };
        return currentUser.getTrainingPackages().stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    @Override
    public void onRefresh() {
        refreshRecyclerView();
    }


    @Override
    public void onBackPressed() {
        bottomAppBar.getNavigationView().setSelectedItemId(R.id.homeMenuBtn);
    }

    @SuppressLint("HardwareIds")
    private void refreshRecyclerView() {
        AndroidNetworking.get(StartActivity.WEB_SERVER_URL + "/userinput/userdata")
                .addHeaders("token", currentToken.getTokenString())
                .addHeaders("deviceId", Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID))
                .build()
                .getAsOkHttpResponseAndJSONObject(new OkHttpResponseAndJSONObjectRequestListener() {
                    @Override
                    public void onResponse(Response okHttpResponse, JSONObject response) {
                        ObjectMapper objectMapper = new ObjectMapper();
                        try {
                            currentTime = okHttpResponse.header(LoaderActivity.CURRENT_TIME_HEADER);
                            currentUser = objectMapper.readValue(response.getString("user"), User.class);
                            currentUser.setStats(objectMapper.readValue(response.getString("stats"), UserStats.class));
                            trainingPackages = generateListForView();
                            app.setMainBundle(MainBundleBuilder.getCurrentBundle(currentUser, currentToken, currentTime, bundleOfTips));
                            recyclerView.setAdapter(new TrainingViewAdapter(trainingPackages, app.getMainBundle(), bottomAppBar));
                            bottomAppBar.setBadges();
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        catch(JSONException | JsonProcessingException ex) {
                            ex.printStackTrace();
                            onError(new ANError());
                        }

                    }

                    @Override
                    public void onError(ANError anError) {
                        if(anError.getErrorCode() == 401) {
                            app.loggedOut(TrainingsActivity.this);
                        }
                        else {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }
                });

    }
}
