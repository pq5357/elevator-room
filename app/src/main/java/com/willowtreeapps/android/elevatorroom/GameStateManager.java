package com.willowtreeapps.android.elevatorroom;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.graphics.Rect;
import android.view.View;

/**
 * Created by willowtree on 4/25/17.
 */

public class GameStateManager {

    public static final int FRAME_LENGTH = 12; // length of a frame in the game

    public enum GameState {
        INIT, CALIBRATION, PLAYING
    }

    public final MutableLiveData<GameState> gameState = new DistinctLiveData<>();
    public final MutableLiveData<Boolean> doorsOpen = new DistinctLiveData<>();
    public final MutableLiveData<Integer> currentScore = new MutableLiveData<>();
    public final MultiWindowDivider multiWindowDividerSize = new MultiWindowDivider();

    public GameStateManager() {
        gameState.setValue(GameState.INIT);
        doorsOpen.setValue(false);
        currentScore.setValue(0);
    }

    public static class MultiWindowDivider extends LiveData<Integer> {

        private int left;
        private int right;

        public void setLeftView(final LifecycleOwner lifecycleOwner, final View view) {
            left = Integer.MIN_VALUE;
            final ViewTreeLiveData liveData = new ViewTreeLiveData(view);
            liveData.observe(lifecycleOwner, aVoid -> {
                Rect outRect = new Rect();
                view.getWindowVisibleDisplayFrame(outRect);
                if (left > 0 && left == outRect.right + 1) { // got same value twice. stop listening
                    liveData.removeObservers(lifecycleOwner);
                }
                left = outRect.right + 1;
                update();
            });
        }

        public void setRightView(final LifecycleOwner lifecycleOwner, final View view) {
            right = Integer.MIN_VALUE;
            final ViewTreeLiveData liveData = new ViewTreeLiveData(view);
            liveData.observe(lifecycleOwner, aVoid -> {
                Rect outRect = new Rect();
                view.getWindowVisibleDisplayFrame(outRect);
                if (right > 0 && right == outRect.left - 1) { // got same value twice. stop listening
                    liveData.removeObservers(lifecycleOwner);
                }
                right = outRect.left - 1;
                update();
            });
        }

        private void update() {
            if (left > 0 && right > 0 && right >= left) {
                setValue(right - left);
            }
        }

    }

}
