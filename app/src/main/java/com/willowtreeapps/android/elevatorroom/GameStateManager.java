package com.willowtreeapps.android.elevatorroom;

import android.arch.lifecycle.MutableLiveData;

/**
 * Created by willowtree on 4/25/17.
 */

public class GameStateManager {

    public enum GameState {
        INIT, CALIBRATION, PLAYING
    }

    public final MutableLiveData<GameState> gameState = new DistinctLiveData<>();
    public final MutableLiveData<Boolean> doorsOpen = new DistinctLiveData<>();
    public final MutableLiveData<Integer> currentScore = new MutableLiveData<>();

    public GameStateManager() {
        gameState.setValue(GameState.INIT);
        doorsOpen.setValue(false);
        currentScore.setValue(0);
    }

}
