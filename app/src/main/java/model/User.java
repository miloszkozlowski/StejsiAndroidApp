package model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import java.io.Serializable;
import java.util.Set;

@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
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
}
