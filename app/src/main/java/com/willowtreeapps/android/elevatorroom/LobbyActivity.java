package com.willowtreeapps.android.elevatorroom;

import android.arch.lifecycle.LifecycleActivity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.willowtreeapps.android.elevatorroom.persistence.VisitedFloor;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.functions.Consumer;

import static com.willowtreeapps.android.elevatorroom.R.id.textview;

public class LobbyActivity extends LifecycleActivity {

    private Unbinder unbinder;
    private Disposable floorDisposable = Disposables.disposed();
    private LobbyViewModel viewModel;

    @BindView(textview) TextView label;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        unbinder = ButterKnife.bind(this);
        viewModel = ViewModelProviders.of(this).get(LobbyViewModel.class);
        floorDisposable.dispose();
        floorDisposable = viewModel.currentFloor()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<VisitedFloor>() {
                    @Override
                    public void accept(@NonNull VisitedFloor floor) throws Exception {
                        label.setText(getString(R.string.floor_n, floor.getFloor()));
                    }
                });
    }

    @OnClick(R.id.btn_launch)
    public void launchElevator() {
        Intent intent = new Intent(this, ElevatorActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT); //Launch in adjacent MultiWindow
        startActivity(intent);
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
