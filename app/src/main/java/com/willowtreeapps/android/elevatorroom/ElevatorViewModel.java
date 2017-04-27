package com.willowtreeapps.android.elevatorroom;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;

import com.willowtreeapps.android.elevatorroom.persistence.GameDatabase;
import com.willowtreeapps.android.elevatorroom.persistence.VisitedFloor;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class ElevatorViewModel extends ViewModel {

    static final int TOTAL_FLOORS = 4;
    static final float FLOOR_OVERLAP = 0.2f; // 20% overlap
    public BarometerManager barometer;
    private GameDatabase database;
    private VisitedFloor currentFloor;
    private Float minPressure; // highest altitude
    private Float pressureRange;
    private final List<Floor> floors = new ArrayList<>(); // from ground floor up

    private Observer<Float> pressureObserver = aFloat -> {
        Timber.v("pressure %f", aFloat);
        if (aFloat == null || floors.isEmpty()) {
            return;
        }
        if (currentFloor != null && floors.get(currentFloor.getFloor()).isOnThisFloor(aFloat)) {
            return; // floor hasn't changed
        }
        for (int i = 0; i < floors.size(); i++) {
            if (floors.get(i).isOnThisFloor(aFloat)) {
                database.floorDao().insertFloor(new VisitedFloor(i));
                break;
            }
        }
    };

    private Observer<Float> groundPressureObserver = aFloat -> {
        if (minPressure == null) {
            return;
        }
        float maxPressure = aFloat;
        Timber.d("pressureRange %f to %f", minPressure, maxPressure);
        if (pressureRange == null) {
            pressureRange = maxPressure - minPressure;
            pressureRange = Math.max(pressureRange, 0.1f);
        }
        floors.clear();
        floors.add(new Floor(0, 1)); // add impossible interval for ground floor
        float pressureChangePerFloor = pressureRange / (TOTAL_FLOORS - 1);
        for (int i = 0; i < TOTAL_FLOORS - 1; i++) {
            floors.add(new Floor(
                    maxPressure - pressureChangePerFloor * i + pressureChangePerFloor * FLOOR_OVERLAP,
                    maxPressure - pressureChangePerFloor * (i + 1) - pressureChangePerFloor * FLOOR_OVERLAP
            ));
        }
        if (currentFloor.getFloor() > 0) { // set to ground floor
            database.floorDao().insertFloor(new VisitedFloor(0));
        }
    };

    public ElevatorViewModel() {
        barometer = BarometerManager.getInstance();
        database = MyApplication.getGameDatabase();
    }

    public void recordMinPressure() {
        minPressure = barometer.getValue();
    }

    public void writePressureToDatabase(LifecycleOwner owner) {
        database.currentFloor().subscribe(floor -> currentFloor = floor);
        barometer.observe(owner, pressureObserver);
        barometer.getGroundPressure().observe(owner, groundPressureObserver);
    }

    static class Floor {
        float maxPressure;
        float minPressure;

        public Floor(float maxPressure, float minPressure) {
            this.maxPressure = maxPressure;
            this.minPressure = minPressure;
        }

        boolean isOnThisFloor(float pressure) {
            return pressure < maxPressure && pressure > minPressure;
        }
    }

}
