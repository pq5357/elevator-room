package com.willowtreeapps.android.elevatorroom;

import android.app.Service;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.android.support.lifecycle.Lifecycle;
import com.android.support.lifecycle.LifecycleObserver;
import com.android.support.lifecycle.LifecycleOwner;
import com.android.support.lifecycle.LiveData;
import com.android.support.lifecycle.Observer;
import com.android.support.lifecycle.OnLifecycleEvent;

/**
 * Created by willowtree on 4/20/17.
 */

public class BarometerManager implements LifecycleObserver {

    private static BarometerManager sInstance;
    private LiveData<Float> data = new LiveData<>();
    private SensorManager sensorManager;

    private SensorEventListener pressureListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            data.setValue(event.values[0]);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    public void observe(LifecycleOwner owner, Observer<Float> observer){
        data.observe(owner, observer);
    }

    private BarometerManager(LifecycleOwner owner) {
        owner.getLifecycle().addObserver(this);
        sensorManager = (SensorManager) MyApplication.getContext().getSystemService(Service.SENSOR_SERVICE);
    }

    public static BarometerManager getInstance(LifecycleOwner lifecycleOwner) {
        if (sInstance == null) {
            sInstance = new BarometerManager(lifecycleOwner);
        }
        return sInstance;
    }

    @OnLifecycleEvent(Lifecycle.ON_RESUME)
    protected void register() {
        sensorManager.registerListener(pressureListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @OnLifecycleEvent(Lifecycle.ON_PAUSE)
    protected void unregister() {
        sensorManager.unregisterListener(pressureListener);
    }

}
