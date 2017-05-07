package com.willowtreeapps.android.elevatorroom;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.arch.lifecycle.LifecycleActivity;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.willowtreeapps.android.elevatorroom.persistence.VisitedFloor;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;

import static com.willowtreeapps.android.elevatorroom.GameStateManager.GameState.CALIBRATION;
import static com.willowtreeapps.android.elevatorroom.GameStateManager.GameState.PLAYING;

public class ElevatorActivity extends LifecycleActivity {

    private ElevatorViewModel viewModel;
    private ElevatorView view;
    private Unbinder unbinder;
    private GameStateManager gameStateManager;
    private Disposable floorDisposable = Disposables.disposed();

    @BindView(android.R.id.content) View rootView;
    @BindView(R.id.textview) TextView messageText;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.btn_start) Button btnStart;
    @BindView(R.id.messaging) ViewGroup messaging;
    @BindView(R.id.door_upper) View doorUpper;
    @BindView(R.id.pressure_indicator) ProgressBar pressureIndicator;
    private final List<TextView> floorIndicators = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elevator);
        gameStateManager = MyApplication.getGameStateManager();
        unbinder = ButterKnife.bind(this);
        setupViews();
        gameStateManager.gameState.observe(this, this::onApplyState);
        gameStateManager.doorsOpen.observe(this, this::updateDoors);

        view = new ElevatorView(this);
        viewModel = ViewModelProviders.of(this).get(ElevatorViewModel.class);
        viewModel.writePressureToDatabase(this);
        viewModel.gameLoopTimer.observe(this, view::updateWidgets);
        viewModel.activePeople().observe(this, view::updateForPeople);
        viewModel.barometer.getGroundPressure().observe(this, aFloat -> {
            if (gameStateManager.gameState.getValue() == CALIBRATION) {
                gameStateManager.gameState.setValue(PLAYING);
            }
        });
        viewModel.getCurrentPressurePercentage().observe(this, integer -> {
            pressureIndicator.setProgress(integer, true);
        });
        floorDisposable.dispose();
        floorDisposable = viewModel.currentFloor()
                .map(VisitedFloor::getFloor)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setCurrentFloor);
        gameStateManager.multiWindowDividerSize.setRightView(this, rootView);
    }

    private void setupViews() {
        toolbar.removeAllViews();
        floorIndicators.clear();
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        for (int i = 0; i < ElevatorViewModel.TOTAL_FLOORS; i++) {
            VisitedFloor floor = new VisitedFloor(i);
            TextView textView = (TextView) layoutInflater.inflate(R.layout.elevator_indicator_textview, toolbar, false);
            textView.setText(floor.getFloorString());
            floorIndicators.add(textView);
            toolbar.addView(textView);
        }
    }

    private void setCurrentFloor(int floor) {
        for (int i = 0; i < floorIndicators.size(); i++) {
            if (i == floor) {
                floorIndicators.get(i).setBackgroundResource(R.drawable.lighted_circle);
            } else {
                floorIndicators.get(i).setBackgroundResource(R.drawable.dim_circle);
            }
        }
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
        floorDisposable.dispose();
    }

}
