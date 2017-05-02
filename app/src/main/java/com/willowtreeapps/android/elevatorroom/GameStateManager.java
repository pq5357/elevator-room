package com.willowtreeapps.android.elevatorroom;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;

/**
 * Created by willowtree on 4/25/17.
 */

public class GameStateManager implements LifecycleObserver {

    public enum GameState {
        INIT, CALIBRATION, CAN_PLAY, PLAYING
    }

    private GameState gameState = GameState.INIT;
    private final LifecycleOwner lifecycleOwner;
    private StateChangeListener stateChangeListener;

    public GameStateManager(LifecycleOwner lifecycleOwner, StateChangeListener stateChangeListener) {
        this.lifecycleOwner = lifecycleOwner;
        this.lifecycleOwner.getLifecycle().addObserver(this);
        this.stateChangeListener = stateChangeListener;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        if (this.gameState != gameState) {
            this.gameState = gameState;
            updateState();
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    void applyState() {
        if (stateChangeListener != null) {
            stateChangeListener.onApplyState(gameState);
        }
    }

    void updateState() {
        if (stateChangeListener != null) {
            stateChangeListener.onStateChanged(gameState);
        }
        applyState();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    void onPause() {
        if (getGameState() == GameState.CAN_PLAY) {
            setGameState(GameState.PLAYING);
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    void onDestroy() {
        lifecycleOwner.getLifecycle().removeObserver(this);
    }

    public interface StateChangeListener {

        /**
         * called when state changes
         */
        void onStateChanged(GameState newState);

        /**
         * may be called on same state multiple times
         */
        void onApplyState(GameState currentState);
    }

}
