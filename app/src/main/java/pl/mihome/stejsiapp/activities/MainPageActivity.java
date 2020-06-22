package pl.mihome.stejsiapp.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.OkHttpResponseAndParsedRequestListener;
import com.google.android.material.appbar.MaterialToolbar;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import model.recyclerViews.MainViewElement;
import model.Tip;
import model.Token;
import model.User;
import model.recyclerViews.TrainingForPresenceConfirmation;
import model.recyclerViews.TrainingForScheduleConfirmation;
import okhttp3.Response;
import pl.mihome.stejsiapp.R;

public class MainPageActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    public static final int REQ_CODE_LOAD_USERDATA = 11122;

    private Locale currentLocale;

    private Token currentToken;
    private User currentUser;
    private String currentTime;


    private MaterialToolbar toolbar;
    private RecyclerView mainRecyclerView;
    private RecyclerView.Adapter mainViewAdapter;
    private RecyclerView.LayoutManager mainViewManager;
    private SwipeRefreshLayout swipeRefreshLayout;

    private List<MainViewElement> mainViewItems;

    private TextView textView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        currentToken = (Token)getIntent().getExtras().get(StartActivity.TOKEN);
        if(currentUser == null) {
            currentUser = (User) getIntent().getExtras().get(LoaderActivity.USER);
            currentTime = (String) getIntent().getExtras().get(LoaderActivity.CURRENT_TIME_HEADER);
        }
        currentLocale = getResources().getConfiguration().getLocales().get(0);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page_view);

        toolbar = findViewById(R.id.topAppBar);
        mainRecyclerView = findViewById(R.id.mainPageList);
        swipeRefreshLayout = findViewById(R.id.swipeLayout);

        swipeRefreshLayout.setOnRefreshListener(this);

        toolbar.setSubtitle(currentUser.getImie() + " " + currentUser.getNazwisko());

        initMainView();

    }

    private void initMainView() {
        mainViewItems = loadItemsForView();
        initTopMenu();
        initMainListView(mainViewItems, currentLocale);


    }

    private ArrayList<MainViewElement> loadItemsForView() {
        ArrayList<MainViewElement> list = new ArrayList<>();
        list.add(new Tip("Zrób kilka pompek!", "Na trenngu zrobisz tyle, że zginiesz, więc juz teraz po dobroci kładź się i rób!", "https://scontent.fymy1-2.fna.fbcdn.net/v/t1.0-9/p720x720/92655025_550205392154822_5051416843344936960_o.jpg?_nc_cat=111&_nc_sid=2d5d41&_nc_ohc=K5BGqE4qvgcAX9wJC5M&_nc_ht=scontent.fymy1-2.fna&_nc_tp=6&oh=886d8a80884d962c22636cf92ec66e41&oe=5EF304CF"));
        list.addAll(currentUser.getTrainingPackages().stream()
                .flatMap(p -> p.getTrainings().stream())
                .map(TrainingForPresenceConfirmation::new)
                .filter(t -> t.getScheduledFor() != null)
                .filter(t -> isLDTBefore(t.getScheduledFor()) && !t.isPresenceConfirmed())
                .sorted(Comparator.comparing(TrainingForPresenceConfirmation::getScheduledFor))
                .collect(Collectors.toList()));
        list.addAll(currentUser.getTrainingPackages().stream()
                .flatMap(p -> p.getTrainings().stream())
                .map(TrainingForScheduleConfirmation::new)
                .filter(t -> t.getScheduledFor() != null)
                .filter(t -> !t.isConfirmed() && !isLDTBefore(t.getScheduledFor()))
                .sorted(Comparator.comparing(TrainingForScheduleConfirmation::getScheduledFor))
                .collect(Collectors.toList()));
        return list;
    }


    private void initTopMenu() {

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.refreshMenuBtn:
                        swipeRefreshLayout.setRefreshing(true);
                        refreshRecyclerViewData();
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    private void initMainListView(List<MainViewElement> mainViewItems, Locale currentLocale) {
        mainViewManager = new LinearLayoutManager(this);
        mainRecyclerView.setLayoutManager(mainViewManager);
        mainViewAdapter = new MainViewAdapter(this, mainViewItems, currentLocale, currentToken, findViewById(R.id.mainPageList));
        mainRecyclerView.setAdapter(mainViewAdapter);
    }

    private boolean isLDTBefore(@NotNull LocalDateTime thisBefore) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        try {
            Date thisBeforeDate = sdf.parse(thisBefore.toString());
            Date thisOneDate = sdf.parse(currentTime);
            if(thisBeforeDate.getTime() < thisOneDate.getTime())
                return true;
            return false;
        }
        catch (ParseException ex) {
            ex.printStackTrace();
            return false;
        }

    }

    private boolean isLDTBefore(@NotNull LocalDateTime thisBefore, @NotNull LocalDateTime thisOne) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        try {
            Date thisBeforeDate = sdf.parse(thisBefore.toString());
            Date thisOneDate = sdf.parse(thisOne.toString());
            if(thisBeforeDate.getTime() < thisOneDate.getTime())
                return true;
            return false;
        }
        catch (ParseException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public void onRefresh() {
        refreshRecyclerViewData();
    }


    public void refreshRecyclerViewData() {
        AndroidNetworking.get(StartActivity.WEB_SERVER_URL + "/userinput/userdata")
                .addHeaders("token", currentToken.getTokenString())
                .addHeaders("deviceId", Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID))
                .build()
                .getAsOkHttpResponseAndObject(User.class, new OkHttpResponseAndParsedRequestListener<User>() {
                    @Override
                    public void onResponse(Response okHttpResponse, User response) {
                        currentUser = response;
                        currentTime = okHttpResponse.header(LoaderActivity.CURRENT_TIME_HEADER);
                        mainViewItems = loadItemsForView();
                        mainRecyclerView.setAdapter(new MainViewAdapter(getApplicationContext(), mainViewItems, currentLocale, currentToken, findViewById(R.id.mainPageList)));
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onError(ANError anError) {
                        anError.getErrorBody();
                        //TODO: dopisać xo się ma dziać, gdy brak dostępu/lub unauthorized
                    }
                });


    }

}
