package com.willowtreeapps.android.elevatorroom;

import android.app.Application;
import android.arch.persistence.room.Room;
import android.content.Context;

import com.willowtreeapps.android.elevatorroom.persistence.GameDatabase;

import timber.log.Timber;

/**
 * Created by willowtree on 4/20/17.
 */

public class MyApplication extends Application {

    private static MyApplication sApplication;
    private static GameDatabase gameDatabase;

    public static Context getContext() {
        return sApplication;
    }

    public static GameDatabase getGameDatabase() {
        return gameDatabase;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        setupDatabases();
    }

    private void setupDatabases() {
        gameDatabase = Room.databaseBuilder(this, GameDatabase.class, "game.db")
                .build();
    }

}
