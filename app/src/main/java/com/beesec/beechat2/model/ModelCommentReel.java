package com.beesec.beechat2.model;

public class ModelCommentReel {

    String cId, comment, timestamp, id, pId;

    public ModelCommentReel() {
    }

    public ModelCommentReel(String cId, String comment, String timestamp, String id, String pId) {
        this.cId = cId;
        this.comment = comment;
        this.timestamp = timestamp;
        this.id = id;
        this.pId = pId;
    }

    public String getcId() {
        return cId;
    }

    public void setcId(String cId) {
        this.cId = cId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
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
}
