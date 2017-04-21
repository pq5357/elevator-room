package com.willowtreeapps.android.elevatorroom;

import android.app.Application;

import com.android.support.lifecycle.AndroidViewModel;
import com.android.support.lifecycle.ViewModel;
import com.willowtreeapps.android.elevatorroom.BarometerManager;


public class ElevatorViewModel extends ViewModel {
    public BarometerManager barometer;

    public ElevatorViewModel() {
        barometer = BarometerManager.getInstance();
    }
}
