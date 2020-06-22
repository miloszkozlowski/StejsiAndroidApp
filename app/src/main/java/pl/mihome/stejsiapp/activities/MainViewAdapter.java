package pl.mihome.stejsiapp.activities;

import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.provider.CalendarContract;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.OkHttpResponseListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import model.recyclerViews.MainViewElement;
import model.Tip;
import model.Token;
import model.Training;
import model.recyclerViews.TrainingForPresenceConfirmation;
import model.recyclerViews.TrainingForScheduleConfirmation;
import okhttp3.Response;
import pl.mihome.stejsiapp.R;


public class MainViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context currentContext;
    private Locale currentLocale;
    private Token currentToken;
    private View currentView;

    private List<MainViewElement> listOfElements;
    private Training training;


    public MainViewAdapter(Context currentContext, List<MainViewElement> trainingList, Locale currentLocale, Token currentToken, View currentView) {
        this.listOfElements = trainingList;
        this.currentContext = currentContext;
        this.currentLocale = currentLocale;
        this.currentToken = currentToken;
        this.currentView = currentView;
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
                ((TrainingViewHolder) holder).bind((TrainingForPresenceConfirmation)item, currentLocale, currentToken, listOfElements, this);
                break;
            case MainViewElement.TIP_VIEW_TYPE:
                ((TipViewHolder)holder).bind((Tip)item);
                break;
            case MainViewElement.TRAINING_SCHEDULE_CONFIRM_TYPE:
                ((TrainingScheduleViewHolder)holder).bind((TrainingForScheduleConfirmation)item, currentLocale, currentToken, listOfElements, this);
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

        public void bind(TrainingForPresenceConfirmation training, Locale currentLocale, Token currentToken, List<MainViewElement> listOfElements, MainViewAdapter adapter) {

            //todo: usunąc wymuszone locale
            currentLocale = new Locale("pl-pl");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
            SimpleDateFormat sdfOut = new SimpleDateFormat("d' 'MMMM' 'YY', 'HH:mm", currentLocale);

            try {
                Date date = sdf.parse(training.getScheduledFor().toString());
                header.setText(itemView.getContext().getString(R.string.training) + " " + sdfOut.format(date));
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
            } catch (ParseException ex) {
                ex.printStackTrace();
            }

        }
    }

    static class TipViewHolder extends RecyclerView.ViewHolder {

        private ImageView image;
        private TextView header;
        private TextView bodyText;

        public TipViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.mainViewTipCardImage);
            header = itemView.findViewById(R.id.mainViewTipCardHeader);
            bodyText = itemView.findViewById(R.id.mainViewTipCardText);
        }

        public void bind(Tip tip) {

            header.setText(tip.getTitle());
            bodyText.setText(tip.getBodyText());
            Picasso.with(itemView.getContext()).load(tip.getImageUrl()).into(image);


        }
    }

    static class TrainingScheduleViewHolder extends RecyclerView.ViewHolder {

        private TextView text;

        private MaterialButton addToCallendarBtn;
        private MaterialButton confirmScheduleBtn;
        private ProgressBar progressBar;
        private ImageButton closeBtn;

        public TrainingScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.mainViewScheduleCardText);
            addToCallendarBtn = itemView.findViewById(R.id.mainViewScheduleToCallendarButton);
            confirmScheduleBtn = itemView.findViewById(R.id.mainViewScheduleConfirmButton);
            progressBar = itemView.findViewById(R.id.mainViewButtonProgressBar);
            closeBtn = itemView.findViewById(R.id.mainViewScheduleCloseButton);
        }

        public void bind(TrainingForScheduleConfirmation training, Locale currentLocale, Token currentToken, List<MainViewElement> listOfElements, MainViewAdapter adapter) {

            //todo: usunąc wymuszone locale
            currentLocale = new Locale("pl-pl");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
            SimpleDateFormat sdfOut = new SimpleDateFormat("d' 'MMMM' 'YY', 'HH:mm", currentLocale);

            try {
                Date date = sdf.parse(training.getScheduledFor().toString());
                if(training.getLocation() == null)
                    text.setText(itemView.getResources().getString(R.string.info_you_have_new_schedule_no_location, sdfOut.format(date)));
                else
                    text.setText(itemView.getResources().getString(R.string.info_you_have_new_schedule, sdfOut.format(date), training.getLocation().getName(), training.getLocation().getPostalAddress().replace("\n", "")));

                addToCallendarBtn.setOnClickListener(new View.OnClickListener() {
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

                confirmScheduleBtn.setOnClickListener(new View.OnClickListener() {
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
                                            Snackbar snackbar = Snackbar.make(itemView, R.string.info_schedule_confirmed, BaseTransientBottomBar.LENGTH_LONG);
                                            snackbar.setAction(R.string.ok, new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    snackbar.dismiss();
                                                }
                                            });
                                            snackbar.show();
                                            closeBtn.setVisibility(View.VISIBLE);
                                            addToCallendarBtn.setVisibility(View.VISIBLE);
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
            } catch (ParseException ex) {
                ex.printStackTrace();
            }

        }
    }
}
