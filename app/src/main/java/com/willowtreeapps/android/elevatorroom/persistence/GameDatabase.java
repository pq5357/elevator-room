package com.willowtreeapps.android.elevatorroom.persistence;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

/**
 * Created by willowtree on 4/21/17.
 */

@Database(entities = VisitedFloor.class, version = 1)
public abstract class GameDatabase extends RoomDatabase {

    public abstract FloorDao floorDao();

}
