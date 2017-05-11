package com.willowtreeapps.android.elevatorroom.persistence;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverter;
import android.arch.persistence.room.TypeConverters;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.text.TextUtils;

import com.willowtreeapps.android.elevatorroom.RxUtil;

import java.util.Random;

import static com.willowtreeapps.android.elevatorroom.persistence.Person.TABLE;

/**
 * Created by willowtree on 5/3/17.
 */

@Entity(tableName = TABLE)
@TypeConverters(Person.StateConverter.class)
public class Person {
    public static final String TABLE = "Person";
    public static final int SCORE_FACTOR = 100; // milliseconds per point

    public enum State {
        LOBBY, // in the lobby
        LOBBY_WAITING, // in the lobby, has pressed elevator button
        IN_DOOR, // walking from lobby into elevator or vice versa
        ELEVATOR_PRE_PRESS, // just entered elevator, walking towards buttons
        ELEVATOR_POST_PRESS, // just pressed floor button, walking towards center of elevator
        ELEVATOR_GOAL_FLOOR // elevator has arrived on desired floor, walking towards elevator doors and waiting for doors to open
    }

    @PrimaryKey(autoGenerate = true)
    private final long id;
    private State currentState;
    private int style; // appearance of this person
    private boolean gone; // person has exited the elevator
    private final long birth; // timestamp when created
    private final long deadline; // timestamp when need to reach goal
    private long updated; // timestamp when currentState is updated
    private final int goal; // target floor
    private int currentFloor; // current location when

    @Ignore private boolean dirty;

    protected Person(long id, State currentState, int style, boolean gone, long birth, long deadline, long updated, int goal, int currentFloor) {
        this.id = id;
        this.currentState = currentState;
        this.style = style;
        this.gone = gone;
        this.birth = birth;
        this.deadline = deadline;
        this.updated = updated;
        this.goal = goal;
        this.currentFloor = currentFloor;
        dirty = false;
    }

    @Ignore
    public Person(long deadline, int goal, int currentFloor) {
        id = 0;
        this.birth = System.currentTimeMillis();
        this.deadline = deadline;
        this.goal = goal;
        setCurrentFloor(currentFloor);
        setCurrentState(State.LOBBY);
        gone = false;
        style = randomStyle();
        dirty = false;
    }

    private int randomStyle() {
        // TODO currently a random color, should be a random drawable
        Random rand = new Random();
        return Color.rgb(rand.nextInt(200) + 40, rand.nextInt(200) + 40, rand.nextInt(200) + 40);
    }

    private void setDirty() {
        dirty = true;
    }

    /**
     * tell person to disappear
     */
    public void gone() {
        if (!gone) {
            gone = true;
            setDirty();
        }
    }

    public boolean isInLobby() {
        return currentState == State.LOBBY
                || currentState == State.LOBBY_WAITING
                || currentState == State.IN_DOOR;
    }

    public boolean isInElevator() {
        return currentState == State.ELEVATOR_PRE_PRESS
                || currentState == State.ELEVATOR_POST_PRESS
                || currentState == State.ELEVATOR_GOAL_FLOOR
                || currentState == State.IN_DOOR;
    }

    public boolean hasReachedGoal() {
        return currentFloor == goal;
    }

    /**
     * returns time left until deadline as a proportion of total time
     */
    public float timeLeft() {
        if (System.currentTimeMillis() > deadline) {
            return 0;
        }
        float timeLeft = deadline - System.currentTimeMillis();
        float lifeTime = deadline - birth;
        return timeLeft / lifeTime;
    }

    /**
     * how long I have been in my current state
     */
    public long timeInState() {
        return System.currentTimeMillis() - getUpdated();
    }

    public Drawable getAppearance() {
        ShapeDrawable shapeDrawable = new ShapeDrawable(new OvalShape());
        shapeDrawable.getPaint().setColor(getStyle());
        return shapeDrawable;
    }

    /**
     * returns how much this person would add to the player's score right now
     */
    public int getScore() {
        if (System.currentTimeMillis() > deadline) {
            return (int) ((birth - deadline) / SCORE_FACTOR);
        }
        return (int) ((deadline - System.currentTimeMillis()) / SCORE_FACTOR + 1);
    }

    public long getId() {
        return id;
    }

    public State getCurrentState() {
        return currentState;
    }

    public void setCurrentState(State currentState) {
        if (this.currentState != currentState) {
            this.currentState = currentState;
            updated = System.currentTimeMillis();
            setDirty();
        }
    }

    protected boolean isGone() {
        return gone;
    }

    protected long getBirth() {
        return birth;
    }

    protected long getDeadline() {
        return deadline;
    }

    public int getGoal() {
        return goal;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public void setCurrentFloor(int currentFloor) {
        if (this.currentFloor != currentFloor) {
            this.currentFloor = currentFloor;
            setDirty();
        }
    }

    protected int getStyle() {
        return style;
    }

    public long getUpdated() {
        return updated;
    }

    public void save(final GameDatabase database) {
        if (!dirty) {
            return;
        }
        dirty = false;
        RxUtil.runInBg(() -> database.personDao().updatePerson(this));
    }

    public static class StateConverter {
        @TypeConverter
        public static State toState(String state) {
            if (TextUtils.isEmpty(state)) {
                return State.LOBBY;
            }
            return State.valueOf(state);
        }

        @TypeConverter
        public static String toString(State state) {
            if (state == null) {
                return null;
            }
            return state.toString();
        }
    }

}
