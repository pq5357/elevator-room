package com.willowtreeapps.android.elevatorroom;

import android.app.Application;
import android.content.Context;

import com.willowtreeapps.android.elevatorroom.dagger.AppComponent;
import com.willowtreeapps.android.elevatorroom.dagger.DaggerAppComponent;

import timber.log.Timber;

/**
 * Created by willowtree on 4/20/17.
 */

public class MyApplication extends Application {

    private AppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        appComponent = DaggerAppComponent.builder().context(this).build();
    }

    public static AppComponent getAppComponent(Context context) {
        return ((MyApplication) context.getApplicationContext()).appComponent;
    }

}
