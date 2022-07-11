package com.beesec.beechat2.model;

public class ModelLive {
    String room,userid;

    public ModelLive() {
    }

    public ModelLive(String room, String userid) {
        this.room = room;
        this.userid = userid;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }
}
