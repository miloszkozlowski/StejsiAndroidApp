package pl.mihome.stejsiapp.activities;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.Settings;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import model.Token;
import model.Training;
import model.TrainingPackage;
import model.TrainingPackageStatus;
import model.TrainingStatus;
import okhttp3.Response;
import pl.mihome.stejsiapp.R;

public class TrainingViewAdapter extends RecyclerView.Adapter<TrainingViewAdapter.TrainingViewHolder> {

    private List<TrainingPackage> trainingPackages;
    private String currentTime;
    private Token currentToken;
    private BottomAppBar bottomAppBar;

    TrainingViewAdapter(List<TrainingPackage> trainingPackages, Bundle bundle, BottomAppBar bottomAppBar) {
        this.trainingPackages = trainingPackages;
        this.currentTime = bundle.getString(LoaderActivity.CURRENT_TIME_HEADER);
        this.currentToken = (Token)bundle.getSerializable(StartActivity.TOKEN);
        this.bottomAppBar = bottomAppBar;
    }


    @NonNull
    @Override
    public TrainingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.training_view_card, parent, false);
        TrainingViewHolder viewHolder = new TrainingViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull TrainingViewHolder holder, int position) {
        final TrainingPackage trainingPackage = trainingPackages.get(position);
        holder.packageTitle.setText(trainingPackage.getPackageType().getTitle());
        holder.trainingsAmountTxt.setText(holder.itemView.getResources().getString(R.string.trainings_amount, " " + trainingPackage.getPackageType().getAmountOfTrainings()));
        holder.trainingsAmountToPlanTxt.setText(holder.itemView.getResources().getString(R.string.left_to_plan, " " + trainingPackage.getAmountToPlan()));
        holder.trainingsDoneAmountTxt.setText(holder.itemView.getResources().getString(R.string.trainings_done_amount, " " + trainingPackage.getAmountTrainingsDone()));
//        holder.trainingsDoneAmountTxt.setText(trainingPackage.getId().toString());
        holder.packageDesc.setText(trainingPackage.getPackageType().getDescription());
        holder.expandedLayout.setVisibility(View.GONE);
        holder.expandLessBtn.setVisibility(View.GONE);
        holder.expandMoreBtn.setVisibility(View.VISIBLE);

//        holder.test.setText(trainingPackage.getStatus(currentTime).toString());

        /*
            Tutaj okreslamy wyświetlanie ważności pakietu
         */
        holder.validDueTxt.setVisibility(View.VISIBLE);
        if(trainingPackage.getStatus(currentTime) == TrainingPackageStatus.USED) {
            holder.validDueTxt.setVisibility(View.GONE);
        }
        else if(trainingPackage.getValidityDays(currentTime) == null) {
            holder.validDueTxt.setText(holder.itemView.getResources().getString(R.string.valid_indefinitely));
        }
        else if(trainingPackage.isOpened() && trainingPackage.isValid(currentTime)) {
            holder.validDueTxt.setText(holder.itemView.getResources().getString(R.string.valid_days, " " + trainingPackage.getValidityDays(currentTime)));
        }
        else if(trainingPackage.getStatus(currentTime) == TrainingPackageStatus.OUT_DATED){
            holder.validDueTxt.setText(R.string.out_dated);
        }
        else if(trainingPackage.getStatus(currentTime) == TrainingPackageStatus.CLOSE_TO_OUT_DATE && trainingPackage.getValidityDays(currentTime) > 1){
            holder.validDueTxt.setText(holder.itemView.getResources().getString(R.string.timie_is_running_out, " " + trainingPackage.getValidityDays(currentTime)));
        }
        else if(trainingPackage.getStatus(currentTime) == TrainingPackageStatus.CLOSE_TO_OUT_DATE && trainingPackage.getValidityDays(currentTime) == 1){
            holder.validDueTxt.setText(R.string.timie_is_running_out_one_day);
        }
        else {
            holder.validDueTxt.setVisibility(View.GONE);
        }

        /*
            Tutaj określamy kolor kropki w zalezności od statusu
         */
        TrainingPackageStatus status = trainingPackage.getStatus(currentTime);
        holder.statusDot.setVisibility(View.VISIBLE);
        switch (status) {
            case OK:
                holder.statusDot.setImageResource(R.drawable.ic_dot_green_16dp);
                break;
            case CLOSE_TO_OUT_DATE:
                holder.statusDot.setImageResource(R.drawable.ic_dot_orange_16dp);
                break;
            case USED:
                holder.statusDot.setImageResource(R.drawable.ic_dot_grey_16dp);
                break;
            case HAS_TRAININGS_TO_CONFIRM_PRESENCE:
            case HAS_TRAININGS_TO_CONFIRM_SCHEDULE:
                holder.statusDot.setImageResource(R.drawable.ic_dot_yellow_16dp);
                break;
            case OUT_DATED:
                holder.statusDot.setImageResource(R.drawable.ic_dot_dark_red_16dp);
                break;
            default:
                holder.statusDot.setVisibility(View.GONE);

        }

        /*
            Poniżej inicjacja ikon po prawej stronie karty - infomracje o pakiecie
         */
        if(trainingPackage.isPaid()) {
            holder.trainingPaidIcon.setVisibility(View.VISIBLE);
            holder.trainingNotPaidIcon.setVisibility(View.GONE);
            holder.amountDueTxt.setVisibility(View.GONE);
        }
        else {
            holder.trainingPaidIcon.setVisibility(View.GONE);
            holder.trainingNotPaidIcon.setVisibility(View.VISIBLE);
            holder.amountDueTxt.setText("Do zapłaty: PLN " + trainingPackage.getPackageType().getPricePLN());
            holder.amountDueTxt.setVisibility(View.GONE);
        }
        holder.trainingDoneIcon.setVisibility(View.GONE);
        if(!trainingPackage.isOpened()) {
            holder.trainingDoneIcon.setVisibility(View.VISIBLE);
        }


        holder.expanded = false;

        boolean showTrainingsLeft = trainingPackage.getAmountToPlan() > 0 && trainingPackage.isValid(currentTime);

        if(trainingPackage.isCurrentlyUsed()) {
            expandCard(holder, true, showTrainingsLeft, !trainingPackage.isPaid());
        }

        showTrainings(trainingPackage, holder, currentTime);


       View.OnClickListener expandAction = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    expandCard(holder, !holder.expanded, showTrainingsLeft, !trainingPackage.isPaid());
                }
        };

        holder.expandMoreBtn.setOnClickListener(expandAction);
        holder.expandLessBtn.setOnClickListener(expandAction);
        holder.clickableLayout.setOnClickListener(expandAction);
    }

    private void showTrainings(TrainingPackage trainingPackage, TrainingViewHolder holder, String currentTime) {


        List<Training> trainingsList = getTrainingList(trainingPackage.getTrainings()).stream()
                .filter(t -> t.getScheduledFor() != null && !t.isDone(currentTime))
                .sorted(Comparator.comparing(Training::getScheduledFor).reversed())
                .collect(Collectors.toList());

        trainingsList.addAll(getTrainingList(trainingPackage.getTrainings()).stream()
                .filter(t -> t.getScheduledFor() != null && t.isDone(currentTime))
                .sorted(Comparator.comparing(Training::getScheduledFor).reversed())
                .collect(Collectors.toList()));

        LayoutInflater layoutInflater = (LayoutInflater) holder.itemView.getContext().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout insertPoint = holder.itemView.findViewById(R.id.trainingCardItemsView);
        List views = new ArrayList();
        insertPoint.removeAllViews();
        for(int i = 0; i < trainingsList.size(); i++) {
            Training training = trainingsList.get(i);
            View view = layoutInflater.inflate(R.layout.training_view_training_item, null);
            MaterialCardView card = view.findViewById(R.id.trainingCard);
            TextView trainingDateTime = view.findViewById(R.id.trainingItemDate);
            TextView trainingStatus = view.findViewById(R.id.trainingItemStatus);
            TextView trainingInfo = view.findViewById(R.id.trainingItemLocationAndLength);
            MaterialButton cnfPresenceBtn = view.findViewById(R.id.trainingItemCnfPresenceBtn);
            MaterialButton cnfScheduleBtn = view.findViewById(R.id.trainingItemCnfScheduleBtn);
            ImageButton popUpMenuBtn = view.findViewById(R.id.trainingCardPopUpMenuBtn);
            ProgressBar progressBar = view.findViewById(R.id.trainingCardItemProgressBar);

            DateTimeFormatter df = DateTimeFormatter.ofPattern("d' 'MMMM' 'YYYY', 'HH:mm");
            trainingDateTime.setText(training.getScheduledFor().format(df));

            popUpMenuBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ContextWrapper contextWrapper = new ContextThemeWrapper(view.getContext(), R.style.PopUpMenuCustom);
                    PopupMenu popupMenu = new PopupMenu(contextWrapper, view, Gravity.RIGHT);
                    popupMenu.getMenuInflater().inflate(R.menu.training_pop_up_menu, popupMenu.getMenu());
                    popupMenu.show();
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case (R.id.trainingCardPopUpAddToCalendar):
                                    addToCalendar(training, holder.itemView);
                                    return true;
                                case(R.id.trainingCardPopUpCancel):
                                    cancelTraining((View)v.getParent(), holder.itemView, training);
                                    return true;
                                default:
                                    return false;
                            }

                        }
                    });
                }
            });

            if(training.getLocation() == null) {
                trainingInfo.setText(holder.itemView.getResources().getString(R.string.training_info_no_location,
                        training.getTrainingPackage().getPackageType().getLengthMinutes().toString()
                ));
            }
            else {
                trainingInfo.setText(holder.itemView.getResources().getString(R.string.training_info_details,
                        training.getLocation().getName(),
                        training.getLocation().getPostalAddress(),
                        training.getTrainingPackage().getPackageType().getLengthMinutes().toString()
                ));
            }

            //test
            //trainingInfo.setText(trainingsList.get(i).getStatus(currentTime).toString());

            TrainingStatus status = training.getStatus(currentTime);
            trainingStatus.setText(status.getDescription());
            if(status.getDescription() == "") {
                trainingStatus.setVisibility(View.GONE);
            }
            else {
                trainingStatus.setVisibility(View.VISIBLE);
            }

            if(status == TrainingStatus.PRESENCE_TO_CONFIRM) {
                cnfPresenceBtn.setVisibility(View.VISIBLE);
                cnfPresenceBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        confirmPresence((View)v.getParent().getParent(), training);
                    }
                });
            }
            else if(status == TrainingStatus.READY_TO_HAPPEN){
                popUpMenuBtn.setVisibility(View.VISIBLE);
            }
            else if(status == TrainingStatus.SCHEDULE_TO_CONFIRM) {
                cnfScheduleBtn.setVisibility(View.VISIBLE);
                cnfScheduleBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        confirmSchedule((View)v.getParent().getParent(), training);
                    }
                });
            }

            views.add(view);
        }

        for(int i = 0; i<views.size(); i++)
            insertPoint.addView((View) views.get(i));
    }

    private void confirmPresence(View itemView, Training training) {
        ProgressBar pb = itemView.findViewById(R.id.trainingCardItemProgressBar);
        MaterialButton button = itemView.findViewById(R.id.trainingItemCnfPresenceBtn);
        TextView tv = itemView.findViewById(R.id.trainingItemStatus);

        pb.setVisibility(View.VISIBLE);
        button.setVisibility(View.GONE);

        AndroidNetworking.patch(StartActivity.WEB_SERVER_URL + "/userinput/present/" + training.getId())
                .setPriority(Priority.HIGH)
                .addHeaders("token", currentToken.getTokenString())
                .addHeaders("deviceId", Settings.Secure.getString(itemView.getContext().getContentResolver(), Settings.Secure.ANDROID_ID))
                .build()
                .getAsOkHttpResponse(new OkHttpResponseListener() {
                    @Override
                    public void onResponse(Response response) {
                        pb.setVisibility(View.GONE);
                        if(response.code() == 204) {
                            training.setPresenceConfirmedByUser(LocalDateTime.now());
                            if(training.getScheduleConfirmed() == null) {
                                training.setScheduleConfirmed(LocalDateTime.now());
                            }
                            Snackbar snackbar = Snackbar.make(itemView, R.string.info_presence_confirmed, BaseTransientBottomBar.LENGTH_LONG);
                            snackbar.setAction(R.string.ok, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    snackbar.dismiss();
                                }
                            });
                            snackbar.show();
                            tv.setVisibility(View.GONE);
                            bottomAppBar.setBadges();
                        }
                        else
                            button.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError(ANError anError) {
                        pb.setVisibility(View.GONE);
                        button.setVisibility(View.VISIBLE);
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

    private void confirmSchedule(View itemView, Training training) {
        ProgressBar pb = itemView.findViewById(R.id.trainingCardItemProgressBar);
        MaterialButton button = itemView.findViewById(R.id.trainingItemCnfScheduleBtn);
        ImageButton ib = itemView.findViewById(R.id.trainingCardPopUpMenuBtn);
        TextView tv = itemView.findViewById(R.id.trainingItemStatus);

        pb.setVisibility(View.VISIBLE);
        button.setVisibility(View.GONE);

        AndroidNetworking.patch(StartActivity.WEB_SERVER_URL + "/userinput/scheduleconfirmation/" + training.getId())
                .setPriority(Priority.HIGH)
                .addHeaders("token", currentToken.getTokenString())
                .addHeaders("deviceId", Settings.Secure.getString(itemView.getContext().getContentResolver(), Settings.Secure.ANDROID_ID))
                .build()
                .getAsOkHttpResponse(new OkHttpResponseListener() {
                    @Override
                    public void onResponse(Response response) {
                        pb.setVisibility(View.GONE);
                        if(response.code() == 204) {
                            training.setScheduleConfirmed(LocalDateTime.now());
                            Snackbar snackbar = Snackbar.make(itemView, R.string.info_schedule_confirmed, BaseTransientBottomBar.LENGTH_LONG);
                            snackbar.setAction(R.string.ok, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    snackbar.dismiss();
                                }
                            });
                            snackbar.show();
                            ib.setVisibility(View.VISIBLE);
                            tv.setVisibility(View.GONE);
                            bottomAppBar.setBadges();
                        }
                        else
                            button.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError(ANError anError) {
                        pb.setVisibility(View.GONE);
                        button.setVisibility(View.VISIBLE);
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


    private void cancelTraining(View itemView, View globalView, Training training) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(globalView.getContext());
        builder.setTitle("Odwoływanie treningu");
        builder.setCancelable(true);
        builder.setIcon(R.drawable.ic_cancel_gray_24dp);
        builder.setMessage(R.string.cancel_confirmation);
        builder.setPositiveButton(R.string.cancel_confirmed, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ProgressBar pb = itemView.findViewById(R.id.trainingCardItemProgressBar);
                ImageButton ib = itemView.findViewById(R.id.trainingCardPopUpMenuBtn);
                TextView tv = globalView.findViewById(R.id.trainingItemStatus);
                pb.setVisibility(View.VISIBLE);
                ib.setVisibility(View.GONE);
                AndroidNetworking.patch(StartActivity.WEB_SERVER_URL + "/userinput/cancel/" + training.getId())
                        .setPriority(Priority.MEDIUM)
                        .addHeaders("token", currentToken.getTokenString())
                        .addHeaders("deviceId", Settings.Secure.getString(itemView.getContext().getContentResolver(), Settings.Secure.ANDROID_ID))
                        .build()
                        .getAsOkHttpResponse(new OkHttpResponseListener() {
                            @Override
                            public void onResponse(Response response) {
                                pb.setVisibility(View.GONE);
                                if(response.code() == 204) {
                                    training.setWhenCanceled(LocalDateTime.now());
                                    tv.setText(training.getStatus(currentTime).getDescription());
                                    tv.setVisibility(View.VISIBLE);
                                    Snackbar snackbar = Snackbar.make(itemView, R.string.cancel_done, BaseTransientBottomBar.LENGTH_LONG);
                                    snackbar.setAction(R.string.ok, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            snackbar.dismiss();
                                        }
                                    });
                                    snackbar.show();
                                }
                                else {
                                    pb.setVisibility(View.GONE);
                                    ib.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void onError(ANError anError) {
                                pb.setVisibility(View.GONE);
                                ib.setVisibility(View.VISIBLE);
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

        builder.show();

    }

    @NotNull
    private List<Training> getTrainingList(Set<Training> trainingSet) {
        return trainingSet.stream()
                .filter(t -> t.getScheduledFor() != null)
                .sorted(Comparator.comparing(Training::getScheduledFor))
                .collect(Collectors.toList());
    }

    private void addToCalendar(Training training, View itemView) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        try {
            Date date = sdf.parse(training.getScheduledFor().toString());

            Calendar beginOfEvent = Calendar.getInstance();
            beginOfEvent.setTime(date);
            Calendar endOfEvent = Calendar.getInstance();
            endOfEvent.setTime(date);
            endOfEvent.add(Calendar.MINUTE, training.getTrainingPackage().getPackageType().getLengthMinutes());

            Intent addToCalendarIntent = new Intent(Intent.ACTION_INSERT)
                    .setData(CalendarContract.Events.CONTENT_URI)
                    .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginOfEvent.getTimeInMillis())
                    .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endOfEvent.getTimeInMillis())
                    .putExtra(CalendarContract.Events.TITLE, "Trening personalny ze Stejsi")
                    .putExtra(CalendarContract.Events.DESCRIPTION, "Będziemy trenować! Zapraszam serdecznie na nasz wspólny trening.")
                    .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
                    .putExtra(CalendarContract.Events.HAS_ALARM, 0);
            if (training.getLocation() != null) {
                addToCalendarIntent.putExtra(CalendarContract.Events.EVENT_LOCATION, training.getLocation().getName() + ", " + training.getLocation().getPostalAddress());
            }
            itemView.getContext().startActivity(addToCalendarIntent);
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
    }

    private void expandCard(TrainingViewHolder holder, boolean expanded, boolean amountToPlanVisible, boolean amountDueVisible) {
        if(expanded) {
            holder.expandMoreBtn.setVisibility(View.GONE);
            holder.expandLessBtn.setVisibility(View.VISIBLE);
            if(amountDueVisible) {
                holder.amountDueTxt.setVisibility(View.VISIBLE);
            }
            if(holder.packageDesc.getText().length() != 0) {
                holder.packageDesc.setVisibility(View.VISIBLE);
            }
            holder.expandedLayout.setVisibility(View.VISIBLE);

            if(amountToPlanVisible) {
                holder.trainingsAmountToPlanTxt.setVisibility(View.VISIBLE);
            }
            else {
                holder.trainingsAmountToPlanTxt.setVisibility(View.GONE);
            }
        }
        else {
            holder.expandMoreBtn.setVisibility(View.VISIBLE);
            holder.expandLessBtn.setVisibility(View.GONE);
            holder.packageDesc.setVisibility(View.GONE);
            holder.expandedLayout.setVisibility(View.GONE);
            holder.amountDueTxt.setVisibility(View.GONE);

            holder.trainingsAmountToPlanTxt.setVisibility(View.GONE);
        }
        holder.expanded = expanded;
    }


    @Override
    public int getItemCount() {
        return trainingPackages.size();
    }

    public static class TrainingViewHolder extends RecyclerView.ViewHolder {

        private final TextView packageTitle;
        private final TextView trainingsAmountTxt;
        private final TextView trainingsDoneAmountTxt;
        private final TextView packageDesc;
        private final TextView trainingsAmountToPlanTxt;
        private final TextView validDueTxt;
        private final TextView amountDueTxt;
        private final ImageButton expandMoreBtn;
        private final ImageButton expandLessBtn;
        private final ImageView statusDot;
        private final ImageView trainingDoneIcon;
        private final ImageView trainingPaidIcon;
        private final ImageView trainingNotPaidIcon;


        private final ConstraintLayout expandedLayout;
        private final ConstraintLayout clickableLayout;

        private boolean expanded;


        public TrainingViewHolder(@NonNull View itemView) {
            super(itemView);

            packageTitle = itemView.findViewById(R.id.trainingCardTitle);
            trainingsAmountTxt  = itemView.findViewById(R.id.trainingCardAmount);
            trainingsDoneAmountTxt = itemView.findViewById(R.id.trainingCardAmountDone);
            packageDesc = itemView.findViewById(R.id.trainingCardDesc);
            expandLessBtn = itemView.findViewById(R.id.trainingCardExpandLessBtn);
            expandMoreBtn = itemView.findViewById(R.id.trainingCardExpandMoreBtn);
            clickableLayout = itemView.findViewById(R.id.trainingICardClickablePart);
            expandedLayout = itemView.findViewById(R.id.trainingCardExpandable);
            trainingsAmountToPlanTxt = itemView.findViewById(R.id.trainingCardAmountLeftToPlan);
            validDueTxt = itemView.findViewById(R.id.trainingCardValidDue);
            statusDot = itemView.findViewById(R.id.trainingCardDot);
            trainingDoneIcon = itemView.findViewById(R.id.trainingCardTrainingDone);
            trainingPaidIcon = itemView.findViewById(R.id.trainingCardPaid);
            trainingNotPaidIcon = itemView.findViewById(R.id.trainingCardNotPaid);
            amountDueTxt = itemView.findViewById(R.id.trainingCardAmountToBePaid);
        }
    }
}
