package com.willowtreeapps.android.elevatorroom;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;

/**
 * Created by willowtree on 4/26/17.
 * <p>
 * keeps track of if the pressure increases past some threshold
 * value will be max pressure, or -1 if threshold hasn't been reached
 */
public class PressureIncrease extends LiveData<Float> {

    private BarometerManager barometer;
    private float min;
    private Observer<Float> pressureObserver = aFloat -> {
        if (aFloat > min && aFloat > getValue()) {
            setValue(aFloat);
        }
    };

    public PressureIncrease(BarometerManager barometer) {
        this.barometer = barometer;
        setThreshold(Float.MAX_VALUE);
    }

    public void setThreshold(float min) {
        setValue(-1f);
        this.min = min;
    }

    @Override
    protected void onActive() {
        super.onActive();
        barometer.observeForever(pressureObserver);
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        barometer.removeObserver(pressureObserver);
    }

}
