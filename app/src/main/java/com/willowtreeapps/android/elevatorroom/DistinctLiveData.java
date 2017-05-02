package com.willowtreeapps.android.elevatorroom;

import android.arch.lifecycle.MutableLiveData;

/**
 * Created by willowtree on 5/2/17.
 * <p>
 * only accepts new values that are distinct from the old value
 */

public class DistinctLiveData<T> extends MutableLiveData<T> {

    @Override
    public void setValue(T value) {
        if (value != getValue()) {
            super.setValue(value);
        }
    }

}
