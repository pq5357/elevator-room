package com.willowtreeapps.android.elevatorroom.persistence;

import com.android.support.room.Database;
import com.android.support.room.RoomDatabase;

/**
 * Created by willowtree on 4/21/17.
 */

@Database(entities = VisitedFloor.class, version = 1)
public abstract class GameDatabase extends RoomDatabase {

    public abstract FloorDao floorDao();

}
