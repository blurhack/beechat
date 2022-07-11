package com.beesec.beechat2.model;

public class ModelVerification {

    String name, username, vId, uID, link, known;

    public ModelVerification() {
    }

    public ModelVerification(String name, String username, String vId, String uID, String link, String known) {
        this.name = name;
        this.username = username;
        this.vId = vId;
        this.uID = uID;
        this.link = link;
        this.known = known;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getvId() {
        return vId;
    }

    public void setvId(String vId) {
        this.vId = vId;
    }

    public String getuID() {
        return uID;
    }

    public void setuID(String uID) {
        this.uID = uID;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getKnown() {
        return known;
    }

    public void setKnown(String known) {
        this.known = known;
    }
}
