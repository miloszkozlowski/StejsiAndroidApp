package model;

public enum TrainingStatus {
    DONE("wykonany"),
    CANCELED("w trakcie odwoływania"),
    SCHEDULE_TO_CONFIRM("potwierdź termin"),
    PRESENCE_TO_CONFIRM("potwierdź obecność"),
    UNPLANNED("niezaplanowany"),
    READY_TO_HAPPEN(""),
    UNKNOWN("obecność potwierdzona");

    private final String description;

    TrainingStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        if(description.isEmpty()) {
            return description;
        }
        return "";
    }
}
