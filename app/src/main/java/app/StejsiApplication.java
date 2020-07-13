package app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import model.Tip;
import model.Token;
import model.Training;
import model.TrainingPackage;
import model.User;
import pl.mihome.stejsiapp.R;
import pl.mihome.stejsiapp.activities.LoaderActivity;
import pl.mihome.stejsiapp.activities.StartActivity;

@SuppressLint("ApplySharedPref")
public class StejsiApplication extends Application {

    public static final String DATA_STORAGE_USER = "DATA_STORAGE_USER";
    private static final String DATA_STORAGE_TOKEN = "DATA_STORAGE_TOKEN";
    private static final String DATA_STORAGE_TIME = "DATA_STORAGE_TIME";
    private static final String DATA_STORAGE_TIPS = "DATA_STORAGE_TIPS";

    private Bundle mainBundle;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public void saveStoredDataToken(Token token) {
        sharedPreferences = getSharedPreferences(DATA_STORAGE_TOKEN, MODE_PRIVATE);
        editor = sharedPreferences.edit();
        ObjectMapper mapper = new ObjectMapper();
        String json;
        try {
            json = mapper.writeValueAsString(token);
        }
        catch (JsonProcessingException ex) {
            ex.printStackTrace();
            json = "";
        }
        editor.putString(StartActivity.TOKEN, json);
        editor.commit();
    }

    public void saveStoredDataCurrentTime(String currentTime) {
        sharedPreferences = getSharedPreferences(DATA_STORAGE_TIME, MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putString(LoaderActivity.CURRENT_TIME_HEADER, currentTime);
        editor.commit();
    }

    public void saveStoredDataUser(User user) {
        sharedPreferences = getSharedPreferences(DATA_STORAGE_USER, MODE_PRIVATE);
        editor = sharedPreferences.edit();
        ObjectMapper mapper = new ObjectMapper();
        String json;
        try {
            json = mapper.writeValueAsString(user);
        }
        catch (JsonProcessingException ex) {
            ex.printStackTrace();
            json = "";
        }
        editor.putString(LoaderActivity.USER, json);
        editor.commit();
    }


    public void saveStoredDataTips(List<Tip> listOfTips) {
        sharedPreferences = getSharedPreferences(DATA_STORAGE_TIPS, MODE_PRIVATE);
        editor = sharedPreferences.edit();
        ObjectMapper mapper = new ObjectMapper();
        String json;
        try {
            json = mapper.writeValueAsString(listOfTips);
        }
        catch (JsonProcessingException ex) {
            ex.printStackTrace();
            json = "";
        }
        editor.putString(LoaderActivity.TIPS_BUNDLE, json);
        editor.commit();
    }


    public Token loadStoredDataToken() {
        sharedPreferences = getSharedPreferences(DATA_STORAGE_TOKEN, MODE_PRIVATE);
        ObjectMapper mapper = new ObjectMapper();
        String json = sharedPreferences.getString(StartActivity.TOKEN, "");
        try {
            return mapper.readValue(json, Token.class);
        }
        catch (JsonProcessingException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public String loadStoredDataCurrentTime() {
        sharedPreferences = getSharedPreferences(DATA_STORAGE_TIME, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(LoaderActivity.CURRENT_TIME_HEADER, "");
        return json;
    }

    public User loadStoredDataUser() {
        sharedPreferences = getSharedPreferences(DATA_STORAGE_USER, MODE_PRIVATE);
        ObjectMapper mapper = new ObjectMapper();
        String json = sharedPreferences.getString(LoaderActivity.USER, "");
        try {
            return mapper.readValue(json, User.class);
        }
        catch (JsonProcessingException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public List<Tip> loadStoredDataTips() {
        sharedPreferences = getSharedPreferences(DATA_STORAGE_TIPS, MODE_PRIVATE);
        String json = sharedPreferences.getString(LoaderActivity.TIPS_BUNDLE, "");
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(json, new TypeReference<List<Tip>>(){});

        }
        catch (JsonProcessingException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public void saveAllStoredData(User currentUser, Token currentToken, String currentTime, List<Tip> currentTips) {
        saveStoredDataTips(currentTips);
        saveStoredDataCurrentTime(currentTime);
        saveStoredDataToken(currentToken);
        saveStoredDataUser(currentUser);
    }

    public void replaceTipInStoredData(Tip tip) {
        List<Tip> list = loadStoredDataTips();
        list = list.stream()
                .map(t -> t.getId().equals(tip.getId()) ? tip : t)
                .collect(Collectors.toList());
        saveStoredDataTips(list);
        //return list;
    }

    public void replaceTrainingInStoredData(Training training) {
        User user = loadStoredDataUser();
        user.getTrainingPackages().stream()
                .flatMap(p -> p.getTrainings().stream())
                .filter(t -> t.getId().equals(training.getId()))
        .forEach(t -> {
            t.setWhenCanceled(training.getWhenCanceled());
            t.setScheduleConfirmed(training.getScheduleConfirmed());
            t.setPresenceConfirmedByUser(training.getPresenceConfirmedByUser());
        });
        saveStoredDataUser(user);
    }



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
