package pl.mihome.stejsiapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import app.StejsiApplication;
import model.Tip;
import pl.mihome.stejsiapp.R;

public class TipsActivity extends AppCompatActivity {

    private BottomAppBar bottomAppBar;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter recyclerAdapter;
    private RecyclerView.LayoutManager recLayoutManager;
    private MaterialToolbar materialToolbar;
    private List<Tip> tips;

    private Bundle mainBundle;
    private Bundle tipsBundle;

    private StejsiApplication app;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tips_page_view);
        app = (StejsiApplication)getApplication();
        recyclerView = findViewById(R.id.tipList);
        recLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(recLayoutManager);
        recyclerView.hasFixedSize();


        initMenus();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mainBundle = app.getMainBundle();
        tips = generateTipsForView();
        recyclerAdapter = new TipViewAdapter(tips, TipsActivity.this, app);
        recyclerView.setAdapter(recyclerAdapter);
        bottomAppBar.setBadges();
    }

    private List<Tip> generateTipsForView() {
        List<Tip> list = new ArrayList<>();
        tipsBundle = mainBundle.getBundle(LoaderActivity.TIPS_BUNDLE);
        list = (List<Tip>)tipsBundle.getSerializable(LoaderActivity.TIPS_LIST);
        list = list.stream()
                .sorted(Comparator.comparing(Tip::getWhenCreated).reversed())
                .collect(Collectors.toList());

        return list;
    }

    private void initMenus() {
        materialToolbar = findViewById(R.id.topAppBar);
        materialToolbar.getMenu().removeItem(R.id.refreshMenuBtn);
        bottomAppBar = new BottomAppBar(TipsActivity.this, app);

        materialToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent;
                switch (item.getItemId()) {
                    case R.id.profileMenuBtn:
                        intent = new Intent(TipsActivity.this, UserActivity.class);
                        startActivity(intent);
                        return true;
                    case R.id.aboutMenuBtn:
                        intent = new Intent(TipsActivity.this, AboutApp.class);
                        startActivity(intent);
                        return true;
                    default:
                        return false;
                }
            }
        });
        bottomAppBar = new BottomAppBar(TipsActivity.this, app);
    }
}
