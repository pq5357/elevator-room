package com.willowtreeapps.android.elevatorroom;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.arch.lifecycle.LifecycleActivity;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.willowtreeapps.android.elevatorroom.persistence.Person;
import com.willowtreeapps.android.elevatorroom.persistence.VisitedFloor;
import com.willowtreeapps.android.elevatorroom.widget.PersonWidget;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.schedulers.Schedulers;

public class LobbyActivity extends LifecycleActivity {

    private Unbinder unbinder;
    private LobbyViewModel viewModel;
    private GameStateManager gameStateManager;
    private Disposable intervalDisposable = Disposables.disposed();
    private Integer floorToRender;

    @BindView(android.R.id.content) View rootView;
    @BindView(R.id.textview) TextView label;
    @BindView(R.id.playfield) ViewGroup playfield;
    @BindView(R.id.persons_container) ViewGroup personsContainer;
    @BindView(R.id.door_upper) View doorUpper;
    @BindView(R.id.door_lower) View doorLower;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        gameStateManager = MyApplication.getGameStateManager();
        unbinder = ButterKnife.bind(this);
        gameStateManager.gameState.observe(this, this::onApplyState);
        gameStateManager.doorsOpen.observe(this, this::updateDoors);

        viewModel = ViewModelProviders.of(this).get(LobbyViewModel.class);
        viewModel.gameLoopTimer.observe(this, this::updateWidgets);
        viewModel.currentFloor.observe(this, this::updateForFloor);
        viewModel.activePeople().observe(this, this::updateForPeople);
        gameStateManager.multiWindowDividerSize.setLeftView(this, rootView);
    }

    @OnClick(android.R.id.content)
    protected void tappedOnLobby() {
        gameStateManager.doorsOpen.setValue(true);
    }

    private void onApplyState(GameStateManager.GameState currentState) {
        switch (currentState) {
            case INIT:
            case CALIBRATION:
                break;
            case PLAYING:
                break;
        }
    }

    private void updateDoors(boolean open) {
        float doorMovement = getResources().getDimension(R.dimen.elevator_door_movement);
        doorUpper.animate().translationY(open ? -doorMovement : 0);
        doorLower.animate().translationY(open ? doorMovement : 0);
    }

    private final int fadeDuration = 200;
    private AnimatorListenerAdapter fadeOutListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            floorToRender = viewModel.currentFloor.getValue().getFloor();
            playfield.animate().alpha(1).setDuration(fadeDuration)
                    .setListener(null)
                    .start();
        }
    };

    private void updateForFloor(VisitedFloor visitedFloor) {
        label.setText(getString(R.string.floor_n, visitedFloor.getFloorString()));
        if (floorToRender == null) {
            floorToRender = visitedFloor.getFloor();
            return;
        }
        playfield.animate().alpha(0).setDuration(fadeDuration)
                .setListener(fadeOutListener)
                .start();
    }

    private void updateForPeople(List<Person> people) {
        int childCount = personsContainer.getChildCount();
        for (int childIndex = childCount - 1; childIndex >= 0; childIndex--) {
            PersonWidget widget = (PersonWidget) personsContainer.getChildAt(childIndex);
            boolean found = false;
            for (Person person : people) {
                if (widget.setPerson(person)) {
                    people.remove(person); // person has been accounted for
                    found = true;
                    break;
                }
            }
            if (!found) {
                // didn't match an active person, so remove it from playfield
                personsContainer.removeView(widget);
            }
        }
        // add persons that haven't been accounted for
        for (Person person : people) {
            PersonWidget widget = new PersonWidget(this);
            personsContainer.addView(widget);
            widget.setPerson(person);
        }
        updateWidgets(null);
    }

    private void updateWidgets(Object o) {
        for (int i = 0; i < personsContainer.getChildCount(); i++) {
            PersonWidget widget = (PersonWidget) personsContainer.getChildAt(i);
            widget.update();
            if (floorToRender != null) {
                widget.onlyShowCurrentFloor(floorToRender);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        intervalDisposable.dispose();
        intervalDisposable = Observable.interval(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.computation())
                .subscribe(aLong -> {
                    viewModel.fakeNew();
                });
//        person.setOnClickListener(v -> startDrag());
    }

    private boolean startDrag(View person) {
        person.startDragAndDrop(null, getShadow(person), Boolean.TRUE,
                View.DRAG_FLAG_GLOBAL | View.DRAG_FLAG_GLOBAL_URI_READ |
                        View.DRAG_FLAG_GLOBAL_PERSISTABLE_URI_PERMISSION);
        return true;
    }

    private View.DragShadowBuilder getShadow(View v) {
        return new View.DragShadowBuilder() {
            @Override
            public void onProvideShadowMetrics(Point outShadowSize, Point outShadowTouchPoint) {
                outShadowSize.set(v.getWidth() / 8, v.getHeight() / 8);
                outShadowTouchPoint.set(outShadowSize.x / 2, outShadowSize.y / 2);
            }

            @Override
            public void onDrawShadow(Canvas canvas) {
                v.draw(canvas);
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        intervalDisposable.dispose();
        if (unbinder != null) {
            unbinder.unbind();
        }
    }

}
