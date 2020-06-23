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
public class TrainingPackage  implements Serializable {

    private Long id;
    private boolean paid;
    private User owner;
    private PackageType packageType;
    private Set<Training> trainings;

    public TrainingPackage() {
    }

    public Long getId() {
        return id;
    }

    public boolean isPaid() {
        return paid;
    }

    public User getOwner() {
        return owner;
    }

    public PackageType getPackageType() {
        return packageType;
    }

    public Set<Training> getTrainings() {
        return trainings;
    }
}
