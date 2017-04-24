package com.willowtreeapps.android.elevatorroom;

import android.arch.lifecycle.LifecycleActivity;
import android.arch.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.widget.TextView;

import com.willowtreeapps.android.elevatorroom.persistence.VisitedFloor;

import org.reactivestreams.Subscription;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.functions.Consumer;


import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class LobbyActivity extends LifecycleActivity {

    private Unbinder unbinder;
    private Disposable floorDisposable = Disposables.disposed();
    private LobbyViewModel viewModel;

    @BindView(R.id.textview)
    TextView label;
    @BindView(R.id.person)
    View person;
    @BindView(R.id.playfield)
    View playField;
    Disposable disposable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        unbinder = ButterKnife.bind(this);
        viewModel = ViewModelProviders.of(this).get(LobbyViewModel.class);
        floorDisposable.dispose();
        floorDisposable = viewModel.currentFloor()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(floor -> label.setText(getString(R.string.floor_n, floor.getFloor())));
    }

    @Override
    protected void onStart() {
        super.onStart();
        disposable = Observable.interval(5, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.computation())
                .subscribe(aLong -> animatePerson());
        person.setOnClickListener(v -> startDrag());
    }

    private boolean startDrag() {
        person.startDragAndDrop(null, getShadow(person), Boolean.TRUE,
                View.DRAG_FLAG_GLOBAL|View.DRAG_FLAG_GLOBAL_URI_READ|
                        View.DRAG_FLAG_GLOBAL_PERSISTABLE_URI_PERMISSION);
        return true;
    }

    private View.DragShadowBuilder getShadow(View v) {
        return new View.DragShadowBuilder(){
            @Override
            public void onProvideShadowMetrics(Point outShadowSize, Point outShadowTouchPoint) {
                outShadowSize.set(v.getWidth()/8, v.getHeight()/8);
                outShadowTouchPoint.set(outShadowSize.x/2, outShadowSize.y/2);
            }

            @Override
            public void onDrawShadow(Canvas canvas) {
                v.draw(canvas);
            }
        };
    }

    private void animatePerson() {
        Random random = new Random();
        person.animate().x(random.nextInt(Math.max(playField.getWidth() - person.getWidth(), 0))).y(random.nextInt(Math.max(playField.getHeight() - person.getWidth(), 0))).setDuration(2000).start();
    }

    @OnClick(R.id.btn_launch)
    public void launchElevator() {
        Intent lobby = new Intent(this, LobbyActivity.class);
        Intent elevator = new Intent(this, ElevatorActivity.class);
        elevator.addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT); //Launch in adjacent MultiWindow
        startActivities(new Intent[]{lobby, elevator});
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
        if (unbinder != null) {
            unbinder.unbind();
        }
        floorDisposable.dispose();
    }

}
