package com.willowtreeapps.android.elevatorroom.widget;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.willowtreeapps.android.elevatorroom.R;
import com.willowtreeapps.android.elevatorroom.persistence.Person;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.willowtreeapps.android.elevatorroom.persistence.Person.State.ELEVATOR_GOAL_FLOOR;
import static com.willowtreeapps.android.elevatorroom.persistence.Person.State.ELEVATOR_POST_PRESS;
import static com.willowtreeapps.android.elevatorroom.persistence.Person.State.ELEVATOR_PRE_PRESS;
import static com.willowtreeapps.android.elevatorroom.persistence.Person.State.IN_DOOR;

/**
 * Created by willowtree on 5/3/17.
 * <p>
 * a custom view for a person that walks around and hits elevator buttons
 */

public class PersonWidget extends FrameLayout {

    public static final float TIME_TO_STEP = 430; // time required to move 1 person width

    @BindView(R.id.background) View background;
    @BindView(R.id.progress_bar) ProgressBar progressBar;

    private final float preferredX = centeredRandom() * 0.4f + 0.15f; // where this person will choose to stand while waiting in the elevator
    private final float preferredY = centeredRandom() * 0.6f + 0.2f; // somewhere in the center of the available space
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

    private static float centeredRandom() {
        double random = 0.5 +
                Math.min(Math.random(), Math.random()) * (Math.random() > 0.5 ? 0.5 : -0.5);
        return (float) random;
    }

