package model;

import java.io.Serializable;

public enum UserRank implements Serializable {
    LESER("Leser"),
    BEGINNER("Żółtodziub"),
    FIT_MALINA("Fit Malina"),
    OSIŁEK("Osiłek"),
    KULTURYSTA("Kulturysta");

    private final String description;

    private UserRank(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
