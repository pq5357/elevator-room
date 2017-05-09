package com.willowtreeapps.android.elevatorroom.lobby;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.text.format.DateUtils;

import com.willowtreeapps.android.elevatorroom.MyApplication;
import com.willowtreeapps.android.elevatorroom.RxUtil;
import com.willowtreeapps.android.elevatorroom.elevator.ElevatorViewModel;
import com.willowtreeapps.android.elevatorroom.livedata.LiveDataRx;
import com.willowtreeapps.android.elevatorroom.persistence.GameDatabase;
import com.willowtreeapps.android.elevatorroom.persistence.Person;
import com.willowtreeapps.android.elevatorroom.persistence.VisitedFloor;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.internal.operators.flowable.FlowableOnBackpressureDrop;

import static com.willowtreeapps.android.elevatorroom.GameStateManager.FRAME_LENGTH;

public class LobbyViewModel extends ViewModel {

    private final GameDatabase database;
    public final LiveData<Long> gameLoopTimer;
    public final LiveData<VisitedFloor> currentFloor;

    public LobbyViewModel() {
        database = MyApplication.getGameDatabase();
        currentFloor = database.floorDao().currentFloor();
        gameLoopTimer = LiveDataRx.fromEternalPublisher(FlowableOnBackpressureDrop.interval(FRAME_LENGTH, TimeUnit.MILLISECONDS));
    }

    /**
     * returns people currently in the lobby
     */
    public LiveData<List<Person>> activePeople() {
        return LiveDataRx.fromEternalPublisher(database.activePeople()
                .flatMap(persons -> Flowable.fromIterable(persons)
                        .filter(Person::isInLobby)
                        .toList().toFlowable()
                )
        );
    }

    public void fakeNew() {
        int goal = (int) (Math.random() * ElevatorViewModel.TOTAL_FLOORS);
        final Person person = new Person(
                System.currentTimeMillis() + DateUtils.SECOND_IN_MILLIS * 15,
                1, 0
        );
        RxUtil.runInBg(() -> database.personDao().newPerson(person));
    }

}
