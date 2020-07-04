package pl.mihome.stejsiapp.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.OkHttpResponseListener;
import com.androidnetworking.interfaces.ParsedRequestListener;
import com.androidnetworking.interfaces.StringRequestListener;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import app.StejsiApplication;
import model.MainBundleBuilder;
import model.Tip;
import model.TipComment;
import model.TipReadStatus;
import model.Token;
import model.User;
import model.viewElements.FadingImageView;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import pl.mihome.stejsiapp.R;

public class ShowTipActivity extends AppCompatActivity {

    private FadingImageView tipImage;
    private ImageButton tipCloseBtn;
    private TextView tipTitle;
    private TextView tipDateTime;
    private TextView tipBody;
    private TextView tipCommentsInfo;
    private ImageButton tipNewCommentBtn;
    private ProgressBar tipProgressBar;
    private EditText tipNewCommentBody;

    private LinearLayout tipCommentsView;

    private StejsiApplication app;

    private Token currentToken;
    private Tip currentTip;
    private User currentUser;
    private Bundle mainBundle;

    private List<TipComment> commentsList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        app = (StejsiApplication) getApplication();
        setContentView(R.layout.show_tip_activity);

        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mainBundle = app.getMainBundle();
        currentToken = (Token) mainBundle.getSerializable(StartActivity.TOKEN);
        currentTip = (Tip) getIntent().getSerializableExtra(TipViewAdapter.TIP_TO_SHOW);
        currentUser = (User) mainBundle.getSerializable(LoaderActivity.USER);

        showTipPicture();
        tipTitle.setText(currentTip.getHeading());
        tipBody.setText(currentTip.getBody());
        DateTimeFormatter df = DateTimeFormatter.ofPattern("d MMMM yyyy HH:mm");
        tipDateTime.setText(df.format(currentTip.getWhenCreated()));
        tipNewCommentBtn.setEnabled(false);

        tipNewCommentBody.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                System.out.println(s.length() + ", " + start + ", " + count + ", " + after);
                if(s.length() == 0 && after >= 1) {
                    tipNewCommentBtn.setAlpha(1F);
                    tipNewCommentBtn.setEnabled(true);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() == 0) {
                    tipNewCommentBtn.setAlpha(0.2F);
                    tipNewCommentBtn.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        if(currentTip.getTipStatusByUser().equals(TipReadStatus.NEW)) {
            AndroidNetworking.patch(StartActivity.WEB_SERVER_URL + "/userinput/reporttipseen/" + currentTip.getId())
                    .setPriority(Priority.LOW)
                    .addHeaders("token", currentToken.getTokenString())
                    .addHeaders("deviceId", Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID))
                    .build()
                    .getAsString(new StringRequestListener() {
                        @Override
                        public void onResponse(String response) {
                            currentTip.setTipStatusByUser(TipReadStatus.READ);
                            app.setMainBundle(MainBundleBuilder.updateCurrentBundle(currentTip));
                        }
                        @Override
                        public void onError(ANError anError) {}
                    });
        }

        if(currentTip.getComments().isEmpty()) {
            tipCommentsInfo.setText(R.string.tip_no_comments);
        }
        else {
            tipCommentsInfo.setText(getResources().getString(R.string.tip_comments_info, "(" + currentTip.getComments().size() + ")"));
        }

        showComments(currentTip);

        tipNewCommentBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("HardwareIds")
            @Override
            public void onClick(View v) {
                tipNewCommentBtn.setVisibility(View.GONE);
                tipProgressBar.setVisibility(View.VISIBLE);
                tipNewCommentBody.setEnabled(false);

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("body", tipNewCommentBody.getText().toString());
                    jsonObject.put("tipId", currentTip.getId());

                }
                catch (JSONException ex) {
                    ex.printStackTrace();
                }

