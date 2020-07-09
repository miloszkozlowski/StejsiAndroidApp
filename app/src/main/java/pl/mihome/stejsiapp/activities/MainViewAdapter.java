package pl.mihome.stejsiapp.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.provider.CalendarContract;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.OkHttpResponseListener;
import com.androidnetworking.interfaces.ParsedRequestListener;
import com.androidnetworking.interfaces.StringRequestListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import app.StejsiApplication;
import model.Tip;
import model.TipComment;
import model.TipReadStatus;
import model.Token;
import model.User;
import model.recyclerViews.MainViewElement;
import model.recyclerViews.TrainingForPresenceConfirmation;
import model.recyclerViews.TrainingForScheduleConfirmation;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import pl.mihome.stejsiapp.R;


public class MainViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context currentContext;
    private Token currentToken;
    private User currentUser;
    private View currentView;
    private BottomAppBar bottomAppBar;

    private StejsiApplication app;

    private List<MainViewElement> listOfElements;


    public MainViewAdapter(Context currentContext, List<MainViewElement> trainingList, StejsiApplication app, View currentView, BottomAppBar bottomAppBar) {
        this.listOfElements = trainingList;
        this.currentUser = (User)app.getMainBundle().getSerializable(LoaderActivity.USER);
        this.currentContext = currentContext;
        this.currentToken = (Token)app.getMainBundle().getSerializable(StartActivity.TOKEN);
        this.currentView = currentView;
        this.bottomAppBar = bottomAppBar;
        this.app = app;
    }


    @Override
    public int getItemViewType(int position) {
        MainViewElement item = listOfElements.get(position);
        return item.getModelType();
    }

    @Override
    public int getItemCount() {
       return listOfElements == null ? 0 : listOfElements.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case MainViewElement.TRAINING_PRESENCE_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_view_card_presence, parent, false);
                return new TrainingViewHolder(view);
            case MainViewElement.TIP_VIEW_TYPE :
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_view_card_tip, parent, false);
                return new TipViewHolder(view);
            case MainViewElement.TRAINING_SCHEDULE_CONFIRM_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_view_card_schedule, parent, false);
                return new TrainingScheduleViewHolder(view);
            default:
                throw new RuntimeException("Unknown data type");

        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MainViewElement item = listOfElements.get(position);

        switch (item.getModelType()) {
            case MainViewElement.TRAINING_PRESENCE_TYPE:
                ((TrainingViewHolder) holder).bind((TrainingForPresenceConfirmation)item, app, listOfElements, this, bottomAppBar);
                break;
            case MainViewElement.TIP_VIEW_TYPE:
                ((TipViewHolder)holder).bind((Tip)item, currentToken, currentUser);
                break;
            case MainViewElement.TRAINING_SCHEDULE_CONFIRM_TYPE:
                ((TrainingScheduleViewHolder)holder).bind((TrainingForScheduleConfirmation)item, app, listOfElements, this, bottomAppBar);
                break;
            default:
                break;
        }
    }


    static class TrainingViewHolder extends RecyclerView.ViewHolder {

        private TextView header;

        private MaterialButton presentBtn;
        private ProgressBar progressBar;

        public TrainingViewHolder(@NonNull View itemView) {
            super(itemView);
            header = itemView.findViewById(R.id.mainViewCardHeader);
            presentBtn = itemView.findViewById(R.id.mainViewPresentButton);
            progressBar = itemView.findViewById(R.id.mainViewButtonProgressBar);
        }

        public void bind(TrainingForPresenceConfirmation training, StejsiApplication app, List<MainViewElement> listOfElements, MainViewAdapter adapter, BottomAppBar bottomAppBar) {

            Token currentToken = (Token)app.getMainBundle().getSerializable(StartActivity.TOKEN);
            DateTimeFormatter dtfOut = DateTimeFormatter.ofPattern("d' 'MMMM' 'YYYY', 'HH:mm");

            header.setText(itemView.getContext().getString(R.string.training) + " " + dtfOut.format(training.getScheduledFor()));
            presentBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    presentBtn.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);

                    AndroidNetworking.patch(StartActivity.WEB_SERVER_URL + "/userinput/present/" + training.getId())
                            .setPriority(Priority.HIGH)
                            .addHeaders("token", currentToken.getTokenString())
                            .addHeaders("deviceId", Settings.Secure.getString(itemView.getContext().getContentResolver(), Settings.Secure.ANDROID_ID))
                            .build()
                            .getAsOkHttpResponse(new OkHttpResponseListener() {
                                @Override
                                public void onResponse(Response response) {
                                    progressBar.setVisibility(View.GONE);
                                    presentBtn.setVisibility(View.VISIBLE);

                                    if(response.code() == 204) {
                                        training.getTrainingSource().setPresenceConfirmedByUser(LocalDateTime.now());
                                        bottomAppBar.setBadges();
                                        Snackbar snackbar = Snackbar.make(itemView, R.string.info_presence_confirmed, BaseTransientBottomBar.LENGTH_LONG);
                                        snackbar.setAction(R.string.ok, new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                snackbar.dismiss();
                                            }
                                        });
                                        snackbar.show();
                                        listOfElements.remove(getAdapterPosition());
                                        adapter.notifyItemRemoved(getAdapterPosition());
                                        adapter.notifyItemRangeChanged(getAdapterPosition(), listOfElements.size());
                                    }

                                }

                                @Override
                                public void onError(ANError anError) {
                                    progressBar.setVisibility(View.GONE);
                                    presentBtn.setVisibility(View.VISIBLE);
                                    Snackbar snackbar = Snackbar.make(itemView, R.string.error_sth_went_wrong, BaseTransientBottomBar.LENGTH_LONG);
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
    }

    static class TipViewHolder extends RecyclerView.ViewHolder {

        private ImageView image;
        private TextView header;
        private TextView bodyText;
        private TextView dateTimeText;
        private TextView commentsInfo;
        private ConstraintLayout expandableView;
        private ImageButton expandBtn;
        private ImageButton addCommentBtn;
        private EditText newCommentBody;
        private ProgressBar newCommentProgressBar;
        private LinearLayout clickableLayout;


        private List<TipComment> commentsList;

        private boolean expanded = false;

        public TipViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.mainViewTipCardImage);
            header = itemView.findViewById(R.id.mainViewTipCardHeader);
            bodyText = itemView.findViewById(R.id.mainViewTipCardText);
            dateTimeText = itemView.findViewById(R.id.tipDateTime);
            commentsInfo = itemView.findViewById(R.id.tipCommentesInfo);
            expandableView = itemView.findViewById(R.id.tipCardExpandablePart);
            expandBtn = itemView.findViewById(R.id.tipCardExpandBtn);
            addCommentBtn = itemView.findViewById(R.id.tipCardNewCommentBtn);
            newCommentBody = itemView.findViewById(R.id.tipCardNewCommentEdit);
            newCommentProgressBar = itemView.findViewById(R.id.tipCardNewCommentProgressBar);
            clickableLayout = itemView.findViewById(R.id.mainViewTipCardClickableSpace);

        }

        public void bind(Tip tip, Token currentToken, User currentUser) {

            showComments(tip, currentToken, currentUser);

            addCommentBtn.setEnabled(false);

            newCommentBody.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    System.out.println(s.length() + ", " + start + ", " + count + ", " + after);
                    if(s.length() == 0 && after >= 1) {
                        addCommentBtn.setAlpha(1F);
                        addCommentBtn.setEnabled(true);
                    }
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if(s.length() == 0) {
                        addCommentBtn.setAlpha(0.2F);
                        addCommentBtn.setEnabled(false);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            addCommentBtn.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("HardwareIds")
                @Override
                public void onClick(View v) {
                    addCommentBtn.setVisibility(View.GONE);
                    newCommentProgressBar.setVisibility(View.VISIBLE);
                    newCommentBody.setEnabled(false);

                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("body", newCommentBody.getText().toString());
                        jsonObject.put("tipId", tip.getId());

                    }
                    catch (JSONException ex) {
                        ex.printStackTrace();
                    }

                    AndroidNetworking.post(StartActivity.WEB_SERVER_URL + "/userinput/newcomment")
                            .setPriority(Priority.MEDIUM)
                            .addHeaders("token", currentToken.getTokenString())
                            .addHeaders("deviceId", Settings.Secure.getString(itemView.getContext().getContentResolver(), Settings.Secure.ANDROID_ID))
                            .addJSONObjectBody(jsonObject)
                            .build()
                            .getAsOkHttpResponse(new OkHttpResponseListener() {
                                @Override
                                public void onResponse(Response response) {
                                    if(response.code() == 201) {
                                        refreshComments(tip, currentToken, currentUser);
                                        newCommentProgressBar.setVisibility(View.GONE);
                                        addCommentBtn.setVisibility(View.VISIBLE);
                                        newCommentBody.setEnabled(true);
                                        newCommentBody.setText("");
                                        addCommentBtn.setAlpha(0.2F);
                                    }
                                    else {
                                        onError(new ANError());
                                    }
                                }

                                @Override
                                public void onError(ANError anError) {
                                    newCommentProgressBar.setVisibility(View.GONE);
                                    addCommentBtn.setVisibility(View.VISIBLE);
                                    Snackbar snackbar = Snackbar.make(itemView, R.string.error_sth_went_wrong, BaseTransientBottomBar.LENGTH_LONG);
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

            header.setText(tip.getHeading());
            bodyText.setText(tip.getBody());


            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("d' 'MMMM' 'YYYY', 'HH:mm");
            dateTimeText.setText(dtf.format(tip.getWhenCreated()));

            if(tip.getComments().isEmpty()) {
                commentsInfo.setText(R.string.tip_no_comments);
            }
            else {
                Integer commentsAmount = tip.getComments().size();
                commentsInfo.setText(itemView.getResources().getString(R.string.tip_comments_info, commentsAmount.toString()));
            }

            View.OnClickListener cardExpanderListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!expanded) {
                        expandableView.setVisibility(View.VISIBLE);
                        expandBtn.setImageResource(R.drawable.ic_expand_less_black_24dp);
                        expanded = true;
                    }
                    else {
                        expandableView.setVisibility(View.GONE);
                        expandBtn.setImageResource(R.drawable.ic_expand_more_black_24dp);
                        expanded = false;
                    }
                }
            };

            clickableLayout.setOnClickListener(cardExpanderListener);

            if(tip.isLocalImagePresent()) {

                OkHttpClient client = new OkHttpClient.Builder()
                        .addInterceptor(new Interceptor() {
                            @Override
                            public Response intercept(Chain chain) throws IOException {
                                @SuppressLint("HardwareIds") Request newRequest = chain.request().newBuilder()
                                        .addHeader("token", currentToken.getTokenString())
                                        .addHeader("deviceId", Settings.Secure.getString(itemView.getContext().getContentResolver(), Settings.Secure.ANDROID_ID))
                                        .build();
                                return chain.proceed(newRequest);
                            }
                        })
                        .build();
                Picasso picasso = new Picasso.Builder(itemView.getContext())
                        .downloader(new OkHttp3Downloader(client))
                        .build();

                picasso.load(StartActivity.WEB_SERVER_URL + "/userinput/tipimage/" + tip.getId()).into(image);


            }
            else if(tip.getImageUrl() != null) {
                if(!tip.getImageUrl().isEmpty())
                    Picasso.with(itemView.getContext()).load(tip.getImageUrl()).into(image);
                else
                    image.setVisibility(View.GONE);
            }

            else
                image.setVisibility(View.GONE);

            if(tip.getTipStatusByUser().equals(TipReadStatus.NEW)) {
                AndroidNetworking.patch(StartActivity.WEB_SERVER_URL + "/userinput/reporttipseen/" + tip.getId())
                        .setPriority(Priority.LOW)
                        .addHeaders("token", currentToken.getTokenString())
                        .addHeaders("deviceId", Settings.Secure.getString(itemView.getContext().getContentResolver(), Settings.Secure.ANDROID_ID))
                        .build()
                        .getAsString(new StringRequestListener() {
                            @Override
                            public void onResponse(String response) {}
                            @Override
                            public void onError(ANError anError) {}
                        });
            }
        }

        private void refreshComments(Tip tip, Token currentToken, User currentUser) {
            AndroidNetworking.get(StartActivity.WEB_SERVER_URL + "/userinput/comments/" + tip.getId())
                    .setPriority(Priority.HIGH)
                    .addHeaders("token", currentToken.getTokenString())
                    .addHeaders("deviceId", Settings.Secure.getString(itemView.getContext().getContentResolver(), Settings.Secure.ANDROID_ID))
                    .build()
                    .getAsObjectList(TipComment.class, new ParsedRequestListener<List<TipComment>>() {
                        @Override
                        public void onResponse(List<TipComment> response) {
                            tip.setComments(new HashSet<>(response));
                            showComments(tip, currentToken, currentUser);
                            Integer commentsAmount = tip.getComments().size();
                            TextView commentsInfo = itemView.findViewById(R.id.tipCommentesInfo);
                            commentsInfo.setText(itemView.getResources().getString(R.string.tip_comments_info,  commentsAmount.toString()));
                        }

                        @Override
                        public void onError(ANError anError) {

                        }
                    });
        }

        private void removeComment(Long cid, Tip tip, Token currentToken, User currentUser) {
            AndroidNetworking.delete(StartActivity.WEB_SERVER_URL + "/userinput/removecomment/" + cid)
                    .setPriority(Priority.MEDIUM)
                    .addHeaders("token", currentToken.getTokenString())
                    .addHeaders("deviceId", Settings.Secure.getString(itemView.getContext().getContentResolver(), Settings.Secure.ANDROID_ID))
                    .build()
                    .getAsString(new StringRequestListener() {
                        @Override
                        public void onResponse(String response) {
                            refreshComments(tip, currentToken, currentUser);
                        }

                        @Override
                        public void onError(ANError anError) {
                            Snackbar snackbar = Snackbar.make(itemView, R.string.error_sth_went_wrong, BaseTransientBottomBar.LENGTH_LONG);
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

        private void showComments(Tip tip, Token currentToken, User currentUser) {
            commentsList = getCommentsList(tip.getComments());

            LayoutInflater layoutInflater = (LayoutInflater) itemView.getContext().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout insertPoint = itemView.findViewById(R.id.tipCardCommentListView);
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
                                            removeComment(tipComment.getId(), tip, currentToken, currentUser);
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

    static class TrainingScheduleViewHolder extends RecyclerView.ViewHolder {

        private TextView text;

        private MaterialButton addToCalendarBtn;
        private MaterialButton confirmScheduleBtn;
        private ProgressBar progressBar;
        private ImageButton closeBtn;


        public TrainingScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.mainViewScheduleCardText);
            addToCalendarBtn = itemView.findViewById(R.id.mainViewScheduleToCallendarButton);
            confirmScheduleBtn = itemView.findViewById(R.id.mainViewScheduleConfirmButton);
            progressBar = itemView.findViewById(R.id.mainViewButtonProgressBar);
            closeBtn = itemView.findViewById(R.id.mainViewScheduleCloseButton);

        }

        public void bind(TrainingForScheduleConfirmation training, StejsiApplication app, List<MainViewElement> listOfElements, MainViewAdapter adapter, BottomAppBar bottomAppBar) {
            Token currentToken = (Token)app.getMainBundle().getSerializable(StartActivity.TOKEN);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
            try {
                Date date = sdf.parse(training.getScheduledFor().toString());

                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("d' 'MMMM' 'YYYY', 'HH:mm");
                if(training.getLocation() == null)
                    text.setText(itemView.getResources().getString(R.string.info_you_have_new_schedule_no_location, dtf.format(training.getScheduledFor())));
                else
                    text.setText(itemView.getResources().getString(R.string.info_you_have_new_schedule, dtf.format(training.getScheduledFor()), training.getLocation().getName(), training.getLocation().getPostalAddress().replace("\n", "")));

                addToCalendarBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Calendar beginOfEvent = Calendar.getInstance();
                        beginOfEvent.setTime(date);
                        Calendar endOfEvent = Calendar.getInstance();
                        endOfEvent.setTime(date);
                        endOfEvent.add(Calendar.MINUTE, training.getTrainingPackage().getPackageType().getLengthMinutes());

                        Intent addToCallendarIntent = new Intent(Intent.ACTION_INSERT)
                                .setData(CalendarContract.Events.CONTENT_URI)
                                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginOfEvent.getTimeInMillis())
                                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endOfEvent.getTimeInMillis())
                                .putExtra(CalendarContract.Events.TITLE, "Trening personalny ze Stejsi")
                                .putExtra(CalendarContract.Events.DESCRIPTION, "Będziemy trenować! Zapraszam serdecznie na nasz wspólny trening.")
                                .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
                                .putExtra(CalendarContract.Events.HAS_ALARM, 0);
                        if(training.getLocation() != null)
                            addToCallendarIntent.putExtra(CalendarContract.Events.EVENT_LOCATION, training.getLocation().getName() + ", " + training.getLocation().getPostalAddress());
                        itemView.getContext().startActivity(addToCallendarIntent);
                    }
                });
            }
            catch(ParseException ex) {
                ex.printStackTrace();
            }

            confirmScheduleBtn.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("HardwareIds")
                @Override
                public void onClick(View v) {
                    confirmScheduleBtn.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);

                    AndroidNetworking.patch(StartActivity.WEB_SERVER_URL + "/userinput/scheduleconfirmation/" + training.getId())
                            .setPriority(Priority.HIGH)
                            .addHeaders("token", currentToken.getTokenString())
                            .addHeaders("deviceId", Settings.Secure.getString(itemView.getContext().getContentResolver(), Settings.Secure.ANDROID_ID))
                            .build()
                            .getAsOkHttpResponse(new OkHttpResponseListener() {
                                @Override
                                public void onResponse(Response response) {
                                    progressBar.setVisibility(View.GONE);

                                    if(response.code() == 204) {
                                        training.getTrainingSource().setScheduleConfirmed(LocalDateTime.now());
                                        bottomAppBar.setBadges();
                                        Snackbar snackbar = Snackbar.make(itemView, R.string.info_schedule_confirmed, BaseTransientBottomBar.LENGTH_LONG);
                                        snackbar.setAction(R.string.ok, new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                snackbar.dismiss();
                                            }
                                        });
                                        snackbar.show();
                                        closeBtn.setVisibility(View.VISIBLE);
                                        addToCalendarBtn.setVisibility(View.VISIBLE);
                                    }
                                    else
                                        confirmScheduleBtn.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onError(ANError anError) {
                                    progressBar.setVisibility(View.GONE);
                                    confirmScheduleBtn.setVisibility(View.VISIBLE);
                                    Snackbar snackbar = Snackbar.make(itemView, R.string.error_sth_went_wrong, BaseTransientBottomBar.LENGTH_LONG);
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

            closeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listOfElements.remove(getAdapterPosition());
                    adapter.notifyItemRemoved(getAdapterPosition());
                    adapter.notifyItemRangeChanged(getAdapterPosition(), listOfElements.size());
                }
            });


        }
    }
}
