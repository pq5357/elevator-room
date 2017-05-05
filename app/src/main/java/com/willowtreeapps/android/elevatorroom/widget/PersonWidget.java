package com.willowtreeapps.android.elevatorroom.widget;

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
    private boolean ready = false; // ready for updates
    private Runnable updateRunnable = () -> {
        if (person == null) {
            return;
        }
        ready = true;
        progressBar.setProgress((int) (person.timeLeft() * 1000));
        if (person.timeLeft() < 0.4) {
            person.gone();
            person.save();
        }
        switch (person.getCurrentState()) {
            case LOBBY:
                updateInLobby();
                break;
            case ELEVATOR_PRE_PRESS:
            case ELEVATOR_POST_PRESS:
                updateInElevator();
                break;
        }
    };

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

    public void init(Context context) {
        inflate(getContext(), R.layout.widget_person, this);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ButterKnife.bind(this, this);
        setVisibility(INVISIBLE);
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
        post(updateRunnable);
    }

    private void updateInLobby() {
        ViewParent parent = getParent();
        if (!(parent instanceof ViewGroup)) {
            return;
        }
        ViewGroup parentView = (ViewGroup) parent;
        float speed = parentView.getMeasuredWidth() / TIME_TO_CROSS; // pixels per ms
        float targetY = (parentView.getMeasuredHeight() - getMeasuredHeight()) / 2.0f;
        setY(targetY);
        if (person.hasReachedGoal()) {
            // walk from elevator doors to stage left
        } else {
            // walk from stage left to elevator doors
            float progress = person.timeInState() / TIME_TO_CROSS;
            progress = Math.min(progress, 1);
            setX(progress * parentView.getMeasuredWidth() - getMeasuredWidth());
        }
    }

    private void updateInElevator() {
        ViewParent parent = getParent();
        if (!(parent instanceof ViewGroup)) {
            return;
        }
        ViewGroup parentView = (ViewGroup) parent;
    }

    public void onlyShowCurrentFloor(int currentFloor) {
        if (!ready) {
            return;
        }
        setVisibility(currentFloor == person.getCurrentFloor() ? VISIBLE : GONE);
    }

}
