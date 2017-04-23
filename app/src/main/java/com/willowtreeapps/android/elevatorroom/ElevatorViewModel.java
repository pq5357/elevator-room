package com.willowtreeapps.android.elevatorroom;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.Nullable;

import com.willowtreeapps.android.elevatorroom.persistence.GameDatabase;
import com.willowtreeapps.android.elevatorroom.persistence.VisitedFloor;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

public class ElevatorViewModel extends ViewModel {

    public BarometerManager barometer;
    private GameDatabase database;
    private VisitedFloor currentFloor;

    private Observer<Float> pressureObserver = new Observer<Float>() {
        @Override
        public void onChanged(@Nullable Float aFloat) {
            if (aFloat == null) {
                return;
            }
            int floor = (int) (aFloat * 100);
            if (currentFloor != null && floor == currentFloor.getFloor()) {
                return; // floor hasn't changed
            }
            database.floorDao().insertFloor(new VisitedFloor(floor));
        }
    };

    public ElevatorViewModel() {
        barometer = BarometerManager.getInstance();
        database = MyApplication.getGameDatabase();
    }

    public void writePressureToDatabase(LifecycleOwner owner) {
        database.currentFloor().subscribe(new Consumer<VisitedFloor>() {
            @Override
            public void accept(@NonNull VisitedFloor floor) throws Exception {
                currentFloor = floor;
            }
        });
        barometer.observe(owner, pressureObserver);
    }

}
