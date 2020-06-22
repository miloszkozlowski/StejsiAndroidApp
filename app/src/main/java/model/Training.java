package model;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;

import java.io.Serializable;
import java.time.LocalDateTime;


public class Training implements Serializable {

    private long id;

    private LocalDateTime scheduledFor;
    private LocalDateTime markedAsDone;
    private LocalDateTime scheduleConfirmed;
    private LocalDateTime presenceConfirmedByUser;
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
    public LocalDateTime getPresenceConfirmedByUser() {
        return presenceConfirmedByUser;
    }

    public TrainingPackage getTrainingPackage() {
        return trainingPackage;
    }

    public TrainingLocation getLocation() {
        return location;
    }
}
