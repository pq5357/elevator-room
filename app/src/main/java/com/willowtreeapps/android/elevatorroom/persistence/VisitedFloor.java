package com.willowtreeapps.android.elevatorroom.persistence;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import static com.willowtreeapps.android.elevatorroom.persistence.VisitedFloor.TABLE;

/**
 * Created by willowtree on 4/21/17.
 */
@Entity(tableName = TABLE)
public class VisitedFloor {
    public static final String TABLE = "VisitedFloor";

    @PrimaryKey(autoGenerate = true)
    private long id;
    private int floor;

    public VisitedFloor(int floor) {
        this.floor = floor;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getFloor() {
        return floor;
    }

    public String getFloorString() {
        if (floor == 0) {
            return "G";
        }
        return Integer.toString(floor);
    }
}
