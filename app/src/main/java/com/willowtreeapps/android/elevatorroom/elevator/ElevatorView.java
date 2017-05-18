package com.willowtreeapps.android.elevatorroom.elevator;

import android.arch.lifecycle.LifecycleActivity;
import android.arch.lifecycle.LiveData;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.willowtreeapps.android.elevatorroom.BaseGameView;
import com.willowtreeapps.android.elevatorroom.R;
import com.willowtreeapps.android.elevatorroom.dagger.AppComponent;
import com.willowtreeapps.android.elevatorroom.livedata.LiveDataRx;
import com.willowtreeapps.android.elevatorroom.persistence.Person;
import com.willowtreeapps.android.elevatorroom.persistence.VisitedFloor;
import com.willowtreeapps.android.elevatorroom.widget.PersonWidget;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import io.reactivex.internal.operators.flowable.FlowableOnBackpressureDrop;

/**
 * Created by willowtree on 5/6/17.
 */

public class ElevatorView extends BaseGameView {

    private final LiveData<Integer> currentFloor;
    private final LiveData<Long> floorBlinker;

    @BindView(R.id.toolbar) Toolbar toolbar;
    private final List<TextView> floorIndicators = new ArrayList<>();
    private final Set<Integer> requestedFloors = new TreeSet<>();

    public ElevatorView(AppComponent appComponent, LifecycleActivity activity, LiveData<Integer> currentFloor) {
        super(appComponent, activity);
        this.currentFloor = currentFloor;
        setupViews();
        this.currentFloor.observe(activity, this::setCurrentFloor);
        floorBlinker = LiveDataRx.fromEternalPublisher(FlowableOnBackpressureDrop.interval(500, TimeUnit.MILLISECONDS));
        floorBlinker.observe(activity, this::blinkRequestedFloors);
    }

    private void setupViews() {
        toolbar.removeAllViews();
        floorIndicators.clear();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        for (int i = 0; i < ElevatorViewModel.TOTAL_FLOORS; i++) {
            VisitedFloor floor = new VisitedFloor(i);
            TextView textView = (TextView) layoutInflater.inflate(R.layout.elevator_indicator_textview, toolbar, false);
            textView.setText(floor.getFloorString());
            floorIndicators.add(textView);
            toolbar.addView(textView);
        }
    }

    @Override
    protected PersonWidget createNewPersonWidget() {
        return new PersonWidget(context).init(appComponent, false,
                gameStateManager.multiWindowDividerSize, currentFloor, doorsOpen);
    }

    @Override
    public void updateForPeople(List<Person> people) {
        requestedFloors.clear();
        for (int i = people.size() - 1; i >= 0; i--) {
            Person person = people.get(i);
            switch (person.getCurrentState()) {
                case LOBBY_WAITING:
                    requestedFloors.add(person.getCurrentFloor());
                    break;
                case ELEVATOR_POST_PRESS:
                case ELEVATOR_GOAL_FLOOR:
                    requestedFloors.add(person.getGoal());
                    break;
            }
            if (!person.isInElevator()) {
                people.remove(i);
            }
        }
        if (currentFloor.getValue() != null) {
            setCurrentFloor(currentFloor.getValue());
        }
        super.updateForPeople(people);
    }

    private void setCurrentFloor(int floor) {
        for (int i = 0; i < floorIndicators.size(); i++) {
            if (i == floor) {
                floorIndicators.get(i).setBackgroundResource(R.drawable.lighted_circle);
            } else {
                floorIndicators.get(i).setBackgroundResource(R.drawable.dim_circle);
            }
        }
        if (floorBlinker.getValue() != null) {
            blinkRequestedFloors(floorBlinker.getValue());
        }
    }

    private void blinkRequestedFloors(long t) {
        for (int i = 0; i < floorIndicators.size(); i++) {
            if (currentFloor.getValue() != null && i == currentFloor.getValue()) {
                continue; // don't modify current floor
            }
            if (requestedFloors.contains(i)) {
                floorIndicators.get(i).setBackgroundResource(t % 2 == 0 ? R.drawable.lighted_circle : R.drawable.dim_circle);
            } else {
                floorIndicators.get(i).setBackgroundResource(R.drawable.dim_circle);
            }
        }
    }

}
