package com.beesec.beechat2.model;

public class ModelGroups {

    String groupId, gName, gUsername, gIcon;

    public ModelGroups() {
    }

    public ModelGroups(String groupId, String gName, String gUsername, String gIcon) {
        this.groupId = groupId;
        this.gName = gName;
        this.gUsername = gUsername;
        this.gIcon = gIcon;
    }

    public String getGroupId() {
        return groupId;
    }


    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }


    public String getgName() {
        return gName;
    }



    public void setgName(String gName) {
        this.gName = gName;

    }


    public String getgUsername() {
        return gUsername;
    }

    public void setgUsername(String gUsername) {
        this.gUsername = gUsername;
    }

    public String getgIcon() {
        return gIcon;
    }


    public void setgIcon(String gIcon) {
        this.gIcon = gIcon;
    }

}
