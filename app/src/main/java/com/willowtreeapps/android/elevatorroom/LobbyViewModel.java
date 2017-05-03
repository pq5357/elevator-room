package com.willowtreeapps.android.elevatorroom;

import android.arch.lifecycle.ViewModel;
import android.text.format.DateUtils;

import com.willowtreeapps.android.elevatorroom.persistence.GameDatabase;
import com.willowtreeapps.android.elevatorroom.persistence.Person;
import com.willowtreeapps.android.elevatorroom.persistence.VisitedFloor;

import java.util.List;

import io.reactivex.Flowable;


public class LobbyViewModel extends ViewModel {

    private GameDatabase database;

    public LobbyViewModel() {
        database = MyApplication.getGameDatabase();
    }

    public Flowable<VisitedFloor> currentFloor() {
        return database.currentFloor();
    }

    public Flowable<List<Person>> activePeople() {
        return database.activePeople();
    }

    public void fakeNew() {
        database.personDao().newPerson(new Person(
                System.currentTimeMillis() + DateUtils.SECOND_IN_MILLIS * 10,
                2, 0
        ));
    }

}
