package com.willowtreeapps.android.elevatorroom;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.android.support.lifecycle.LifecycleActivity;
import com.android.support.lifecycle.Observer;

public class ElevatorActivity extends LifecycleActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elevator);
        final TextView textview = (TextView) findViewById(R.id.textview);
        textview.setText("elevator!");

        BarometerManager barometerManager = BarometerManager.getInstance();
        barometerManager.observe(this, new Observer<Float>() {
            @Override
            public void onChanged(@Nullable Float aFloat) {
                textview.setText("Pressure: " + aFloat.toString());
            }
        });
    }

}
