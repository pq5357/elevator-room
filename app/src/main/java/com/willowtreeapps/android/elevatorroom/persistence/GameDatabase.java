package com.willowtreeapps.android.elevatorroom.persistence;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.RxRoom;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by willowtree on 4/21/17.
 */

@Database(
        entities = {VisitedFloor.class, Person.class},
        version = 1
)
public abstract class GameDatabase extends RoomDatabase {

    public abstract FloorDao floorDao();

    public abstract PersonDao personDao();

    public Flowable<VisitedFloor> currentFloor() {
        return RxRoom.createFlowable(this, VisitedFloor.TABLE)
                .map(o -> {
                    VisitedFloor currentFloor = floorDao().loadCurrentFloor();
                    if (currentFloor == null) {
                        currentFloor = new VisitedFloor(0);
                    }
                    return currentFloor;
                }).subscribeOn(Schedulers.io());
    }

    public Flowable<List<Person>> activePeople() {
        return RxRoom.createFlowable(this, Person.TABLE)
                .map(o -> personDao().loadActivePeople())
                .subscribeOn(Schedulers.io());
    }

}
