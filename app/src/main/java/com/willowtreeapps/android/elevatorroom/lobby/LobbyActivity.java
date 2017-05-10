package com.willowtreeapps.android.elevatorroom.lobby;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.arch.lifecycle.LifecycleActivity;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.willowtreeapps.android.elevatorroom.GameStateManager;
import com.willowtreeapps.android.elevatorroom.MyApplication;
import com.willowtreeapps.android.elevatorroom.R;
import com.willowtreeapps.android.elevatorroom.persistence.VisitedFloor;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class LobbyActivity extends LifecycleActivity {

    private Unbinder unbinder;
    private LobbyViewModel viewModel;
    private LobbyView view;
    private GameStateManager gameStateManager;

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
        viewModel.newPersonTimer.observe(this, aLong -> {
            if (gameStateManager.gameState.getValue() == GameStateManager.GameState.PLAYING) {
                viewModel.generatePerson();
            }
        });
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
    protected void onDestroy() {
        super.onDestroy();
        if (unbinder != null) {
            unbinder.unbind();
        }
    }

}
