package model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.threetenbp.deser.LocalDateTimeDeserializer;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.Serializable;


@JsonIgnoreProperties(ignoreUnknown = true)
public class Training implements Serializable {

    private long id;

    private LocalDateTime scheduledFor;
    private LocalDateTime markedAsDone;
    private LocalDateTime scheduleConfirmed;
    private LocalDateTime presenceConfirmedByUser;
    private LocalDateTime whenCanceled;
    private TrainingPackage trainingPackage;
    private TrainingLocation location;
    

    public Training() {
    }

    public long getId() {
        return id;
    }

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public LocalDateTime getScheduledFor() {
        return scheduledFor;
    }

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public LocalDateTime getMarkedAsDone() {
        return markedAsDone;
    }

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public LocalDateTime getScheduleConfirmed() {
        return scheduleConfirmed;
    }

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public LocalDateTime getWhenCanceled() {
        return whenCanceled;
    }

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public LocalDateTime getPresenceConfirmedByUser() {
        return presenceConfirmedByUser;
    }

    public TrainingPackage getTrainingPackage() {
        return trainingPackage;
    }

    public TrainingLocation getLocation() {
        return location;
    }

    public void setScheduleConfirmed(LocalDateTime scheduleConfirmed) {
        this.scheduleConfirmed = scheduleConfirmed;
    }

    public void setPresenceConfirmedByUser(LocalDateTime presenceConfirmedByUser) {
        this.presenceConfirmedByUser = presenceConfirmedByUser;
    }

    public void setWhenCanceled(LocalDateTime whenCanceled) {
        this.whenCanceled = whenCanceled;
    }

    public TrainingStatus getStatus(String currentTime) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        LocalDateTime now = LocalDateTime.parse(currentTime, df);
        if(scheduledFor == null) {
            return TrainingStatus.UNPLANNED;
        }

        if(whenCanceled != null) {
            return TrainingStatus.CANCELED;
        }

        if(scheduleConfirmed == null && scheduledFor.isAfter(now)) {
            return TrainingStatus.SCHEDULE_TO_CONFIRM;
        }

        if(scheduledFor.isAfter(now) || scheduledFor.isEqual(now)) {
            return TrainingStatus.READY_TO_HAPPEN;
        }

        if(scheduledFor.isBefore(now) && presenceConfirmedByUser == null) {
            return TrainingStatus.PRESENCE_TO_CONFIRM;
        }

        if(scheduledFor.isBefore(now) && markedAsDone == null) {
            return TrainingStatus.UNKNOWN;
        }

        return TrainingStatus.DONE;
    }

    public boolean isDone(String currentTime) {
        return getStatus(currentTime) == TrainingStatus.UNKNOWN || getStatus(currentTime) == TrainingStatus.DONE;
    }
}
