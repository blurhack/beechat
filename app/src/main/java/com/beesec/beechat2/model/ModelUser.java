package com.beesec.beechat2.model;

public class ModelUser {

    String name, username, location, photo, id, verified,typingTo;
    boolean isBlocked = false;

    public ModelUser() {
    }

    public ModelUser(String name, String username, String location, String photo, String id, String verified, String typingTo, boolean isBlocked) {
        this.name = name;
        this.username = username;
        this.location = location;
        this.photo = photo;
        this.id = id;
        this.verified = verified;
        this.typingTo = typingTo;
        this.isBlocked = isBlocked;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVerified() {
        return verified;
    }

    public void setVerified(String verified) {
        this.verified = verified;
    }

    public String getTypingTo() {
        return typingTo;
    }

    public void setTypingTo(String typingTo) {
        this.typingTo = typingTo;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }
}
