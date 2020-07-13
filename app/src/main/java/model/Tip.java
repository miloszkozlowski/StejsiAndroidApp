package model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.threetenbp.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.threetenbp.ser.LocalDateTimeSerializer;

import org.threeten.bp.LocalDateTime;

import java.io.Serializable;
import java.util.Set;

import model.recyclerViews.MainViewElement;

public class Tip implements MainViewElement, Serializable {

    private Long id;
    private String body;
    private String heading;
    private String imageUrl;
    private boolean localImagePresent;
    private Set<TipComment> comments;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime whenCreated;
    private TipReadStatus tipStatusByUser;



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

    public TipReadStatus getTipStatusByUser() {
        return tipStatusByUser;
    }

    public void setTipStatusByUser(TipReadStatus tipStatusByUser) {
        this.tipStatusByUser = tipStatusByUser;
    }



    @Override
    @JsonIgnore
    public int getModelType() {
        return MainViewElement.TIP_VIEW_TYPE;
    }
}
