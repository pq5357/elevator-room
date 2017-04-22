package com.willowtreeapps.android.elevatorroom;

import android.arch.lifecycle.ViewModel;


public class ElevatorViewModel extends ViewModel {
    public BarometerManager barometer;

    public ElevatorViewModel() {
        barometer = BarometerManager.getInstance();
    }
}
