package pl.mihome.stejsiapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.OkHttpResponseAndJSONObjectRequestListener;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.button.MaterialButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.List;

import app.StejsiApplication;
import model.MainBundleBuilder;
import model.Tip;
import model.Token;
import model.User;
import okhttp3.Response;
import pl.mihome.stejsiapp.R;

public class LoaderActivity extends AppCompatActivity {

    public static final String USER = "USER";
    public static final String USER_RANK = "USER_RANK";
    public static final String TIPS_BUNDLE = "TIPS";
    public static final String CURRENT_TIME_HEADER = "current_time";
    public static final String TIPS_LIST = "TIPS_LIST";
    private TextView waitTxt;
    private ProgressBar progressBar;
    private MaterialButton tryAgainBtn;

    private Token currentToken;
    private User currentUser;
    private String currentTime;
    private String currentRank;
    private List<Tip> currentTipsList;

    private StejsiApplication app;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loader_view);
        app = (StejsiApplication)getApplication();

        waitTxt = findViewById(R.id.wait_txt);
        progressBar = findViewById(R.id.loader_progress);
        tryAgainBtn = findViewById(R.id.loaderTryAgainBtn);

        currentToken = (Token) getIntent().getExtras().get(StartActivity.TOKEN);
        startWaiting();
        loadUserData();

    }


    private void loadUserData() {

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
                                currentTipsList = objectMapper.readValue(response.getString("tips"), new TypeReference<List<Tip>>() {});
                                currentRank = response.getString("rank");
                            }
                            catch(JSONException | JsonProcessingException ex) {
                                ex.printStackTrace();
                            }
                            currentTime = okHttpResponse.header(CURRENT_TIME_HEADER);
                            closeActivity();

                    }

                    @Override
                    public void onError(ANError anError) {
                        anError.getErrorBody();
                        tryAgainBtn.setVisibility(View.VISIBLE);
                        tryAgainBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                tryAgainBtn.setVisibility(View.GONE);
                                startWaiting();
                                loadUserData();
                            }
                        });
                    }
                });
    }


    private void startWaiting() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                waitTxt.setText(R.string.info_waiting_too_long);
            }
        }, 5000);

        final Handler handler_aborted = new Handler();
        handler_aborted.postDelayed(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.INVISIBLE);
                waitTxt.setText(R.string.error_no_server_connection);
            }
        }, 15000);

    }




    private void closeActivity() {
        Intent intent = new Intent(LoaderActivity.this, MainPageActivity.class);
        Bundle bundleOfTips = new Bundle();
        bundleOfTips.putSerializable(TIPS_LIST, (Serializable)currentTipsList);
        app.setMainBundle(MainBundleBuilder.getCurrentBundle(currentUser, currentRank, currentToken, currentTime, bundleOfTips));
//        intent.putExtra(USER, currentUser);
//        intent.putExtra(USER_RANK, currentRank);
//        intent.putExtra(StartActivity.TOKEN, currentToken);
//        intent.putExtra(CURRENT_TIME_HEADER, currentTime);
//
//        intent.putExtra(TIPS_BUNDLE, bundleOfTips);
        startActivity(intent);
        finish();
    }


}
