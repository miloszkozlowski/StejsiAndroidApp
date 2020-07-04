package app;

import android.app.Activity;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import pl.mihome.stejsiapp.R;
import pl.mihome.stejsiapp.activities.StartActivity;

public class StejsiApplication extends Application {

    private Bundle mainBundle;


    public void setMainBundle(Bundle bundle) {
        mainBundle = bundle;
    }

    public Bundle getMainBundle() {return mainBundle;}

    public void loggedOut(Activity activity) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity);
        builder.setTitle(R.string.logout_info);
        builder.setCancelable(false);
        builder.setIcon(R.drawable.ic_stop_black_24dp);
        builder.setMessage(R.string.logout_logged_out_info);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                SharedPreferences sharedPreferences = getSharedPreferences(StartActivity.LOGIN_DATA, MODE_PRIVATE);
                sharedPreferences.edit().clear().apply();
                Intent intent = new Intent(activity.getBaseContext(), StartActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                activity.finish();
            }
        });

        builder.show();
    }
}
