package com.willowtreeapps.android.elevatorroom;

import android.arch.lifecycle.LiveData;
import android.view.View;
import android.view.ViewTreeObserver;

/**
 * Created by willowtree on 5/4/17.
 * <p>
 * currently just reports onWindowAttached. could add more events as needed
 */

public class ViewTreeLiveData extends LiveData<Void> implements ViewTreeObserver.OnWindowAttachListener {

    private View view;

    public ViewTreeLiveData(View view) {
        this.view = view;
    }

    @Override
    protected void onActive() {
        super.onActive();
        view.getViewTreeObserver().addOnWindowAttachListener(this);
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        view.getViewTreeObserver().removeOnWindowAttachListener(this);
    }

    @Override
    public void onWindowAttached() {
        setValue(null);

    }

    @Override
    public void onWindowDetached() {
        // don't care
    }

}
