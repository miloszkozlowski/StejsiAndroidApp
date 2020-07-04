package model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import org.threeten.bp.LocalDateTime;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id",
        scope = User.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class User implements Serializable {

    private Long id;
    private String imie;
    private String nazwisko;
    private String email;
    private int phoneNumber;
    private String activationKey;
    private boolean aktywny;
    private Set<TrainingPackage> trainingPackages;
    private List<TipComment> tipComments;


    public User() {
    }

    public Long getId() {
        return id;
    }

    public String getImie() {
        return imie;
    }

    public String getNazwisko() {
        return nazwisko;
    }

    public String getEmail() {
        return email;
    }

    public int getPhoneNumber() {
        return phoneNumber;
    }

    public String getActivationKey() {
        return activationKey;
    }

    public boolean isAktywny() {
        return aktywny;
    }

    public Set<TrainingPackage> getTrainingPackages() {
        return trainingPackages;
    }

    public Integer getTotalTrainingsDone() {
        return getTrainingPackages().stream()
                .mapToInt(p -> p.getAmountTrainingsDone().intValue())
                .sum();
    }

    public float getLastFourWeeksAvgTrainingsDone() {
        LocalDateTime dateFourWeeksAgo = LocalDateTime.now().minusDays(28);
        Long trainings = getTrainingPackages().stream()
                .flatMap(p -> p.getTrainings().stream())
                .filter(t -> t.getMarkedAsDone() != null && t.getScheduledFor().isAfter(dateFourWeeksAgo))
                .count();
        return trainings / 4;
    }


    public List<TipComment> getTipComments() {
        return tipComments;
    }

    public Integer getUnconfirmedTrainings(String currentTime) {
        Long amount = getTrainingPackages().stream()
                .flatMap(p -> p.getTrainings().stream())
                .filter(t -> t.getScheduleConfirmed() == null || t.getStatus(currentTime) == TrainingStatus.PRESENCE_TO_CONFIRM)
                .count();
        return amount.intValue();
    }
}
