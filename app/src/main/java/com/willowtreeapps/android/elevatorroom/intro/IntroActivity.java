package com.willowtreeapps.android.elevatorroom.intro;

import android.arch.lifecycle.LifecycleActivity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.willowtreeapps.android.elevatorroom.DisplayUtil;
import com.willowtreeapps.android.elevatorroom.ElevatorActivity;
import com.willowtreeapps.android.elevatorroom.LobbyActivity;
import com.willowtreeapps.android.elevatorroom.MyApplication;
import com.willowtreeapps.android.elevatorroom.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class IntroActivity extends LifecycleActivity {

    @BindView(android.R.id.content) View rootView;
    @BindView(R.id.start) Button btnStart;

    Unbinder unbinder;

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
        btnStart.setEnabled(isInMultiWindowMode());
    }

    @OnClick(R.id.start)
    public void startGame() {
        clearPreviousData();

        Intent lobby = new Intent(this, LobbyActivity.class);
        Intent elevator = new Intent(this, ElevatorActivity.class);
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

    void clearPreviousData() {
        MyApplication.getGameDatabase().floorDao().deleteAllFloors();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (unbinder != null) {
            unbinder.unbind();
        }
    }
}
