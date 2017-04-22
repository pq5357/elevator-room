package com.willowtreeapps.android.elevatorroom;

import android.app.Service;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.OnLifecycleEvent;

public class BarometerManager extends LiveData<Float> {

    private static BarometerManager sInstance;
    private SensorManager sensorManager;

    SensorEventListener pressureListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            setValue(event.values[0]);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    private BarometerManager() {
        sensorManager = (SensorManager) MyApplication.getContext().getSystemService(Service.SENSOR_SERVICE);
    }

    public static BarometerManager getInstance() {
        if (sInstance == null) {
            sInstance = new BarometerManager();
        }
        return sInstance;
    }

    @Override
    protected void onActive() {
        super.onActive();
        sensorManager.registerListener(pressureListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        sensorManager.unregisterListener(pressureListener);
    }

}
