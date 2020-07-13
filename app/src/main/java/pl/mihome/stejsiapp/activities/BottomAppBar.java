package pl.mihome.stejsiapp.activities;


import android.app.Activity;
import android.content.Intent;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import app.StejsiApplication;
import model.Tip;
import model.TipReadStatus;
import model.TrainingStatus;
import model.User;
import pl.mihome.stejsiapp.R;

class BottomAppBar  {

    private BottomNavigationView bottomBar;
    private StejsiApplication app;
   // private BottomNavigationView.OnNavigationItemSelectedListener listener;



    BottomAppBar(@NotNull Activity activity, StejsiApplication app) {
        this.bottomBar = activity.findViewById(R.id.bottom_navigation);
        this.app = app;

        if (activity instanceof MainPageActivity) {
            bottomBar.setSelectedItemId(R.id.homeMenuBtn);
        } else if (activity instanceof TrainingsActivity) {
            bottomBar.setSelectedItemId(R.id.trainingMenuBtn);
        } else if (activity instanceof TipsActivity) {
            bottomBar.setSelectedItemId(R.id.tipsMenuBtn);
        }

        bottomBar.setOnNavigationItemSelectedListener(item -> {
            Intent intent;
            switch (item.getItemId()) {
                case R.id.homeMenuBtn:
                    if (!(activity instanceof MainPageActivity)) {
                        intent = new Intent(activity, MainPageActivity.class);
                        activity.startActivity(intent);
                        return true;
                    }
                    return false;
                case R.id.trainingMenuBtn:
                    if (!(activity instanceof TrainingsActivity)) {
                        intent = new Intent(activity, TrainingsActivity.class);
                        activity.startActivity(intent);
                        return true;
                    }
                    return false;
                case R.id.tipsMenuBtn:
                    if (!(activity instanceof TipsActivity)) {
                        intent = new Intent(activity, TipsActivity.class);
                        activity.startActivity(intent);
                        return true;
                    }
                    return false;
                default:
                    return false;
            }

        });
    }

    void setBadges() {
        final User currentUser = app.loadStoredDataUser();
        final String currentTime = app.loadStoredDataCurrentTime();
        final List<Tip> currentTips = app.loadStoredDataTips();

        // ustawianie cyferki na badge Mądrości
        if(currentTips != null) {
            BadgeDrawable tipsBadge = bottomBar.getOrCreateBadge(R.id.tipsMenuBtn);
            long newTips = currentTips.stream()
                    .filter(t -> t.getTipStatusByUser().equals(TipReadStatus.NEW))
                    .count();

            if ((int) newTips == 0) {
                tipsBadge.setVisible(false);
                tipsBadge.clearNumber();
            } else {
                tipsBadge.setVisible(true);
                tipsBadge.setNumber((int) newTips);
            }
        }

        // ustawianie cyferki na badge Treningi
        if (currentUser != null) {
            BadgeDrawable trainingsBadge = bottomBar.getOrCreateBadge(R.id.trainingMenuBtn);

            long actionableTrainings = currentUser.getTrainingPackages().stream()
                    .flatMap(p -> p.getTrainings().stream())
                    .filter(t -> t.getStatus(currentTime).equals(TrainingStatus.PRESENCE_TO_CONFIRM) || t.getStatus(currentTime).equals(TrainingStatus.SCHEDULE_TO_CONFIRM))
                    .count();

            if ((int) actionableTrainings == 0) {
                trainingsBadge.setVisible(false);
                trainingsBadge.clearNumber();
            } else {
                trainingsBadge.setVisible(true);
                trainingsBadge.setNumber((int) actionableTrainings);
            }
        }
    }

    BottomNavigationView getNavigationView() {
        return bottomBar;
    }
}
