package com.beesec.beechat2.model;

public class ModelHigh {

    public String uri;
    public String type;
    public String storyid;
    public String userid;

    public ModelHigh() {
    }

    public ModelHigh(String uri, String type, String storyid, String userid) {
        this.uri = uri;
        this.type = type;
        this.storyid = storyid;
        this.userid = userid;
    }



    public String getUri() {
        return uri;
    }




   public void setUri(String uri) {
        this.uri = uri;
}





    public String getType() {
        return type;
    }



    public void setType(String type) {
        this.type = type;
    }

    public String getStoryid() {
        return storyid;
    }

    public void setStoryid(String storyid) {
        this.storyid = storyid;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }
}
