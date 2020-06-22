package model;

import java.io.Serializable;

public class TrainingLocation implements Serializable {

    private Long id;
    private String name;
    private String postalAddress;
    private Boolean defaultLocation;

    public TrainingLocation() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPostalAddress() {
        return postalAddress;
    }

    public Boolean getDefaultLocation() {
        return defaultLocation;
    }
}
