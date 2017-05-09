package com.willowtreeapps.android.elevatorroom.persistence;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * Created by willowtree on 4/21/17.
 */
@Dao
public interface FloorDao {

    @Query("SELECT * FROM " + VisitedFloor.TABLE)
    List<VisitedFloor> loadAllPastFloors();

    @Query("SELECT * FROM " + VisitedFloor.TABLE + " ORDER BY id DESC LIMIT 1")
    LiveData<VisitedFloor> currentFloor();

    @Query("SELECT * FROM " + VisitedFloor.TABLE + " ORDER BY id DESC LIMIT 1")
    VisitedFloor loadCurrentFloor();

    @Insert
    void insertFloor(VisitedFloor floor);

    @Query("DELETE FROM " + VisitedFloor.TABLE)
    void deleteAllFloors();
}
