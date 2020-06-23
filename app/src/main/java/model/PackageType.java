package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PackageType implements Serializable {

    private Long id;
    private String title;
    private String description;
    private Integer amountOfTrainings;
    private Integer lengthMinutes;
    private BigDecimal pricePLN;
    private boolean active;
    private Integer daysValid;
    private Set<TrainingPackage> pakiety;

    public PackageType() {
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Integer getAmountOfTrainings() {
        return amountOfTrainings;
    }

    public Integer getLengthMinutes() {
        return lengthMinutes;
    }

    public BigDecimal getPricePLN() {
        return pricePLN;
    }

    public boolean isActive() {
        return active;
    }

    public Integer getDaysValid() {
        return daysValid;
    }

    public Set<TrainingPackage> getPakiety() {
        return pakiety;
    }
}
