package com.beesec.beechat2.model;

public class ModelReel {

    String id,pId,text,comment,video,privacy,pTime;

    public ModelReel() {
    }

    public ModelReel(String id, String pId, String text, String comment, String video, String privacy, String pTime) {
        this.id = id;
        this.pId = pId;
        this.text = text;
        this.comment = comment;
        this.video = video;
        this.privacy = privacy;
        this.pTime = pTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public String getPrivacy() {
        return privacy;
    }

    public void setPrivacy(String privacy) {
        this.privacy = privacy;
    }

    public String getpTime() {
        return pTime;
    }

    public void setpTime(String pTime) {
        this.pTime = pTime;
    }
}
