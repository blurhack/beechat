package com.beesec.beechat2.welcome;

public class ScreenItem {

    String Title;
    int screenImg;

    public ScreenItem(String title, int screenImg) {
        Title = title;
        this.screenImg = screenImg;
    }


    public void setTitle(String title) {
        Title = title;
    }

    public void setScreenImg(int screenImg) {
        this.screenImg = screenImg;
    }


    public String getTitle() {
        return Title;
    }


    public int getScreenImg() {
        return screenImg;
    }
}
