package model;

public class UserStats {

    private Integer totalTrainingsDone;
    private float lastFourWeeksAvgTrainingsDone;
    private Integer unconfirmedTrainings;
    private float progressPoints;
    private UserRank rank;

    public UserStats() {
    }

    public Integer getTotalTrainingsDone() {
        return totalTrainingsDone;
    }

    public float getLastFourWeeksAvgTrainingsDone() {
        return lastFourWeeksAvgTrainingsDone;
    }

    public Integer getUnconfirmedTrainings() {
        return unconfirmedTrainings;
    }

    public float getProgressPoints() {
        return progressPoints;
    }

    public UserRank getRank() {
        return rank;
    }
}
