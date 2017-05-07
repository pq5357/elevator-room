package com.willowtreeapps.android.elevatorroom;

import android.app.Activity;
import android.arch.lifecycle.LiveData;

import com.willowtreeapps.android.elevatorroom.persistence.Person;
import com.willowtreeapps.android.elevatorroom.widget.PersonWidget;

import java.util.List;

/**
 * Created by willowtree on 5/6/17.
 */

public class ElevatorView extends BaseGameView {

    private final LiveData<Integer> currentFloor;

    public ElevatorView(Activity activity, LiveData<Integer> currentFloor) {
        super(activity);
        this.currentFloor = currentFloor;
    }

    @Override
    protected PersonWidget createNewPersonWidget() {
        return new PersonWidget(context).init(false,
                gameStateManager.multiWindowDividerSize, currentFloor, doorsOpen);
    }

    @Override
    public void updateForPeople(List<Person> people) {
        for (int i = people.size() - 1; i >= 0; i--) {
            Person person = people.get(i);
            // TODO update indicator lights to reflect requested floors
            if (!person.isInElevator()) {
                people.remove(i);
            }
        }
        super.updateForPeople(people);
    }
}
