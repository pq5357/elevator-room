package com.willowtreeapps.android.elevatorroom;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
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
    private final PressurePercentage currentPressurePercentage = new PressurePercentage();

    private Observer<Float> pressureObserver = aFloat -> {
        Timber.v("pressure %f", aFloat);
        currentPressurePercentage.setPressure(aFloat);
        if (floors.isEmpty()) {
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

    /**
     * recalibrate every time we get a ground pressure
     */
    private Observer<Float> groundPressureObserver = aFloat -> {
        if (minPressure == null) {
            return;
        }
        float maxPressure = aFloat;
        if (pressureRange == null) {
            pressureRange = maxPressure - minPressure;
            pressureRange = Math.max(pressureRange, 0.3f);
        }
        currentPressurePercentage.setGroundPressure(maxPressure);
        floors.clear();
        float pressureChangePerFloor = pressureRange / (TOTAL_FLOORS - 1);
        floors.add(new Floor(maxPressure + 0.01f, maxPressure - 0.01f));
        for (int i = 0; i < TOTAL_FLOORS - 1; i++) {
            floors.add(new Floor(
                    maxPressure - pressureChangePerFloor * i,
                    maxPressure - pressureChangePerFloor * (i + 1)
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

    public LiveData<Integer> getCurrentPressurePercentage() {
        return currentPressurePercentage;
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
            float range = maxPressure - minPressure;
            return pressure < maxPressure + range * FLOOR_OVERLAP
                    && pressure > minPressure - range * FLOOR_OVERLAP;
        }
    }

    private class PressurePercentage extends LiveData<Integer> {

        private Float maxPressure;

        private void setGroundPressure(float pressure) {
            maxPressure = pressure;
            setValue(1);
        }


        private void setPressure(float pressure) {
            if (maxPressure == null) {
                return;
            }
            setValue((int) ((maxPressure - pressure) * 100 / pressureRange));
        }

    }

}
