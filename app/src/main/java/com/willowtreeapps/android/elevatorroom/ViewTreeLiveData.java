package com.willowtreeapps.android.elevatorroom;

import android.arch.lifecycle.LiveData;
import android.view.View;
import android.view.ViewTreeObserver;

/**
 * Created by willowtree on 5/4/17.
 * <p>
 * currently just reports onWindowAttached. could add more events as needed
 */

public class ViewTreeLiveData extends LiveData<Void> implements ViewTreeObserver.OnGlobalLayoutListener {

    private View view;

    public ViewTreeLiveData(View view) {
        this.view = view;
    }

    @Override
    protected void onActive() {
        super.onActive();
        view.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
    }

    @Override
    public void onGlobalLayout() {
        setValue(null);
    }

}
