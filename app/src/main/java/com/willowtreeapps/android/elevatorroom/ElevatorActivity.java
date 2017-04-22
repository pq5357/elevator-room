package com.willowtreeapps.android.elevatorroom;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import android.arch.lifecycle.LifecycleActivity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;

public class ElevatorActivity extends LifecycleActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elevator);
        final TextView textview = (TextView) findViewById(R.id.textview);
        textview.setText("elevator!");
        ElevatorViewModel model = ViewModelProviders.of(this).get(ElevatorViewModel.class);
        model.barometer.observe(this, new Observer<Float>() {
            @Override
            public void onChanged(@Nullable Float aFloat) {
                textview.setText("Pressure: " + aFloat.toString());
            }
        });
    }

}
