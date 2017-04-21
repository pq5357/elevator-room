package com.willowtreeapps.android.elevatorroom.persistence;

import com.android.support.room.Entity;
import com.android.support.room.PrimaryKey;

import static com.willowtreeapps.android.elevatorroom.persistence.VisitedFloor.TABLE;

/**
 * Created by willowtree on 4/21/17.
 */
@Entity(tableName = TABLE)
public class VisitedFloor {
    public static final String TABLE = "VisitedFloor";

    @PrimaryKey
    private int id;
    private int floor;

    public VisitedFloor(int floor) {
        this.floor = floor;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFloor() {
        return floor;
    }
}
