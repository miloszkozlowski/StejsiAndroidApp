package pl.mihome.stejsiapp.activities;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import app.StejsiApplication;
import model.Tip;
import model.TipReadStatus;
import model.TrainingStatus;
import model.User;
import pl.mihome.stejsiapp.R;

public class BottomAppBar  {

    private BottomNavigationView bottomBar;
    private Activity activity;

    private StejsiApplication app;

    private BottomNavigationView.OnNavigationItemSelectedListener listener;



    public BottomAppBar(@NotNull Activity activity, StejsiApplication app) {
        this.bottomBar = activity.findViewById(R.id.bottom_navigation);
        this.app = app;
        this.activity = activity;

        if(activity instanceof MainPageActivity) {
            bottomBar.setSelectedItemId(R.id.homeMenuBtn);
        }
        else if(activity instanceof TrainingsActivity) {
            bottomBar.setSelectedItemId(R.id.trainingMenuBtn);
        }
        else if(activity instanceof  TipsActivity) {
            bottomBar.setSelectedItemId(R.id.tipsMenuBtn);
        }

        bottomBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent;
                switch (item.getItemId()) {
                    case R.id.homeMenuBtn:
                        if(!(activity instanceof MainPageActivity)) {
                            intent = new Intent(activity, MainPageActivity.class);
                            intent.putExtras(app.getMainBundle());
                            activity.startActivity(intent);
                            return true;
                        }
                        return false;
                    case R.id.trainingMenuBtn:
                        if(!(activity instanceof TrainingsActivity)) {
                            intent = new Intent(activity, TrainingsActivity.class);
                            intent.putExtras(app.getMainBundle());
                            activity.startActivity(intent);
                            return true;
                        }
                        return false;
                    case R.id.tipsMenuBtn:
                        if(!(activity instanceof TipsActivity)) {
                            intent = new Intent(activity, TipsActivity.class);
                            intent.putExtras(app.getMainBundle());
                            activity.startActivity(intent);
                            return true;
                        }
                        return false;
                    default:
                        return false;
                }

            }
        });
//        setBadges();
//        setBar();

    }

    private void setBar() {
        listener = item -> {
            Intent intent;
            switch (item.getItemId()) {
                case R.id.homeMenuBtn:
                    if(!(false)) {
                        intent = new Intent(activity, MainPageActivity.class);
                        intent.putExtras(app.getMainBundle());
                        activity.startActivity(intent);
                        return true;
                    }
                    return false;
                case R.id.trainingMenuBtn:
                    if(!(activity instanceof TrainingsActivity)) {
                        intent = new Intent(activity, TrainingsActivity.class);
                        intent.putExtras(app.getMainBundle());
                        activity.startActivity(intent);
                        return true;
                    }
                    return false;
                case R.id.tipsMenuBtn:
                    if(!(activity instanceof TipsActivity)) {
                        intent = new Intent(activity, TipsActivity.class);
                        intent.putExtras(app.getMainBundle());
                        activity.startActivity(intent);
                        return true;
                    }
                    return false;
                default:
                    return false;
            }

        };
        bottomBar.setOnNavigationItemSelectedListener(listener);
    }


    public void setBadges() {
        final User currentUser = (User)app.getMainBundle().getSerializable(LoaderActivity.USER);
        final String currentTime = app.getMainBundle().getString(LoaderActivity.CURRENT_TIME_HEADER);
        final List<Tip> currentTips = (List<Tip>)app.getMainBundle().getBundle(LoaderActivity.TIPS_BUNDLE).getSerializable(LoaderActivity.TIPS_LIST);

        // ustawianie cyferki na badge Mądrości
        if(currentTips != null) {
            BadgeDrawable tipsBadge = bottomBar.getOrCreateBadge(R.id.tipsMenuBtn);
            Long newTips = currentTips.stream()
                    .filter(t -> t.getTipStatusByUser().equals(TipReadStatus.NEW))
                    .count();

            if (newTips.intValue() == 0) {
                tipsBadge.setVisible(false);
                tipsBadge.clearNumber();
            } else {
                tipsBadge.setVisible(true);
                tipsBadge.setNumber(newTips.intValue());
            }
        }

        // ustawianie cyferki na badge Treningi
        if (currentUser != null) {
            BadgeDrawable trainingsBadge = bottomBar.getOrCreateBadge(R.id.trainingMenuBtn);

            Long actionableTrainings = currentUser.getTrainingPackages().stream()
                    .flatMap(p -> p.getTrainings().stream())
                    .filter(t -> t.getStatus(currentTime).equals(TrainingStatus.PRESENCE_TO_CONFIRM) || t.getStatus(currentTime).equals(TrainingStatus.SCHEDULE_TO_CONFIRM))
                    .count();

            if (actionableTrainings.intValue() == 0) {
                trainingsBadge.setVisible(false);
                trainingsBadge.clearNumber();
            } else {
                trainingsBadge.setVisible(true);
                trainingsBadge.setNumber(actionableTrainings.intValue());
            }
        }
    }

    public BottomNavigationView getNavigationView() {
        return bottomBar;
    }
}
