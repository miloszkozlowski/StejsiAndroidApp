package pl.mihome.stejsiapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import org.threeten.bp.format.DateTimeFormatter;

import java.io.IOException;
import java.util.List;

import app.StejsiApplication;
import model.Tip;
import model.TipReadStatus;
import model.Token;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import pl.mihome.stejsiapp.R;

public class TipViewAdapter extends RecyclerView.Adapter<TipViewAdapter.TipViewHolder> {

    public static final String TIP_TO_SHOW = "TIP_TO_SHOW";
    private List<Tip> tips;
    private Bundle mainBundle;
    private StejsiApplication app;
    private Activity activity;

    private Token currentToken;

    private OkHttpClient httpClient;

    TipViewAdapter(List<Tip> tips, Activity activity, StejsiApplication app) {
        this.tips = tips;
        this.app = app;
        this.activity = activity;
        this.mainBundle = app.getMainBundle();
        this.currentToken = (Token) mainBundle.getSerializable(StartActivity.TOKEN);
        this.httpClient = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request newRequest = chain.request().newBuilder()
                                .addHeader("token", currentToken.getTokenString())
                                .addHeader("deviceId", Settings.Secure.getString(app.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID))
                                .build();
                        return chain.proceed(newRequest);
                    }
                })
                .build();
    }


    @NonNull
    @Override
    public TipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.tip_view_card, parent, false);
        TipViewHolder viewHolder = new TipViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull TipViewHolder holder, int position) {
        final Tip tip = tips.get(position);
        holder.tipTitle.setText(tip.getHeading());
        holder.tipBody.setText(tip.getBody());
        if(tip.getTipStatusByUser().equals(TipReadStatus.NEW)) {
            holder.tipStatusDot.setVisibility(View.VISIBLE);
        }
        else {
            holder.tipStatusDot.setVisibility(View.GONE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            holder.tipBody.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
        }

        Picasso picasso = new Picasso.Builder(holder.itemView.getContext())
                .downloader(new OkHttp3Downloader(httpClient))
                .build();

        picasso.load(StartActivity.WEB_SERVER_URL + "/userinput/tipthumbimage/" + tip.getId()).into(holder.tipThumbnail);

        if(tip.getComments().isEmpty()) {
            holder.tipCommentsInfo.setText(R.string.tip_no_comments_short);
        }
        else {
            holder.tipCommentsInfo.setText(holder.itemView.getResources().getString(R.string.tip_comments_info, "(" + tip.getComments().size() + ")"));
        }

        DateTimeFormatter df = DateTimeFormatter.ofPattern("d MMMM yyyy HH:mm");
        holder.tipDatetime.setText(df.format(tip.getWhenCreated()));

        holder.clickableSurface.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, ShowTipActivity.class);
                intent.putExtra(TIP_TO_SHOW, tip);
                activity.startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return tips.size();
    }

    public static class TipViewHolder extends RecyclerView.ViewHolder {

        private final TextView tipTitle;
        private final TextView tipBody;
        private final TextView tipDatetime;
        private final TextView tipCommentsInfo;
        private final ImageView tipThumbnail;
        private final LinearLayout clickableSurface;
        private final ImageView tipStatusDot;

        public TipViewHolder(@NonNull View itemView) {
            super(itemView);

            tipTitle = itemView.findViewById(R.id.tipCardTitle);
            tipBody  = itemView.findViewById(R.id.tipCardBody);
            tipDatetime = itemView.findViewById(R.id.tipCardDateTime);
            tipCommentsInfo = itemView.findViewById(R.id.tipCardCommentsInfo);
            tipThumbnail = itemView.findViewById(R.id.tipCardThumbnail);
            clickableSurface = itemView.findViewById(R.id.tipCardClicablePart);
            tipStatusDot = itemView.findViewById(R.id.tipCardDot);
        }
    }
}
