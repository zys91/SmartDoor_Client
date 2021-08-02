package com.hhuc.smartdoor;

import android.app.Application;

public class MainApplication extends Application {

    public static MainApplication mainApplication;
    public static String app_uid = null;
    public static String app_hid = null;
    public static String app_tel = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mainApplication = this;
    }

}
