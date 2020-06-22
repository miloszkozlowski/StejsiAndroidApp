package pl.mihome.stejsiapp.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.gson.Gson;
import com.jacksonandroidnetworking.JacksonParserFactory;

import org.json.JSONException;
import org.json.JSONObject;

import model.Token;
import pl.mihome.stejsiapp.R;

public class StartActivity extends AppCompatActivity {

    public static final String TOKEN = "TOKEN";
    public static final String EMAIL = "EMAIL";
    public static final String CONNECTION_STATUS = "CONNECTION_STATUS";
    public static final String REGISTRATION_STATUS = "REGISTRATION_STATUS";
    public static final String WEB_SERVER_URL = "http://192.168.0.96:8080";
    public static final String LOGIN_DATA = "LOGIN_DATA";
    public static final String ERROR_400 = "ERROR400";

    private boolean isConnected;

    private Token currentToken;
    private String currentEmail;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        AndroidNetworking.initialize(getApplicationContext());
        AndroidNetworking.setParserFactory(new JacksonParserFactory());

        final SharedPreferences sharedPreferences = getApplication().getSharedPreferences(LOGIN_DATA, MODE_PRIVATE);
        String gsonString  = sharedPreferences.getString(TOKEN, null);

        final Intent intentLogin = new Intent(StartActivity.this, Login.class);
        intentLogin.putExtra(CONNECTION_STATUS, isConnected);

        if(gsonString == null) {
            intentLogin.putExtra(REGISTRATION_STATUS, false);
            startActivity(intentLogin);
            StartActivity.this.finish();
        }
        else {
            intentLogin.putExtra(REGISTRATION_STATUS, true);
            Gson gson = new Gson();
            currentToken = gson.fromJson(gsonString, Token.class);
            currentEmail = sharedPreferences.getString(EMAIL, null);
            intentLogin.putExtra(EMAIL, currentEmail);
            intentLogin.putExtra(TOKEN, currentToken);

            AndroidNetworking.get(WEB_SERVER_URL + "/userinput/authorized")
                    .addHeaders("token", currentToken.getTokenString())
                    .addHeaders("deviceId", Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID))
                    .setPriority(Priority.HIGH)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String emailAuthorised = response.getString("email");
                                if(emailAuthorised.equals(currentEmail))
                                {
                                    Intent intent = new Intent(StartActivity.this, LoaderActivity.class);
                                    intent.putExtra(TOKEN, currentToken);
                                    intent.putExtra(EMAIL, currentEmail);
                                    startActivity(intent);
                                    StartActivity.this.finish();
                                }

                            }
                            catch (JSONException ex) {
                                ex.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(ANError anError) {
                            String email = sharedPreferences.getString(EMAIL, "");
                            intentLogin.putExtra(EMAIL, email);
                            if(anError.getErrorCode() == 400)
                                intentLogin.putExtra(ERROR_400, true);
                            else
                                intentLogin.putExtra(ERROR_400, false);
                            startActivity(intentLogin);
                            StartActivity.this.finish();
                        }
                    });
        }
    }

    public Token getCurrentToken() {
        return currentToken;
    }
}
