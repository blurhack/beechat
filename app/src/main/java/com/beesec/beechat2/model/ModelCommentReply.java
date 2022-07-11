package com.beesec.beechat2.model;

@SuppressWarnings("ALL")
public class ModelCommentReply {

    String cId, comment, timestamp, id, rId;

    public ModelCommentReply() {
    }

    public ModelCommentReply(String cId, String comment, String timestamp, String id, String rId) {
        this.cId = cId;
        this.comment = comment;
        this.timestamp = timestamp;
        this.id = id;
        this.rId = rId;
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

    public String getrId() {
        return rId;
    }

    public void setrId(String rId) {
        this.rId = rId;
    }
}
