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
import com.androidnetworking.interfaces.OkHttpResponseAndParsedRequestListener;

import model.Token;
import model.User;
import okhttp3.Response;
import pl.mihome.stejsiapp.R;

public class LoaderActivity extends AppCompatActivity {

    public static final String USER = "USER";
    public static final String CURRENT_TIME_HEADER = "current_time";
    private TextView waitTxt;
    private ProgressBar progressBar;

    private Token currentToken;
    private User currentUser;
    private String currentTime;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loader_view);

        waitTxt = findViewById(R.id.wait_txt);
        progressBar = findViewById(R.id.loader_progress);

        currentToken = (Token) getIntent().getExtras().get(StartActivity.TOKEN);
        startWaiting();
        loadUserData();

    }


    private void loadUserData() {

        AndroidNetworking.get(StartActivity.WEB_SERVER_URL + "/userinput/userdata")
                .addHeaders("token", currentToken.getTokenString())
                .addHeaders("deviceId", Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID))
                .build()
                .getAsOkHttpResponseAndObject(User.class, new OkHttpResponseAndParsedRequestListener<User>() {
                    @Override
                    public void onResponse(Response okHttpResponse, User response) {
                        currentUser = response;
                        currentTime = okHttpResponse.header(CURRENT_TIME_HEADER);
                        closeActivity();
                    }

                    @Override
                    public void onError(ANError anError) {
                        anError.getErrorBody();
                        //TODO: dopisać xo się ma dziać, gdy brak dostępu/lub unauthorized
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
        intent.putExtra(USER, currentUser);
        intent.putExtra(StartActivity.TOKEN, currentToken);
        intent.putExtra(CURRENT_TIME_HEADER, currentTime);
        startActivity(intent);
        finish();
    }


}
