package com.willowtreeapps.android.elevatorroom;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.LiveDataReactiveStreams;
import android.arch.lifecycle.ViewModel;
import android.text.format.DateUtils;

import com.willowtreeapps.android.elevatorroom.persistence.GameDatabase;
import com.willowtreeapps.android.elevatorroom.persistence.Person;
import com.willowtreeapps.android.elevatorroom.persistence.VisitedFloor;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.internal.operators.flowable.FlowableOnBackpressureDrop;


public class LobbyViewModel extends ViewModel {

    private GameDatabase database;
    private final LiveData<Long> timer = LiveDataReactiveStreams.fromPublisher(FlowableOnBackpressureDrop.interval(12, TimeUnit.MILLISECONDS));

    public LobbyViewModel() {
        database = MyApplication.getGameDatabase();
    }

    public Flowable<VisitedFloor> currentFloor() {
        return database.currentFloor();
    }

    /**
     * returns people currently in the lobby
     */
    public LiveData<List<Person>> activePeople() {
        return LiveDataReactiveStreams.fromPublisher(database.activePeople()
                .flatMap(persons -> Flowable.fromIterable(persons)
                        .filter(person -> person.getCurrentState() == Person.State.LOBBY)
                        .toList().toFlowable()
                )
        );
    }

    public LiveData<Long> getUpdateTimer() {
        return timer;
    }

    public void fakeNew() {
        database.personDao().newPerson(new Person(
                System.currentTimeMillis() + DateUtils.SECOND_IN_MILLIS * 10,
                2, 0
        ));
    }

}