                AndroidNetworking.post(StartActivity.WEB_SERVER_URL + "/userinput/newcomment")
                        .setPriority(Priority.MEDIUM)
                        .addHeaders("token", currentToken.getTokenString())
                        .addHeaders("deviceId", Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID))
                        .addJSONObjectBody(jsonObject)
                        .build()
                        .getAsOkHttpResponse(new OkHttpResponseListener() {
                            @Override
                            public void onResponse(Response response) {
                                if(response.code() == 201) {
                                    refreshComments();
                                    tipProgressBar.setVisibility(View.GONE);
                                    tipNewCommentBtn.setVisibility(View.VISIBLE);
                                    tipNewCommentBody.setEnabled(true);
                                    tipNewCommentBody.setText("");
                                    tipNewCommentBtn.setAlpha(0.2F);
                                }
                                else {
                                    onError(new ANError());
                                }
                            }

                            @Override
                            public void onError(ANError anError) {
                                tipProgressBar.setVisibility(View.GONE);
                                tipNewCommentBtn.setVisibility(View.VISIBLE);
                                Snackbar snackbar = Snackbar.make(getCurrentFocus(), R.string.error_sth_went_wrong, BaseTransientBottomBar.LENGTH_LONG);
                                //Snackbar snackbar = Snackbar.make(itemView, anError.getErrorCode() + anError.getErrorDetail(), BaseTransientBottomBar.LENGTH_LONG);
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

    }

    private void showTipPicture() {
        if(currentTip.isLocalImagePresent()) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request newRequest = chain.request().newBuilder()
                                    .addHeader("token", currentToken.getTokenString())
                                    .addHeader("deviceId", Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID))
                                    .build();
                            return chain.proceed(newRequest);
                        }
                    })
                    .build();
            Picasso picasso = new Picasso.Builder(getApplicationContext())
                    .downloader(new OkHttp3Downloader(client))
                    .build();

            picasso.load(StartActivity.WEB_SERVER_URL + "/userinput/tipimage/" + currentTip.getId()).into(tipImage);
        }
        else if(currentTip.getImageUrl() != null) {
                if(!currentTip.getImageUrl().isEmpty())
                    Picasso.with(getApplicationContext()).load(currentTip.getImageUrl()).into(tipImage);
                else
                    tipImage.setVisibility(View.GONE);
        }
        tipCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    private void initViews() {
        tipImage = findViewById(R.id.tipItemImage);
        tipImage.setEdgeLength(50);
        tipImage.setFadeDirection(FadingImageView.FadeSide.RIGHT_SIDE);
        tipCloseBtn = findViewById(R.id.tipItemCloseBtn);
        tipTitle = findViewById(R.id.tipItemTitle);
        tipDateTime = findViewById(R.id.tipItemDateTime);
        tipBody = findViewById(R.id.tipItemBody);
        tipCommentsInfo = findViewById(R.id.tipItemCommentsInfo);
        tipCommentsView = findViewById(R.id.tipItemCommentsView);
        tipNewCommentBtn = findViewById(R.id.tipItemNewCommentBtn);
        tipProgressBar = findViewById(R.id.tipItemNewCommentProgressBar);
        tipNewCommentBody = findViewById(R.id.tipItemNewCommentEdit);
    }

    @SuppressLint("HardwareIds")
    private void refreshComments() {
        AndroidNetworking.get(StartActivity.WEB_SERVER_URL + "/userinput/comments/" + currentTip.getId())
                .setPriority(Priority.HIGH)
                .addHeaders("token", currentToken.getTokenString())
                .addHeaders("deviceId", Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID))
                .build()
                .getAsObjectList(TipComment.class, new ParsedRequestListener<List<TipComment>>() {
                    @Override
                    public void onResponse(List<TipComment> response) {
                        currentTip.setComments(new HashSet<>(response));
                        app.setMainBundle(MainBundleBuilder.updateCurrentBundle(currentTip));
                        showComments(currentTip);
                        Integer commentsAmount = currentTip.getComments().size();
                        tipCommentsInfo.setText(getResources().getString(R.string.tip_comments_info,  commentsAmount.toString()));
                    }

                    @Override
                    public void onError(ANError anError) {

                    }
                });
    }

    private void removeComment(Long cid) {
        AndroidNetworking.delete(StartActivity.WEB_SERVER_URL + "/userinput/removecomment/" + cid)
                .setPriority(Priority.MEDIUM)
                .addHeaders("token", currentToken.getTokenString())
                .addHeaders("deviceId", Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID))
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        refreshComments();
                    }

                    @Override
                    public void onError(ANError anError) {
                        tipProgressBar.setVisibility(View.GONE);
                        tipNewCommentBtn.setVisibility(View.VISIBLE);
                        Snackbar snackbar = Snackbar.make(getCurrentFocus(), R.string.error_sth_went_wrong, BaseTransientBottomBar.LENGTH_LONG);
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

    private void showComments(Tip tip) {
        commentsList = getCommentsList(tip.getComments());

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout insertPoint = tipCommentsView;
        List views = new ArrayList();
        insertPoint.removeAllViews();
        for(int i = 0; i < commentsList.size(); i++) {
            View view = layoutInflater.inflate(R.layout.main_view_card_tip_comment, null);
            TextView body = view.findViewById(R.id.tipCommentBody);
            TextView author = view.findViewById(R.id.tipCommentAuthor);
            TextView dateTime = view.findViewById(R.id.tipCommentDateTime);
            MaterialCardView tipCommentBodyLayout = view.findViewById(R.id.tipCommentBodyLayout);
            TipComment tipComment = commentsList.get(i);

            body.setText(tipComment.getBody());
            author.setText(tipComment.getAuthorName());
            DateTimeFormatter df = DateTimeFormatter.ofPattern("d' 'MMMM' 'YYYY', 'HH:mm");
            dateTime.setText(df.format(tipComment.getWhenCreated()));


            if(currentUser.getId().equals(tipComment.getAuthorId())) {
                int finalI = i;
                tipCommentBodyLayout.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        ContextWrapper contextWrapper = new ContextThemeWrapper(view.getContext(), R.style.PopUpMenuCustom);
                        PopupMenu popupMenu = new PopupMenu(contextWrapper, tipCommentBodyLayout, Gravity.RIGHT);
                        popupMenu.getMenuInflater().inflate(R.menu.comment_pop_up_menu, popupMenu.getMenu());
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            popupMenu.setForceShowIcon(true);
                        }
                        popupMenu.show();
                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()) {
                                    case (R.id.tipCommentRemoveBtn):
                                        removeComment(commentsList.get(finalI).getId());
                                        return true;
                                    default:
                                        return false;
                                }

                            }
                        });
                        return false;
                    }
                });
            }
            views.add(view);
        }

        for(int i = 0; i<views.size(); i++)
            insertPoint.addView((View) views.get(i));
    }

    @NotNull
    private List<TipComment> getCommentsList(Set<TipComment> tipCommentSet) {
        return tipCommentSet.stream()
                .sorted(Comparator.comparing(TipComment::getWhenCreated))
                .collect(Collectors.toList());
    }


}
