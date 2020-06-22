package model.recyclerViews;

public interface MainViewElement
{
    public static final int TRAINING_PRESENCE_TYPE = 1;
    public static final int TIP_VIEW_TYPE = 2;
    public static final int TRAINING_SCHEDULE_CONFIRM_TYPE = 3;

    public int getModelType();
}
