package com.willowtreeapps.android.elevatorroom;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.arch.lifecycle.LifecycleActivity;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.willowtreeapps.android.elevatorroom.GameStateManager.GameState.CALIBRATION;
import static com.willowtreeapps.android.elevatorroom.GameStateManager.GameState.PLAYING;

public class ElevatorActivity extends LifecycleActivity {

    private ElevatorViewModel viewModel;
    private ElevatorView view;
    private Unbinder unbinder;
    private GameStateManager gameStateManager;

    @BindView(android.R.id.content) View rootView;
    @BindView(R.id.textview) TextView messageText;
    @BindView(R.id.btn_start) Button btnStart;
    @BindView(R.id.messaging) ViewGroup messaging;
    @BindView(R.id.door_upper) View doorUpper;
    @BindView(R.id.pressure_indicator) ProgressBar pressureIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elevator);
        gameStateManager = MyApplication.getGameStateManager();
        unbinder = ButterKnife.bind(this);
        gameStateManager.multiWindowDividerSize.setRightView(this, rootView);
        gameStateManager.gameState.observe(this, this::onApplyState);
        gameStateManager.doorsOpen.observe(this, this::updateDoors);

        viewModel = ViewModelProviders.of(this).get(ElevatorViewModel.class);
        view = new ElevatorView(this, viewModel.currentFloorLive);
        viewModel.writePressureToDatabase(this);
        viewModel.gameLoopTimer.observe(this, view::updateWidgets);
        viewModel.activePeople().observe(this, view::updateForPeople);
        btnStart.setEnabled(false); // disable until pressure reading starts
        viewModel.barometer.observe(this, aFloat -> btnStart.setEnabled(true));
        viewModel.barometer.getGroundPressure().observe(this, aFloat -> {
            if (gameStateManager.gameState.getValue() == CALIBRATION) {
                gameStateManager.gameState.setValue(PLAYING);
            }
        });
        viewModel.getCurrentPressurePercentage().observe(this, integer -> {
            pressureIndicator.setProgress(integer, true);
        });
    }



    private void updateDoors(boolean open) {
        float doorMovement = getResources().getDimension(R.dimen.elevator_door_movement);
        doorUpper.animate().translationY(open ? -doorMovement : 0)
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

    @OnClick(R.id.btn_start)
    public void clickStartGame() {
        viewModel.recordMinPressure();
        gameStateManager.doorsOpen.setValue(false);
        switch (gameStateManager.gameState.getValue()) {
            case INIT:
                gameStateManager.gameState.setValue(CALIBRATION);
                break;
        }
    }

    @OnClick(android.R.id.content)
    protected void tappedOnElevator() {
        gameStateManager.doorsOpen.setValue(false);
    }

    private void onApplyState(GameStateManager.GameState currentState) {
        switch (currentState) {
            case INIT:
                messaging.setVisibility(View.VISIBLE);
                messageText.setVisibility(View.GONE);
                btnStart.setText(R.string.start_the_day);
                btnStart.setVisibility(View.VISIBLE);
                break;
            case CALIBRATION:
                messaging.setVisibility(View.VISIBLE);
                btnStart.setVisibility(View.GONE);
                messageText.setVisibility(View.VISIBLE);
                messageText.setText(R.string.start_game_message);
                break;
            case PLAYING:
                messaging.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (unbinder != null) {
            unbinder.unbind();
        }
    }

}
