package com.willowtreeapps.android.elevatorroom;

import android.arch.lifecycle.LifecycleActivity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

public class ElevatorActivity extends LifecycleActivity {

    ElevatorViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elevator);
        final TextView textview = (TextView) findViewById(R.id.textview);
        textview.setText("elevator!");
        viewModel = ViewModelProviders.of(this).get(ElevatorViewModel.class);
        viewModel.writePressureToDatabase(this);
        viewModel.barometer.observe(this, new Observer<Float>() {
            @Override
            public void onChanged(@Nullable Float aFloat) {
                textview.setText("Pressure: " + aFloat.toString());
            }
        });
    }

}
