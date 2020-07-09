package model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import java.io.Serializable;
import java.util.List;

@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id",
        scope = User.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class User implements Serializable {

    private Long id;
    private String name;
    private String surname;
    private String email;
    private Integer phoneNumber;
    private boolean active;
    private boolean settingTipNotifications;

    private List<TrainingPackage> trainingPackages;
    private List<TipComment> tipComments;

    private UserStats stats;


    public User() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(Integer phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<TrainingPackage> getTrainingPackages() {
        return trainingPackages;
    }

    public void setTrainingPackages(List<TrainingPackage> trainingPackages) {
        this.trainingPackages = trainingPackages;
    }

    public List<TipComment> getTipComments() {
        return tipComments;
    }

    public void setTipComments(List<TipComment> tipComments) {
        this.tipComments = tipComments;
    }

    public UserStats getStats() {
        return stats;
    }

    public void setStats(UserStats stats) {
        this.stats = stats;
    }

    public boolean isSettingTipNotifications() {
        return settingTipNotifications;
    }

    public void setSettingTipNotifications(boolean settingTipNotifications) {
        this.settingTipNotifications = settingTipNotifications;
    }
}
