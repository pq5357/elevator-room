package com.willowtreeapps.android.elevatorroom;

import android.arch.lifecycle.ViewModel;
import android.arch.persistence.room.Room;
import com.willowtreeapps.android.elevatorroom.persistence.GameDatabase;
import com.willowtreeapps.android.elevatorroom.persistence.VisitedFloor;


public class LobbyViewModel extends ViewModel {

    private GameDatabase database;

    public LobbyViewModel() {
        database = Room.databaseBuilder(MyApplication.getContext(),
                GameDatabase.class, "game.db")
                .build();
    }

    public VisitedFloor getCurrentFloor() {
        return database.floorDao().loadCurrentFloor();
    }

}
