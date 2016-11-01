package com.alexberdnikov.diskspacewidget;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedPrefs {
    private Context mContext;
    private static SharedPrefs sInstance;

    private SharedPrefs(Context context) {
        mContext = context;
    }

    public static SharedPrefs getsInstance(Context context) {
        if (sInstance == null) {
            sInstance = new SharedPrefs(context);
        }

        return sInstance;
    }

    public SharedPreferences getDefaultPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(mContext);
    }
}
