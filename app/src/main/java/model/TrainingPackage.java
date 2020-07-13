package model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.threetenbp.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.threetenbp.ser.LocalDateTimeSerializer;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.temporal.ChronoUnit;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id",
        scope = TrainingPackage.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrainingPackage  implements Serializable {

    private Long id;
    private boolean paid;
    private boolean closed;
    private User owner;
    private PackageType packageType;
    private Set<Training> trainings;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime whenCreated;

    public TrainingPackage() {
    }

    public Long getId() {
        return id;
    }

    public boolean isPaid() {
        return paid;
    }

    public boolean isClosed() {
        return closed;
    }

//    public User getOwner() {
//        return owner;
//    }

    public PackageType getPackageType() {
        return packageType;
    }

    public Set<Training> getTrainings() {
        return trainings;
    }

    private LocalDateTime getWhenCreated() {
        return whenCreated;
    }

    public Boolean isOpened() {
        return (getTrainings().stream().anyMatch(t -> t.getMarkedAsDone() == null));
    }



    public Long getAmountTrainingsDone() {
        return getTrainings().stream()
                .filter(t -> t.getMarkedAsDone() != null || t.getPresenceConfirmedByUser() != null)
                .count();
    }

    public Long getAmountToPlan() {
        return getTrainings().stream()
                .filter(t -> t.getScheduledFor() == null)
                .count();
    }


    public Boolean isCurrentlyUsed() {
        if(closed) {
            return false;
        }
       long amountUnplanned = getTrainings().stream()
                .filter(t -> t.getScheduledFor() == null)
                .count();

        if(amountUnplanned > 0) {
            return true;
        }
        else {
           long amountWithoutPresence = getTrainings().stream()
                   .filter(t -> t.getPresenceConfirmedByUser() == null)
                   .count();

           return 0 < amountWithoutPresence;
       }

    }

    public LocalDateTime getLastTraining() {
        return getTrainings().stream()
                .map(Training::getScheduledFor)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo).orElse(LocalDateTime.MIN);
    }


    public Long getValidityDays(String currentTime) {
        if(getPackageType().getDaysValid() == 0) {
            return null;
        }
        LocalDateTime now = countNow(currentTime);
        LocalDateTime validDue = getWhenCreated().plusDays(getPackageType().getDaysValid());
        return ChronoUnit.DAYS.between(now, validDue);
    }

    public boolean isValid(String currentTime) {
        if(getPackageType().getDaysValid() == 0) {
            return true;
        }
        LocalDateTime now = countNow(currentTime);
        LocalDateTime validDue = getWhenCreated().plusDays(getPackageType().getDaysValid());
        return now.isBefore(validDue);
    }

    public TrainingPackageStatus getStatus(String currentTime) {

        long trainingsPlanned = getPackageType().getAmountOfTrainings() - getAmountToPlan();

        long trainingsPresenceConfirmed = getTrainings().stream()
                .filter(t -> t.getPresenceConfirmedByUser() != null)
                .count();

        long trainingsScheduleConfirmed = getTrainings().stream()
                .filter(t -> t.getScheduledFor() != null)
                .filter(t -> t.getScheduledFor().isAfter(countNow(currentTime)))
                .filter(t -> t.getScheduleConfirmed() != null)
                .count();

        long pastTrainingsAmount = getTrainings().stream()
                .filter(t -> t.getScheduledFor() != null)
                .filter(t -> t.getScheduledFor().isBefore(countNow(currentTime)))
                .count();

        Long trainingsDone = getAmountTrainingsDone();

        if(trainingsDone.intValue() == getPackageType().getAmountOfTrainings()) {
            return TrainingPackageStatus.USED;
        }

        if(!isValid(currentTime)) {
            return TrainingPackageStatus.OUT_DATED;
        }

        if(trainingsPlanned - pastTrainingsAmount > trainingsScheduleConfirmed) {
            return TrainingPackageStatus.HAS_TRAININGS_TO_CONFIRM_SCHEDULE;
        }

        if(pastTrainingsAmount > trainingsPresenceConfirmed) {
            return TrainingPackageStatus.HAS_TRAININGS_TO_CONFIRM_PRESENCE;
        }
        if(getValidityDays(currentTime) != null && isValid(currentTime)) {
            if (getValidityDays(currentTime) < 7 && getAmountToPlan() >= 2) {
                return TrainingPackageStatus.CLOSE_TO_OUT_DATE;
            }

            if (getValidityDays(currentTime) < 4 && getAmountToPlan() >= 1) {
                return TrainingPackageStatus.CLOSE_TO_OUT_DATE;
            }
        }
        return TrainingPackageStatus.OK;

    }

    private LocalDateTime countNow(String currentTime) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
         return LocalDateTime.parse(currentTime, df);
    }

}
