package model;

import model.recyclerViews.MainViewElement;

public class Tip implements MainViewElement {

    private String title;
    private String bodyText;

    private String imageUrl;

    public Tip(String title, String bodyText, String imageUrl) {
        this.title = title;
        this.bodyText = bodyText;
        this.imageUrl = imageUrl;

    }

    public String getTitle() {
        return title;
    }

    public String getBodyText() {
        return bodyText;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    @Override
    public int getModelType() {
        return MainViewElement.TIP_VIEW_TYPE;
    }
}
