package pl.mihome.stejsiapp.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.OkHttpResponseListener;
import com.androidnetworking.interfaces.StringRequestListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

import org.threeten.bp.LocalDateTime;

import app.StejsiApplication;
import model.Token;
import model.User;
import model.UserRank;
import okhttp3.Response;
import pl.mihome.stejsiapp.R;

public class UserActivity extends AppCompatActivity {

    private StejsiApplication app;
    private User currentUser;
    private String currentRank;
    private Token currentToken;
    private Bundle mainBundle;

    private ImageButton closeBtn;
    private TextView userNameAndSurname;
    private TextView userEmail;
    private TextView userPhone;
    private TextView userRankName;
    private TextView userStatTrDone;
    private TextView userStatWklAvg;
    private TextView userStatComments;
    private TextView userStatLackOfActions;
    private SwitchMaterial userSettingTipsNotifications;
    private MaterialButton logoutButton;
    private ProgressBar logoutProgress;
    private NestedScrollView parentLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_activity);

        app = (StejsiApplication) getApplication();
        closeBtn = findViewById(R.id.userCloseBtn);
        userNameAndSurname = findViewById(R.id.userNameAndSurname);
        userEmail = findViewById(R.id.userEmail);
        userPhone = findViewById(R.id.userPhoneNumber);
        logoutButton = findViewById(R.id.userLogoutBtn);
        logoutProgress = findViewById(R.id.userLogoutProgressBar);
        parentLayout = findViewById(R.id.userParentLayout);
        userRankName = findViewById(R.id.userRankName);
        userStatTrDone = findViewById(R.id.userStatTrDone);
        userStatWklAvg = findViewById(R.id.userStatWklAvg);
        userStatComments = findViewById(R.id.userStatComments);
        userStatLackOfActions = findViewById(R.id.userStatLackOfActions);
        userSettingTipsNotifications = findViewById(R.id.userSettingTipsNotifications);

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        mainBundle = app.getMainBundle();
        currentUser = (User) mainBundle.getSerializable(LoaderActivity.USER);
        currentToken = (Token) mainBundle.getSerializable(StartActivity.TOKEN);
        currentRank = mainBundle.getString(LoaderActivity.USER_RANK);

        userNameAndSurname.setText(currentUser.getImie() + " " + currentUser.getNazwisko());
        userEmail.setText(currentUser.getEmail());
        userPhone.setText(" " + currentUser.getPhoneNumber());
        userRankName.setText(currentRank);




        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    private void logout() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(R.string.logout_info);
        builder.setCancelable(true);
        builder.setIcon(R.drawable.ic_stop_black_24dp);
        builder.setMessage(R.string.logout_confirm_info);
        builder.setPositiveButton(R.string.logout_confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                logoutProgress.setVisibility(View.VISIBLE);
                logoutButton.setEnabled(false);
                AndroidNetworking.get(StartActivity.WEB_SERVER_URL + "/userinput/logout")
                        .setPriority(Priority.HIGH)
                        .addHeaders("token", currentToken.getTokenString())
                        .addHeaders("deviceId", Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID))
                        .build()
                        .getAsOkHttpResponse(new OkHttpResponseListener() {
                            @Override
                            public void onResponse(Response response) {
                                if(response.code() == 204) {
                                    SharedPreferences sharedPreferences = app.getSharedPreferences(StartActivity.LOGIN_DATA, MODE_PRIVATE);
                                    sharedPreferences.edit().clear().apply();
                                    Intent intent = new Intent(getApplicationContext(), StartActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                                else {
                                    onError(new ANError());
                                }
                            }

                            @Override
                            public void onError(ANError anError) {
                                logoutProgress.setVisibility(View.GONE);
                                logoutButton.setEnabled(true);
                                Snackbar snackbar = Snackbar.make(parentLayout, R.string.error_sth_went_wrong, BaseTransientBottomBar.LENGTH_LONG);
                                snackbar.setAction(R.string.will_try_again_CAP, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        snackbar.dismiss();
                                    }
                                });
                                snackbar.show();
                            }
                        });
            }
        });
        builder.setNegativeButton(R.string.exit_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }
}
