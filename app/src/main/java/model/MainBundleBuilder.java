package model;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.loader.content.Loader;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import pl.mihome.stejsiapp.activities.BottomAppBar;
import pl.mihome.stejsiapp.activities.LoaderActivity;
import pl.mihome.stejsiapp.activities.StartActivity;

public class MainBundleBuilder {

    private static String lastRank;
    private static User lastUser;
    private static Token lastToken;
    private static String lastTime;
    private static Bundle lastBundleOfTips;

    public MainBundleBuilder() {

    }

    public static Bundle getCurrentBundle(User currentUser, String currentRank, Token currentToken, String currentTime, @Nullable Bundle bundleOfTips) {
        lastRank = currentRank;
        lastUser = currentUser;
        lastToken = currentToken;
        lastTime = currentTime;
        if(bundleOfTips != null) {
            lastBundleOfTips = bundleOfTips;
        }


        Bundle bundle = new Bundle();
        bundle.putSerializable(LoaderActivity.USER, currentUser);
        bundle.putString(LoaderActivity.USER_RANK, currentRank);
        bundle.putSerializable(StartActivity.TOKEN, currentToken);
        bundle.putString(LoaderActivity.CURRENT_TIME_HEADER, currentTime);
        if(bundleOfTips != null) {
            bundle.putBundle(LoaderActivity.TIPS_BUNDLE, bundleOfTips);
        }
        return bundle;
    }

    public static Bundle getCurrentBundle(User currentUser, Token currentToken, String currentTime, @Nullable Bundle bundleOfTips) {
        return getCurrentBundle(currentUser, lastRank, currentToken, currentTime, bundleOfTips);
    }

    public static Bundle getCurrentBundle(Bundle bundleOfTips) {
        return getCurrentBundle(lastUser, lastRank, lastToken, lastTime, bundleOfTips);
    }

    public static Bundle updateCurrentBundle(Tip tip) {
        List<Tip> list = (List<Tip>) lastBundleOfTips.getSerializable(LoaderActivity.TIPS_LIST);
        list = list.stream()
                .map(t -> t.getId().equals(tip.getId()) ? tip : t)
                .collect(Collectors.toList());
        Bundle bundle = new Bundle();
        bundle.putSerializable(LoaderActivity.TIPS_LIST, (Serializable) list);
        return getCurrentBundle(bundle);
    }


}
