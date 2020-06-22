package model.recyclerViews;

import java.time.LocalDateTime;

import model.Training;
import model.TrainingLocation;
import model.TrainingPackage;

public class TrainingForScheduleConfirmation implements MainViewElement {

    private long id;

    private LocalDateTime scheduledFor;
    private LocalDateTime markedAsDone;
    private LocalDateTime scheduleConfirmed;
    private LocalDateTime presenceConfirmedByUser;
    private TrainingPackage trainingPackage;
    private TrainingLocation location;

    public TrainingForScheduleConfirmation(Training training) {
        this.id = training.getId();
        this.markedAsDone = training.getMarkedAsDone();
        this.scheduledFor = training.getScheduledFor();
        this.scheduleConfirmed = training.getScheduleConfirmed();
        this.presenceConfirmedByUser = training.getPresenceConfirmedByUser();
        this.trainingPackage = training.getTrainingPackage();
        this.location = training.getLocation();
    }

    public long getId() {
        return id;
    }

    public LocalDateTime getScheduledFor() {
        return scheduledFor;
    }

    public LocalDateTime getMarkedAsDone() {
        return markedAsDone;
    }

    public LocalDateTime getScheduleConfirmed() {
        return scheduleConfirmed;
    }

    public LocalDateTime getPresenceConfirmedByUser() {
        return presenceConfirmedByUser;
    }

    public TrainingPackage getTrainingPackage() {
        return trainingPackage;
    }

    public TrainingLocation getLocation() {
        return location;
    }

    public boolean isDone() {
        return markedAsDone != null;
    }

    public boolean isConfirmed() {
        return scheduleConfirmed != null;
    }

    public boolean isPresenceConfirmed() {
        return presenceConfirmedByUser != null;
    }

    @Override
    public int getModelType() {
        return MainViewElement.TRAINING_SCHEDULE_CONFIRM_TYPE;
    }
}
