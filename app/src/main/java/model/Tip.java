package model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

import model.recyclerViews.MainViewElement;

public class Tip implements MainViewElement, Serializable {

    private Long id;
    private String body;
    private String heading;
    private String imageUrl;
    private Set<TipComment> comments;
    private boolean localImagePresent;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime whenCreated;

    public Tip() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getHeading() {
        return heading;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Set<TipComment> getComments() {
        return comments;
    }

    public void setComments(Set<TipComment> comments) {
        this.comments = comments;
    }

    public boolean isLocalImagePresent() {
        return localImagePresent;
    }

    public void setLocalImagePresent(boolean localImagePresent) {
        this.localImagePresent = localImagePresent;
    }

    public LocalDateTime getWhenCreated() {
        return whenCreated;
    }

    public void setWhenCreated(LocalDateTime whenCreated) {
        this.whenCreated = whenCreated;
    }

    @Override
    public int getModelType() {
        return MainViewElement.TIP_VIEW_TYPE;
    }
}
