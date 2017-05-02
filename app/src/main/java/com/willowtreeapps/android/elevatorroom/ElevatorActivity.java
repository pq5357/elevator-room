package com.willowtreeapps.android.elevatorroom;

import android.arch.lifecycle.LifecycleActivity;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
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
import static com.willowtreeapps.android.elevatorroom.GameStateManager.GameState.CAN_PLAY;

public class ElevatorActivity extends LifecycleActivity implements GameStateManager.StateChangeListener {

    ElevatorViewModel viewModel;
    Unbinder unbinder;
    GameStateManager gameStateManager;
    private Disposable floorDisposable = Disposables.disposed();

    @BindView(R.id.textview)
    TextView messageText;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.btn_start) Button btnStart;
    @BindView(R.id.pressure_indicator) ProgressBar pressureIndicator;
    private final List<TextView> floorIndicators = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elevator);
        gameStateManager = new GameStateManager(this, this);
        unbinder = ButterKnife.bind(this);
        setupViews();

        viewModel = ViewModelProviders.of(this).get(ElevatorViewModel.class);
        viewModel.writePressureToDatabase(this);
        viewModel.barometer.getGroundPressure().observe(this, aFloat -> {
            if (gameStateManager.getGameState() == CALIBRATION) {
                gameStateManager.setGameState(CAN_PLAY);
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

    @OnClick(R.id.btn_start)
    public void clickStartGame() {
        viewModel.recordMinPressure();
        switch (gameStateManager.getGameState()) {
            case INIT:
                gameStateManager.setGameState(CALIBRATION);
                break;
        }
    }

    @Override
    public void onStateChanged(GameStateManager.GameState newState) {
        switch (newState) {
            case PLAYING: // started playing
                // TODO since we don't care about state transitions, can convert GameStateManager to LiveData
                break;
        }
    }

    @Override
    public void onApplyState(GameStateManager.GameState currentState) {
        switch (currentState) {
            case INIT:
                messageText.setVisibility(View.GONE);
                btnStart.setText(R.string.start_the_day);
                btnStart.setVisibility(View.VISIBLE);
                break;
            case CALIBRATION:
                btnStart.setVisibility(View.GONE);
                messageText.setVisibility(View.VISIBLE);
                messageText.setText(R.string.start_game_message);
                break;
            case CAN_PLAY:
                onApplyState(CALIBRATION);
                messageText.setText(messageText.getText() + "\n" +
                        getString(R.string.tap_lobby_to_start));
                break;
            case PLAYING:
                btnStart.setVisibility(View.GONE);
                messageText.setVisibility(View.VISIBLE);
                messageText.setText("game started!");
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
