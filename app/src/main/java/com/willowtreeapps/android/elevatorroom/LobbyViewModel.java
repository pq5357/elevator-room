package com.willowtreeapps.android.elevatorroom;

import android.arch.lifecycle.LiveData;
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
    public final LiveData<Long> gameLoopTimer;
    public final LiveData<VisitedFloor> currentFloor;

    public LobbyViewModel() {
        database = MyApplication.getGameDatabase();
        currentFloor = LiveDataRx.fromEternalPublisher(database.currentFloor());
        gameLoopTimer = LiveDataRx.fromEternalPublisher(FlowableOnBackpressureDrop.interval(12, TimeUnit.MILLISECONDS));
    }

    /**
     * returns people currently in the lobby
     */
    public LiveData<List<Person>> activePeople() {
        return LiveDataRx.fromEternalPublisher(database.activePeople()
                .flatMap(persons -> Flowable.fromIterable(persons)
                        .filter(person -> person.getCurrentState() == Person.State.LOBBY)
                        .toList().toFlowable()
                )
        );
    }

    public void fakeNew() {
        int goal = (int) (Math.random() * ElevatorViewModel.TOTAL_FLOORS);
        database.personDao().newPerson(new Person(
                System.currentTimeMillis() + DateUtils.SECOND_IN_MILLIS * 10,
                goal, (goal + 2) % ElevatorViewModel.TOTAL_FLOORS
        ));
    }

}
