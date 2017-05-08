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

import com.willowtreeapps.android.elevatorroom.persistence.VisitedFloor;

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
    private LobbyView view;
    private GameStateManager gameStateManager;
    private Disposable intervalDisposable = Disposables.disposed();

    @BindView(android.R.id.content) View rootView;
    @BindView(R.id.textview) TextView label;
    @BindView(R.id.playfield) ViewGroup playfield;
    @BindView(R.id.door_upper) View doorUpper;
    @BindView(R.id.door_lower) View doorLower;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        gameStateManager = MyApplication.getGameStateManager();
        unbinder = ButterKnife.bind(this);
        gameStateManager.multiWindowDividerSize.setLeftView(this, rootView);
        gameStateManager.gameState.observe(this, this::onApplyState);
        gameStateManager.doorsOpen.observe(this, this::updateDoors);

        view = new LobbyView(this);
        viewModel = ViewModelProviders.of(this).get(LobbyViewModel.class);
        viewModel.gameLoopTimer.observe(this, view::updateWidgets);
        viewModel.currentFloor.observe(this, this::updateForFloor);
        viewModel.activePeople().observe(this, view::updateForPeople);
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
        doorLower.animate().translationY(open ? doorMovement : 0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.doorsOpen.setValue(open);
                    }
                });
        if (!open) { // when closing, set state to closed immediately
            view.doorsOpen.setValue(false);
        }
    }

    private final int fadeDuration = 200;
    private AnimatorListenerAdapter fadeOutListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            view.floorToRender.setValue(viewModel.currentFloor.getValue().getFloor());
            if (playfield == null) {
                return;
            }
            playfield.animate().alpha(1).setDuration(fadeDuration).setListener(null);
        }
    };

    private void updateForFloor(VisitedFloor visitedFloor) {
        label.setText(getString(R.string.floor_n, visitedFloor.getFloorString()));
        if (view.floorToRender.getValue() == null
                || view.floorToRender.getValue() == visitedFloor.getFloor()) {
            view.floorToRender.setValue(visitedFloor.getFloor());
            return;
        }
        playfield.animate().alpha(0).setDuration(fadeDuration).setListener(fadeOutListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // TODO migrate this to a LiveData in the view model
        intervalDisposable.dispose();
        intervalDisposable = Observable.interval(3, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.computation())
                .subscribe(aLong -> {
                    if (gameStateManager.gameState.getValue() == GameStateManager.GameState.PLAYING) {
                        viewModel.fakeNew();
                    }
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
