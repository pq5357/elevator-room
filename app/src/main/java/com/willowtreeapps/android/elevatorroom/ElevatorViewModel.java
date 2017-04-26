package com.willowtreeapps.android.elevatorroom;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;

import com.willowtreeapps.android.elevatorroom.persistence.GameDatabase;
import com.willowtreeapps.android.elevatorroom.persistence.VisitedFloor;

import timber.log.Timber;

public class ElevatorViewModel extends ViewModel {

    public BarometerManager barometer;
    private GameDatabase database;
    private VisitedFloor currentFloor;
    private float minPressure; // highest altitude
    public PressureIncrease maxPressureLive; // lowest altitude

    private Observer<Float> pressureObserver = aFloat -> {
        if (aFloat == null) {
            return;
        }
        int floor = (int) (aFloat * 100);
        if (currentFloor != null && floor == currentFloor.getFloor()) {
            return; // floor hasn't changed
        }
        database.floorDao().insertFloor(new VisitedFloor(floor));
    };

    public ElevatorViewModel() {
        barometer = BarometerManager.getInstance();
        database = MyApplication.getGameDatabase();
        maxPressureLive = new PressureIncrease(barometer);
    }

    public void recordMinPressure() {
        minPressure = barometer.getValue();
        maxPressureLive.setThreshold(minPressure + 0.1f);
    }

    public void recordMaxPressure() {
        float maxPressure = maxPressureLive.getValue();
        Timber.d("minPressure %f", minPressure);
        Timber.d("maxPressure %f", maxPressure);
        // TODO calculate floors
    }


    public void writePressureToDatabase(LifecycleOwner owner) {
        database.currentFloor().subscribe(floor -> currentFloor = floor);
        barometer.observe(owner, pressureObserver);
    }

}
