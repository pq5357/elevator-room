package com.willowtreeapps.android.elevatorroom.widget;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.willowtreeapps.android.elevatorroom.R;
import com.willowtreeapps.android.elevatorroom.persistence.Person;

import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by willowtree on 5/3/17.
 * <p>
 * a custom view for a person that walks around and hits elevator buttons
 */

public class PersonWidget extends FrameLayout {

    public static final float TIME_TO_CROSS = DateUtils.SECOND_IN_MILLIS * 3; // time required to cross the room horizontally

    @BindView(R.id.background) View background;
    @BindView(R.id.progress_bar) ProgressBar progressBar;

    Random random = new Random();
    private Person person;
    private boolean belongsToLobby; // this view is rendered in the Lobby window (as opposed to the Elevator window)
    private LiveData<Integer> currentFloor;
    private LiveData<Boolean> doorsOpen; // are the elevator doors all the way open
    private LiveData<Integer> multiWindowDividerSize;

    public PersonWidget(@NonNull Context context) {
        super(context);
        init(context);
    }

    public PersonWidget(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PersonWidget(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private PersonWidget init(Context context) {
        inflate(context, R.layout.widget_person, this);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ButterKnife.bind(this, this);
        setVisibility(INVISIBLE);
        return this;
    }

    public PersonWidget init(boolean belongsToLobby, LiveData<Integer> multiWindowDividerSize, LiveData<Integer> currentFloor, LiveData<Boolean> doorsOpen) {
        this.belongsToLobby = belongsToLobby;
        this.multiWindowDividerSize = multiWindowDividerSize;
        this.currentFloor = currentFloor;
        this.doorsOpen = doorsOpen;
        return this;
    }

    public boolean setPerson(Person person) {
        if (this.person != null && this.person.getId() != person.getId()) {
            return false;
        }
        this.person = person;
        background.setBackground(person.getAppearance());
        return true;
    }

    public void update() {
        if (person == null || currentFloor.getValue() == null) {
            return;
        }
        progressBar.setProgress((int) (person.timeLeft() * 1000));
        if (belongsToLobby) {
            setVisibility(currentFloor.getValue() == person.getCurrentFloor() ? VISIBLE : GONE);
            if (person.isInLobby()) {
                updateInLobby();
            }
        } else {
            setVisibility(VISIBLE);
            if (person.isInElevator()) {
                updateInElevator();
            }
        }
        person.save();
    }

    private void updateInLobby() {
        if (doorsOpen.getValue() == null) {
            return;
        }
        ViewParent parent = getParent();
        if (!(parent instanceof ViewGroup)) {
            return;
        }
        ViewGroup parentView = (ViewGroup) parent;
        float mySize = getResources().getDimension(R.dimen.person_size);
        float speed = parentView.getMeasuredWidth() / TIME_TO_CROSS; // pixels per ms
        float targetY = (parentView.getMeasuredHeight() - mySize) / 2.0f;
        setY(targetY);
        float progress;
        if (person.hasReachedGoal()) {
            // walk from elevator doors to stage left
            person.gone();
        } else {
            switch (person.getCurrentState()) {
                case LOBBY:
                    // walk from stage left to elevator doors
                    progress = person.timeInState() / TIME_TO_CROSS;
                    progress = Math.min(progress, 1);
                    setX(progress * parentView.getMeasuredWidth() - mySize);
                    // if person has reached doors and they are open, then person enters elevator
                    if (progress == 1 && doorsOpen.getValue()) {
                        person.setCurrentState(Person.State.IN_DOOR);
                    }
                    break;
                case IN_DOOR:
                    // walk from the lobby into the elevator
                    float distance = getTraverseDoorsDistance();
                    float time = distance / speed;
                    progress = person.timeInState() / time;
                    progress = Math.min(progress, 1);
                    setX(parentView.getMeasuredWidth() - mySize + distance * progress);
                    if (progress == 1) {
                        person.setCurrentState(Person.State.ELEVATOR_PRE_PRESS);
                    }
                    break;
            }

        }
    }

    private void updateInElevator() {
        ViewParent parent = getParent();
        if (!(parent instanceof ViewGroup)) {
            return;
        }
        ViewGroup parentView = (ViewGroup) parent;
    }

    /**
     * distance required to traverse from lobby into elevator (or vice versa)
     */
    private float getTraverseDoorsDistance() {
        if (multiWindowDividerSize.getValue() == null) {
            return Float.MAX_VALUE;
        }
        return multiWindowDividerSize.getValue()
                + getResources().getDimension(R.dimen.person_size)
                + getResources().getDimension(R.dimen.lobby_doors_width)
                + getResources().getDimension(R.dimen.elevator_doors_width);
    }

}
