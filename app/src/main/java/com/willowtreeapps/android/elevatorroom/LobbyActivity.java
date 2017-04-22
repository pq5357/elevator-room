package com.willowtreeapps.android.elevatorroom;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import android.arch.lifecycle.LifecycleActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.willowtreeapps.android.elevatorroom.R.id.textview;

public class LobbyActivity extends LifecycleActivity {

    Unbinder unbinder;

    @BindView(textview) TextView label;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        unbinder = ButterKnife.bind(this);
        label.setText("lobby!");
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
    }

}
