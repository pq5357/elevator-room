package com.willowtreeapps.android.elevatorroom.intro;

import android.arch.lifecycle.LifecycleActivity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import com.willowtreeapps.android.elevatorroom.DisplayUtil;
import com.willowtreeapps.android.elevatorroom.ElevatorActivity;
import com.willowtreeapps.android.elevatorroom.LobbyActivity;
import com.willowtreeapps.android.elevatorroom.MyApplication;
import com.willowtreeapps.android.elevatorroom.R;
import com.willowtreeapps.android.elevatorroom.RxUtil;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.schedulers.Schedulers;

public class IntroActivity extends LifecycleActivity {

    @BindView(android.R.id.content) View rootView;

    Unbinder unbinder;
    Disposable disposable = Disposables.disposed();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        unbinder = ButterKnife.bind(this);
        checkMultiWindow();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        checkMultiWindow();
    }

    private void checkMultiWindow() {
        if (isInMultiWindowMode()) {
            startGame();
        }
    }

    public void startGame() {
        clearPreviousData();
        disposable = Observable.timer(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.computation())
                .subscribe(aLong -> {
                    Intent lobby = new Intent(this, LobbyActivity.class);
                    Intent elevator = new Intent(this, ElevatorActivity.class);
                    if (rootView != null) {
                        if (DisplayUtil.isMultiWindowPrimary(rootView)) {
                            //Launch elevator in adjacent MultiWindow
                            elevator.addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                            startActivities(new Intent[]{lobby, elevator});
                        } else {
                            //Launch lobby in adjacent MultiWindow
                            lobby.addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                            startActivities(new Intent[]{elevator, lobby});
                        }
                        finish();
                    }
                });

    }

    void clearPreviousData() {
        disposable.dispose();
        RxUtil.runInBg(() -> {
            MyApplication.getGameDatabase().floorDao().deleteAllFloors();
            MyApplication.getGameDatabase().personDao().deleteAllPeople();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (unbinder != null) {
            unbinder.unbind();
        }
    }
}
