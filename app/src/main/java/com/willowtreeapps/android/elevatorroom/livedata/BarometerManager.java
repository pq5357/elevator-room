package com.willowtreeapps.android.elevatorroom.livedata;

import android.app.Service;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class BarometerManager extends LiveData<Float> {

    private final SensorManager sensorManager;
    private final GroundPressure pressureListener = new GroundPressure();

    private final Queue<Float> pressures = new LinkedList<>();

    public BarometerManager(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Service.SENSOR_SERVICE);
    }

    public LiveData<Float> getGroundPressure() {
        return pressureListener;
    }

    private void publishAveragePressure() {
        float sum = 0;
        for (Float pressure : pressures) {
            sum += pressure;
        }
        setValue(sum / pressures.size());
    }

    @Override
    protected void onActive() {
        super.onActive();
        sensorManager.registerListener(pressureListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE),
                SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(pressureListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        sensorManager.unregisterListener(pressureListener);
    }

    private class GroundPressure extends LiveData<Float> implements SensorEventListener {

        static final double GROUND_THRESHOLD = 0.04; // max allowed deviation in acceleration when phone is on the ground (or stable surface)

        float[] accelXWindow = new float[3];
        float[] accelYWindow = new float[3];
        float[] accelZWindow = new float[3];
        int windowIndex = 0;

        int groundCount = 0; // how many frames on the ground
        final List<Float> groundPressures = new ArrayList<>(); // pressures while on the ground

        @Override
        public void onSensorChanged(SensorEvent event) {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_PRESSURE:
                    if (groundCount > 3) {
                        groundPressures.add(event.values[0]);
                        if (groundPressures.size() > 4) {
                            publishAverageGroundPressure();
                        }
                        pressures.clear();
                    } else {
                        if (groundPressures.size() > 0) {
                            publishAverageGroundPressure();
                        }
                        pressures.offer(event.values[0]);
                        if (pressures.size() > 4) {
                            pressures.poll();
                        }
                        publishAveragePressure();
                    }
                    break;
                case Sensor.TYPE_ACCELEROMETER:
                    // keep running window of XYZ accelerations
                    accelXWindow[windowIndex] = event.values[0];
                    accelYWindow[windowIndex] = event.values[1];
                    accelZWindow[windowIndex] = event.values[2];
                    windowIndex = (windowIndex + 1) % 3;
                    // find deviation on each axis
                    float maxX = Math.max(accelXWindow[0], Math.max(accelXWindow[1], accelXWindow[2]));
                    float minX = Math.min(accelXWindow[0], Math.min(accelXWindow[1], accelXWindow[2]));
                    float maxY = Math.max(accelYWindow[0], Math.max(accelYWindow[1], accelYWindow[2]));
                    float minY = Math.min(accelYWindow[0], Math.min(accelYWindow[1], accelYWindow[2]));
                    float maxZ = Math.max(accelZWindow[0], Math.max(accelZWindow[1], accelZWindow[2]));
                    float minZ = Math.min(accelZWindow[0], Math.min(accelZWindow[1], accelZWindow[2]));
                    // use max deviation among the 3 axes
                    float globalMax = Math.max(maxX - minX, Math.max(maxY - minY, maxZ - minZ));
                    if (globalMax < GROUND_THRESHOLD) { // assume phone is on the ground
                        groundCount++;
                    } else { // assume phone is in user's hand
                        groundCount = 0;
                    }
                    break;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        private void publishAverageGroundPressure() {
            float sum = 0;
            for (Float pressure : groundPressures) {
                sum += pressure;
            }
            setValue(sum / groundPressures.size());
            groundPressures.clear();
        }

    }

}
