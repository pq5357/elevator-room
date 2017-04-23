package com.willowtreeapps.android.elevatorroom;

import android.arch.lifecycle.ViewModel;

import com.willowtreeapps.android.elevatorroom.persistence.GameDatabase;
import com.willowtreeapps.android.elevatorroom.persistence.VisitedFloor;

import io.reactivex.Flowable;


public class LobbyViewModel extends ViewModel {

    private GameDatabase database;

    public LobbyViewModel() {
        database = MyApplication.getGameDatabase();
    }

    public Flowable<VisitedFloor> currentFloor() {
        return database.currentFloor();
    }

    public void fakeNew() {
        database.floorDao().insertFloor(new VisitedFloor((int) (Math.random() * 1000)));
    }
}
