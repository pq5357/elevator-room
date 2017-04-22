package com.willowtreeapps.android.elevatorroom;

import com.android.support.lifecycle.ViewModel;
import com.android.support.room.Room;
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
