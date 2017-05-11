package com.willowtreeapps.android.elevatorroom.dagger;

import android.arch.persistence.room.Room;
import android.content.Context;

import com.willowtreeapps.android.elevatorroom.GameStateManager;
import com.willowtreeapps.android.elevatorroom.livedata.BarometerManager;
import com.willowtreeapps.android.elevatorroom.persistence.GameDatabase;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by willowtree on 5/10/17.
 */
@Module
public class GameModule {

    @Singleton
    @Provides
    static GameStateManager provideGameStateManager() {
        return new GameStateManager();
    }

    @Singleton
    @Provides
    static GameDatabase provideGameDatabase(Context context) {
        return Room.databaseBuilder(context, GameDatabase.class, "game.db").build();
    }

    @Singleton
    @Provides
    static BarometerManager provideBarometerManager(Context context) {
        return new BarometerManager(context);
    }

}
