package com.willowtreeapps.android.elevatorroom.lobby;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.text.format.DateUtils;

import com.willowtreeapps.android.elevatorroom.MyApplication;
import com.willowtreeapps.android.elevatorroom.RxUtil;
import com.willowtreeapps.android.elevatorroom.dagger.AppComponent;
import com.willowtreeapps.android.elevatorroom.elevator.ElevatorViewModel;
import com.willowtreeapps.android.elevatorroom.livedata.LiveDataRx;
import com.willowtreeapps.android.elevatorroom.persistence.GameDatabase;
import com.willowtreeapps.android.elevatorroom.persistence.Person;
import com.willowtreeapps.android.elevatorroom.persistence.VisitedFloor;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.internal.operators.flowable.FlowableOnBackpressureDrop;

import static com.willowtreeapps.android.elevatorroom.GameStateManager.FRAME_LENGTH;

public class LobbyViewModel extends AndroidViewModel {

    @Inject GameDatabase database;
    public final LiveData<Long> gameLoopTimer;
    public final LiveData<Long> newPersonTimer;
    public final LiveData<VisitedFloor> currentFloor;

    public LobbyViewModel(Application application) {
        this(application, MyApplication.getAppComponent(application.getApplicationContext()));
    }

    public LobbyViewModel(Application application, AppComponent appComponent) {
        super(application);
        appComponent.inject(this);
        currentFloor = database.floorDao().currentFloor();
        gameLoopTimer = LiveDataRx.fromEternalPublisher(FlowableOnBackpressureDrop.interval(FRAME_LENGTH, TimeUnit.MILLISECONDS));
        newPersonTimer = LiveDataRx.fromEternalPublisher(FlowableOnBackpressureDrop.interval(10, TimeUnit.SECONDS));
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

    public void generateRandomPerson() {
        int goal = (int) (Math.random() * ElevatorViewModel.TOTAL_FLOORS);
        int start = (int) (goal + Math.random() * (ElevatorViewModel.TOTAL_FLOORS - 1) + 1) % ElevatorViewModel.TOTAL_FLOORS;
        final Person person = new Person(
                System.currentTimeMillis() + DateUtils.SECOND_IN_MILLIS * 15,
                goal, start
        );
        RxUtil.runInBg(() -> database.personDao().newPerson(person));
    }

}
