package com.willowtreeapps.android.elevatorroom.persistence;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverter;
import android.arch.persistence.room.TypeConverters;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.text.TextUtils;

import com.willowtreeapps.android.elevatorroom.MyApplication;

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
        LOBBY, ELEVATOR_PRE_PRESS, ELEVATOR_POST_PRESS
    }

    @PrimaryKey(autoGenerate = true)
    private long id;
    private State currentState;
    private int style; // appearance of this person
    private boolean gone; // person has exited the elevator
    private long birth; // timestamp when created
    private long deadline; // timestamp when need to reach goal
    private long updated; // timestamp when currentState is updated
    private int goal; // target floor
    private int currentFloor; // current location when

    public Person(long deadline, int goal, int currentFloor) {
        this.birth = System.currentTimeMillis();
        this.deadline = deadline;
        this.goal = goal;
        this.currentFloor = currentFloor;
        setCurrentState(State.LOBBY);
        gone = false;
        style = randomStyle();
    }

    private int randomStyle() {
        // TODO currently a random color, should be a random drawable
        Random rand = new Random();
        return Color.rgb(rand.nextInt(200) + 40, rand.nextInt(200) + 40, rand.nextInt(200) + 40);
    }

    /**
     * tell person to disappear
     */
    public void gone() {
        gone = true;
    }

    public boolean isInElevator() {
        return currentState != State.LOBBY;
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

    protected void setId(long id) {
        this.id = id;
    }

    public State getCurrentState() {
        return currentState;
    }

    public void setCurrentState(State currentState) {
        if (this.currentState != currentState) {
            this.currentState = currentState;
            updated = System.currentTimeMillis();
        }
    }

    protected boolean isGone() {
        return gone;
    }

    protected void setGone(boolean gone) {
        this.gone = gone;
    }

    protected long getBirth() {
        return birth;
    }

    protected void setBirth(long birth) {
        this.birth = birth;
    }

    protected long getDeadline() {
        return deadline;
    }

    protected void setDeadline(long deadline) {
        this.deadline = deadline;
    }

    protected int getGoal() {
        return goal;
    }

    protected void setGoal(int goal) {
        this.goal = goal;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public void setCurrentFloor(int currentFloor) {
        this.currentFloor = currentFloor;
    }

    protected int getStyle() {
        return style;
    }

    protected void setStyle(int style) {
        this.style = style;
    }

    public long getUpdated() {
        return updated;
    }

    protected void setUpdated(long updated) {
        this.updated = updated;
    }

    public void save() {
        MyApplication.getGameDatabase().personDao().updatePerson(this);
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
