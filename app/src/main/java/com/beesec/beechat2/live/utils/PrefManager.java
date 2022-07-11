package com.beesec.beechat2.live.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.beesec.beechat2.live.Constants;


public class PrefManager {
    public static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
    }
}
