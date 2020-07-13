package pl.mihome.stejsiapp.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.OkHttpResponseListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.text.DecimalFormat;

import app.StejsiApplication;
import model.Token;
import model.User;
import okhttp3.Response;
import pl.mihome.stejsiapp.R;

public class UserActivity extends AppCompatActivity {

    public static final String USER_SETTING_TIP_NOTIFICATIONS = "tipnotification";

    private StejsiApplication app;
    private User currentUser;
    private Token currentToken;
    private String currentTime;

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
    private RatingBar rankRatingBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_activity);

        app = (StejsiApplication) getApplication();
        ImageButton closeBtn = findViewById(R.id.userCloseBtn);
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
        rankRatingBar = findViewById(R.id.userRankStars);

        closeBtn.setOnClickListener(v -> finish());

    }

    @Override
    protected void onResume() {
        super.onResume();
//        mainBundle = app.getMainBundle();
        currentUser = app.loadStoredDataUser();
        currentToken = app.loadStoredDataToken();
        currentTime = app.loadStoredDataCurrentTime();

        userNameAndSurname.setText(currentUser.getName() + " " + currentUser.getSurname());
        userEmail.setText(currentUser.getEmail());
        userPhone.setText(" " + currentUser.getPhoneNumber());
        userRankName.setText(currentUser.getStats().getRank().getDescription());
        float rankMultiplier = (5f/11f);
        rankRatingBar.setRating(currentUser.getStats().getProgressPoints()*rankMultiplier);
        userStatTrDone.setText(" " + currentUser.getStats().getTotalTrainingsDone());
        DecimalFormat df = new DecimalFormat("0.00");
        userStatWklAvg.setText(df.format(currentUser.getStats().getLastFourWeeksAvgTrainingsDone()));
        userStatComments.setText(" " + currentUser.getTipComments().size());
        userStatLackOfActions.setText(" " + currentUser.getStats().getUnconfirmedTrainings());
        userSettingTipsNotifications.setChecked(currentUser.isSettingTipNotifications());


        userSettingTipsNotifications.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                userSettingTipsNotifications.setOnCheckedChangeListener(null);
                changeToggleableSetting(USER_SETTING_TIP_NOTIFICATIONS, isChecked, userSettingTipsNotifications);
                userSettingTipsNotifications.setOnCheckedChangeListener(this);
            }
        });

        logoutButton.setOnClickListener(v -> logout());
    }

    @SuppressLint("HardwareIds")
    private void changeToggleableSetting(String settingName, boolean settingStatus, SwitchMaterial switchItem) {
        switchItem.setEnabled(false);
        AndroidNetworking.patch(StartActivity.WEB_SERVER_URL + "/userinput/setting/" + settingName + "/" + settingStatus)
                .setPriority(Priority.LOW)
                .addHeaders("token", currentToken.getTokenString())
                .addHeaders("deviceId", Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID))
                .build()
                .getAsOkHttpResponse(new OkHttpResponseListener() {
                    @Override
                    public void onResponse(Response response) {

                        if(response.code() == 204) {
                            switchItem.setEnabled(true);
                            switchItem.setChecked(settingStatus);
                            changeCurrentUserSetting(settingName, settingStatus);
                        }
                        else {
                            onError(new ANError());
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        switchItem.setEnabled(true);
                        switchItem.setChecked(!settingStatus);
                        Snackbar snackbar = Snackbar.make(parentLayout, R.string.error_sth_went_wrong, BaseTransientBottomBar.LENGTH_LONG);
                        snackbar.setAction(R.string.will_try_again_CAP, v -> snackbar.dismiss());
                        snackbar.show();
                    }
                });
    }

    private void changeCurrentUserSetting(String settingName, boolean boolValue) {
        if (USER_SETTING_TIP_NOTIFICATIONS.equals(settingName)) {
            currentUser.setSettingTipNotifications(boolValue);
        }
    }


    @SuppressLint("HardwareIds")
    private void logout() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(R.string.logout_info);
        builder.setCancelable(true);
        builder.setIcon(R.drawable.ic_stop_black_24dp);
        builder.setMessage(R.string.logout_confirm_info);
        builder.setPositiveButton(R.string.logout_confirm, (dialog, which) -> {
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
                            snackbar.setAction(R.string.will_try_again_CAP, v -> snackbar.dismiss());
                            snackbar.show();
                        }
                    });
        });
        builder.setNegativeButton(R.string.exit_cancel, (dialog, which) -> dialog.dismiss());

        builder.show();
    }
}
