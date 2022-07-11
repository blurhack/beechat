package com.beesec.beechat2;

import android.content.Context;
import android.content.SharedPreferences;

public class NightMode {
    final SharedPreferences sharedPreferences;
    public NightMode(Context context){
        sharedPreferences = context.getSharedPreferences("filename", Context.MODE_PRIVATE);

    }
    public void setNightModeState(String state){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("NightMode", state);
        editor.apply();
    }
    public String loadNightModeState(){
        return sharedPreferences.getString("NightMode", "day");
    }

}
