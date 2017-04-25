package com.willowtreeapps.android.elevatorroom.intro;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.willowtreeapps.android.elevatorroom.ElevatorActivity;
import com.willowtreeapps.android.elevatorroom.LobbyActivity;
import com.willowtreeapps.android.elevatorroom.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class IntroActivity extends AppCompatActivity {

    Unbinder unbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        unbinder = ButterKnife.bind(this);
    }

    @OnClick(R.id.start)
    public void startGame() {
        Intent lobby = new Intent(this, LobbyActivity.class);
        Intent elevator = new Intent(this, ElevatorActivity.class);
        elevator.addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT); //Launch in adjacent MultiWindow
        startActivities(new Intent[]{lobby, elevator});
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (unbinder != null) {
            unbinder.unbind();
        }
    }
}