    public void update() {
        if (person == null || currentFloor.getValue() == null || doorsOpen.getValue() == null) {
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
                person.setCurrentFloor(currentFloor.getValue());
                updateInElevator();
            }
        }
        person.save();
    }

    private void updateInLobby() {
        ViewParent parent = getParent();
        if (!(parent instanceof ViewGroup)) {
            return;
        }
        ViewGroup parentView = (ViewGroup) parent;
        Resources res = getResources();
        float mySize = res.getDimension(R.dimen.person_size);
        float speed = mySize / TIME_TO_STEP; // pixels per ms
        float targetY = (parentView.getMeasuredHeight() - mySize) / 2.0f;
        setY(targetY);
        if (person.hasReachedGoal()) {
            switch (person.getCurrentState()) {
                case LOBBY:
                    // walk from elevator doors to stage left
                    person.gone();
                    break;
                case IN_DOOR:
                    // walk from the elevator into the lobby
                    float startAtDoor = parentView.getMeasuredWidth() - mySize - res.getDimension(R.dimen.lobby_doors_width);
                    float traverseDoorsDistance = getTraverseDoorsDistance();
                    float progress = moveX(speed, startAtDoor + traverseDoorsDistance, -traverseDoorsDistance);
                    if (progress == 1) {
                        person.setCurrentState(Person.State.LOBBY);
                    }
                    break;
            }
        } else {
            switch (person.getCurrentState()) {
                case LOBBY:
                    // walk from stage left to elevator doors
                    float roomWidth = parentView.getMeasuredWidth() - res.getDimension(R.dimen.lobby_doors_width);
                    float crossProgress = moveX(speed, -mySize, roomWidth);
                    // if person has reached doors and they are open, then person enters elevator
                    if (crossProgress == 1 && doorsOpen.getValue() && currentFloor.getValue() == person.getCurrentFloor()) {
                        person.setCurrentState(Person.State.IN_DOOR);
                    }
                    break;
                case IN_DOOR:
                    // walk from the lobby into the elevator
                    float startAtDoor = parentView.getMeasuredWidth() - mySize - res.getDimension(R.dimen.lobby_doors_width);
                    moveX(speed, startAtDoor, getTraverseDoorsDistance());
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
        Resources res = getResources();
        float mySize = res.getDimension(R.dimen.person_size);
        float speed = mySize / TIME_TO_STEP; // pixels per ms
        float baseLineX = res.getDimension(R.dimen.elevator_doors_width); // x values start right after elevator doors
        int parentWidth = parentView.getMeasuredWidth();
        int parentHeight = parentView.getMeasuredHeight();
        float centerY = (parentHeight - mySize) / 2.0f;
        switch (person.getCurrentState()) {
            case IN_DOOR:
                setY(centerY);
                float traverseDoorsDistance = getTraverseDoorsDistance();
                if (person.hasReachedGoal()) {
                    // exiting elevator
                    moveX(speed, baseLineX, -traverseDoorsDistance);
                } else {
                    // entering elevator
                    float enterProgress = moveX(speed, baseLineX - traverseDoorsDistance, traverseDoorsDistance);
                    if (enterProgress == 1) {
                        person.setCurrentState(ELEVATOR_PRE_PRESS);
                    }
                }
                break;
            case ELEVATOR_PRE_PRESS:
                setX(baseLineX);
                float distanceToPanel = res.getDimension(R.dimen.elevator_panel_offset) + res.getDimension(R.dimen.elevator_panel_size) / 2;
                float panelProgress = moveY(speed, centerY, distanceToPanel);
                if (panelProgress == 1) {
                    person.setCurrentState(ELEVATOR_POST_PRESS);
                }
                break;
            case ELEVATOR_POST_PRESS:
                float startPanel = centerY + res.getDimension(R.dimen.elevator_panel_offset)
                        + res.getDimension(R.dimen.elevator_panel_size) / 2;
                float progress = moveXY(speed, baseLineX, startPanel,
                        parentWidth * preferredX - baseLineX,
                        parentHeight * preferredY - startPanel);
                if (progress == 1 && person.hasReachedGoal()) {
                    person.setCurrentState(ELEVATOR_GOAL_FLOOR);
                }
                break;
            case ELEVATOR_GOAL_FLOOR:
                float progressTowardDoor = moveXY(speed, parentWidth * preferredX, parentHeight * preferredY,
                        baseLineX - parentWidth * preferredX,
                        centerY - parentHeight * preferredY);
                if (progressTowardDoor == 1 && person.hasReachedGoal() && doorsOpen.getValue()) {
                    person.setCurrentState(IN_DOOR);
                }
                break;
        }
    }

    private float moveX(float speed, float start, float distance) {
        float time = Math.abs(distance) / speed;
        float progress = person.timeInState() / time;
        progress = Math.min(progress, 1);
        setX(start + distance * progress);
        return progress;
    }

    private static final Interpolator INTERPOLATOR = new AccelerateDecelerateInterpolator();
    private static final float EXTRA_TIME = 1.2f; // extra time to account for interpolation

    private float moveY(float speed, float start, float distance) {
        float time = Math.abs(distance) * EXTRA_TIME / speed;
        float progress = person.timeInState() / time;
        progress = Math.min(progress, 1);
        setY(start + distance * INTERPOLATOR.getInterpolation(progress));
        return progress;
    }

    private float moveXY(float speed, float startX, float startY, float dx, float dy) {
        float time = (float) (Math.sqrt(dx * dx + dy * dy) / speed) * EXTRA_TIME;
        float progress = person.timeInState() / time;
        progress = Math.min(progress, 1);
        setX(startX + dx * INTERPOLATOR.getInterpolation(progress));
        setY(startY + dy * INTERPOLATOR.getInterpolation(progress));
        return progress;
    }

    /**
     * distance required to traverse from lobby into elevator (or vice versa)
     */
    private float getTraverseDoorsDistance() {
        if (multiWindowDividerSize.getValue() == null) {
            return Float.MAX_VALUE;
        }
        Resources res = getResources();
        return multiWindowDividerSize.getValue()
                + res.getDimension(R.dimen.person_size)
                + res.getDimension(R.dimen.lobby_doors_width)
                + res.getDimension(R.dimen.elevator_doors_width);
    }

}
