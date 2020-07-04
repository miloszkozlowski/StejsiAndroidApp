package pl.mihome.stejsiapp.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.regex.Pattern;

import model.RegistrationStatus;
import model.Token;
import pl.mihome.stejsiapp.R;

public class Login extends AppCompatActivity {

    private TextView inputEmailTxt;
    private TextView clickableTxt;
    private EditText emailInput;
    private Button registerBtn;
    private Button continueButton;
    private TextView info;
    private ProgressBar progressBar;


    private Pattern emailPattern = Pattern.compile("^\\w+[\\w-\\.]*\\@\\w+((-\\w+)|(\\w*))\\.[a-z]{2,3}$");

    private boolean isConnected;
    private boolean isRegistered;
    private Token currentToken;
    private String currentEmail;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_view);

        isRegistered = getIntent().getExtras().getBoolean(StartActivity.REGISTRATION_STATUS);
        initView(isRegistered);

    }

    private void initView(boolean isRegistered) {
        emailInput = findViewById(R.id.email_input);
        registerBtn = findViewById(R.id.register_btn);
        continueButton = findViewById(R.id.continue_btn);
        info = findViewById(R.id.info_txt);
        inputEmailTxt = findViewById(R.id.email_req_txt);
        progressBar = findViewById(R.id.indeterminateBar);
        clickableTxt = findViewById(R.id.clickable_text);

        clickableTxt.setVisibility(View.GONE);
        registerBtn.setVisibility(View.VISIBLE);

        if(getIntent().getExtras().getString(StartActivity.EMAIL) != null)
            currentEmail = getIntent().getExtras().getString(StartActivity.EMAIL);

        if(!isConnected) {
            emailInput.setEnabled(false);
            registerBtn.setEnabled(false);
            registerBtn.setTextColor(getColor(R.color.colorGray));
            info.setTextColor(getColor(R.color.colorRed));
            info.setText(R.string.error_no_internet_connection);
        }
        else {
            emailInput.setEnabled(true);
            registerBtn.setEnabled(true);
            registerBtn.setTextColor(getColor(R.color.colorWhite));
            info.setTextColor(getColor(R.color.colorText));
            info.setText("");
        }

        if(!isRegistered)
            continueButton.setVisibility(View.GONE);

        clickableTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRepeatedRegistrationAlert();
            }
        });

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                continueButton.setEnabled(false);
                continueButton.setTextColor(getColor(R.color.colorGray));
                checkAuthorization();
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(emailValidated()) {
                    inputEmailTxt.setTextColor(getColor(R.color.colorText));
                    progressBar.setVisibility(View.VISIBLE);
                    registerBtn.setEnabled(false);
                    registerBtn.setTextColor(getColor(R.color.colorGray));
                    String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                    currentToken = new Token();
                    currentEmail = emailInput.getText().toString();

                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("email", currentEmail);
                        jsonObject.put("token", currentToken.getTokenString());
                        jsonObject.put("device", deviceId);
                    }
                    catch (JSONException ex) {
                        ex.printStackTrace();
                    }

                    AndroidNetworking.post(StartActivity.WEB_SERVER_URL + "/userinput/register")
                            .addJSONObjectBody(jsonObject)
                            .setTag("registration")
                            .setPriority(Priority.MEDIUM)
                            .build()
                            .getAsJSONObject(new JSONObjectRequestListener() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    String res = null;
                                    try {
                                         res = (String)response.get("status");

                                    } catch (JSONException ex) {
                                        Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                    progressBar.setVisibility(View.GONE);
                                    registerBtn.setEnabled(true);
                                    registerBtn.setTextColor(getColor(R.color.colorWhite));

                                    info.setVisibility(View.VISIBLE);
                                    switch (RegistrationStatus.valueOf(res)) {
                                        case ACTIVATION_SENT:
                                            info.setTextColor(getColor(R.color.colorText));
                                            info.setText(R.string.info_activation_email_sent);
                                            afterSuccessfulRegistration();
                                            saveNewToken(currentToken, currentEmail);
                                            break;
                                        case ALREADY_OK:
                                            info.setTextColor(getColor(R.color.colorText));
                                            afterSuccessfulRegistration();
                                            saveNewToken(currentToken, currentEmail);
                                            break;
                                        case NEW_DEVICE:
                                            info.setTextColor(getColor(R.color.colorText));
                                            showNewDeviceAlert();
                                            info.setText(R.string.info_activation_email_sent);
                                            afterSuccessfulRegistration();
                                            saveNewToken(currentToken, currentEmail);
                                            break;
                                        case EMAIL_NOT_FOUND:
                                            info.setTextColor(getColor(R.color.colorRed));
                                            info.setText(R.string.error_registration_wrong_email);
                                            break;
                                        case ALLOWED_REGISTRATION_ATTEMPS_EXCEEDED:
                                            info.setTextColor(getColor(R.color.colorRed));
                                            info.setText(R.string.error_allowed_registration_attemps_exceeded);
                                            break;
                                        default:
                                            info.setTextColor(getColor(R.color.colorRed));
                                            info.setText(R.string.error_unexpected);
                                    }
                                }

                                @Override
                                public void onError(ANError anError) {
                                    progressBar.setVisibility(View.GONE);
                                    registerBtn.setEnabled(true);
                                    registerBtn.setTextColor(getColor(R.color.colorWhite));
                                    if(anError.getErrorCode() == 0)
                                        info.setText(R.string.error_no_server_connection);
                                }
                            });




                }
                else {
                    inputEmailTxt.setTextColor(getColor(R.color.colorRed));
                }
            }
        });

    }

    private void afterSuccessfulRegistration() {

        registerBtn.setVisibility(View.GONE);
        String email = getIntent().getExtras().getString(StartActivity.EMAIL, "");
        emailInput.setText(email == "" ? currentEmail : email);
        emailInput.setEnabled(false);
        if(!email.isEmpty() && getIntent().getExtras().getBoolean(StartActivity.ERROR_400) && isRegistered)
            info.setText(R.string.info_still_watinitg_for_activation);
        else if(!email.isEmpty() && !getIntent().getExtras().getBoolean(StartActivity.ERROR_400))
            info.setText(R.string.error_no_server_connection);
        info.setVisibility(View.VISIBLE);
        clickableTxt.setVisibility(View.VISIBLE);
        continueButton.setVisibility(View.VISIBLE);


    }

    private void saveNewToken(Token token, String email) {
        SharedPreferences sharedPreferences = getApplication().getSharedPreferences(StartActivity.LOGIN_DATA, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        editor.putString(StartActivity.TOKEN, gson.toJson(token));
        editor.putString(StartActivity.EMAIL, email);
        editor.apply();
    }

    private void showRepeatedRegistrationAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
        builder.setTitle(R.string.repeat_registration);
        builder.setMessage(R.string.info_repeated_registration_message);
        builder.setPositiveButton(R.string.repeat_registration, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                initView(false);
            }
        });
        builder.setNegativeButton("Wróć", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void showNewDeviceAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
        builder.setTitle(R.string.registration_new_device_info_title);
        builder.setMessage(R.string.registration_new_device_info);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private boolean emailValidated() {
        if(emailInput.getText().toString().isEmpty())
            return false;
        if(emailPattern.matcher(emailInput.getText().toString()).matches())
            return true;
        return false;
    }

    private void checkAuthorization() {
        if(currentToken == null)
            currentToken = (Token)getIntent().getExtras().get(StartActivity.TOKEN);
        AndroidNetworking.get(StartActivity.WEB_SERVER_URL + "/userinput/authorized")
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
                                Intent intent = new Intent(Login.this, LoaderActivity.class);
                                intent.putExtra(StartActivity.TOKEN, currentToken);
                                intent.putExtra(StartActivity.EMAIL, currentEmail);
                                startActivity(intent);
                                Login.this.finish();
                            }

                        }
                        catch (JSONException ex) {
                            ex.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {

                        try {
                            Thread.sleep(1000);
                        }
                        catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                        progressBar.setVisibility(View.GONE);
                        continueButton.setEnabled(true);
                        continueButton.setTextColor(getColor(R.color.colorWhite));
                        Toast toast;
                        if(anError.getErrorCode() == 400) {
                            toast = Toast.makeText(Login.this, R.string.info_still_not_active, Toast.LENGTH_LONG);
                            info.setText(R.string.info_still_watinitg_for_activation);
                            info.setTextColor(getColor(R.color.colorText));
                            toast.show();
                        }
                        else {
                            info.setText(R.string.error_no_server_connection);
                            info.setTextColor(getColor(R.color.colorRed));
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if(!isConnected) {
            emailInput.setEnabled(false);
            registerBtn.setEnabled(false);
            registerBtn.setTextColor(getColor(R.color.colorGray));
            info.setTextColor(getColor(R.color.colorRed));
            info.setText(R.string.error_no_internet_connection);
        }
        else {
            emailInput.setEnabled(true);
            registerBtn.setEnabled(true);
            registerBtn.setTextColor(getColor(R.color.colorWhite));
            info.setTextColor(getColor(R.color.colorText));
            info.setText("");
            if(isRegistered)
                afterSuccessfulRegistration();
        }
    }

}
