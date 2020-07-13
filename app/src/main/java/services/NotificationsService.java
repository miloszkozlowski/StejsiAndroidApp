package services;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import model.Token;
import pl.mihome.stejsiapp.activities.StartActivity;

import static pl.mihome.stejsiapp.activities.StartActivity.LOGIN_DATA;
import static pl.mihome.stejsiapp.activities.StartActivity.TOKEN;

public class NotificationsService extends FirebaseMessagingService {


    public static final String FCM_TOKEN_REGISTERED = "FCM_TOKEN_REGISTERED";
    public static final String FCM_TOKEN_VALUE = "FCM_TOKEN_VALUE";
    private SharedPreferences sharedPreferences;

    @SuppressLint("HardwareIds")
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        sharedPreferences = getApplication().getSharedPreferences(LOGIN_DATA, MODE_PRIVATE);
        String gsonString  = sharedPreferences.getString(TOKEN, null);

        if(gsonString != null) {
            Gson gson = new Gson();
            Token currentToken = gson.fromJson(gsonString, Token.class);

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("tokenFCM", s);
            }
            catch (JSONException ex) {
                ex.printStackTrace();
            }
            AndroidNetworking.patch(StartActivity.WEB_SERVER_URL + "/userinput/newfcmtoken")
                    .setPriority(Priority.LOW)
                    .addHeaders("token", currentToken.getTokenString())
                    .addHeaders("deviceId", Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID))
                    .addJSONObjectBody(jsonObject)
                    .build()
                    .getAsString(new StringRequestListener() {
                        @Override
                        public void onResponse(String response) {
                            updateSharedPreferencesOnFCMTokenStatus(true, s);
                        }
                        @Override
                        public void onError(ANError anError) {
                            updateSharedPreferencesOnFCMTokenStatus(false, s);
                        }
                    });

        }
        else {
            updateSharedPreferencesOnFCMTokenStatus(false, s);
        }



    }

    private void updateSharedPreferencesOnFCMTokenStatus(boolean registered, @Nullable String tokenValue) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(FCM_TOKEN_REGISTERED, registered);
        if(tokenValue != null) {
            editor.putString(FCM_TOKEN_VALUE, tokenValue);
        }
        editor.apply();
    }

}
