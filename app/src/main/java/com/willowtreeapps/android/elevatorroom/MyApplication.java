package com.willowtreeapps.android.elevatorroom;

import android.app.Application;
import android.content.Context;

import timber.log.Timber;

/**
 * Created by willowtree on 4/20/17.
 */

public class MyApplication extends Application {

    private static MyApplication sApplication;

    public static Context getContext() {
        return sApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }

}
