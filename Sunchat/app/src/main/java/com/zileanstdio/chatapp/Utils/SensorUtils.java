package com.zileanstdio.chatapp.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.PowerManager;

@SuppressLint("StaticFieldLeak")
public class SensorUtils implements SensorEventListener {

    private final Context context;
    private static SensorUtils instance;
    private SensorManager sensorManager;
    private PowerManager.WakeLock wakeLock;

    public static SensorUtils getInstance(Context context) {
        if (instance == null) {
            instance = new SensorUtils(context);
        }
        return instance;
    }

    public SensorUtils(Context context) {
        this.context = context;
    }

    public void acquireProximitySensor(String className) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        int screenLockValue = PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK;
        wakeLock = powerManager.newWakeLock(screenLockValue, className);

        wakeLock.acquire(1440 * 60 * 1000L);
    }

    public void releaseSensor() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
            sensorManager = null;
        }

        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            wakeLock = null;
        }

        if (instance != null) {
            instance = null;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float value = sensorEvent.values[0];
        if (value == 0) {
            if (wakeLock != null && !wakeLock.isHeld()) {
                wakeLock.acquire(1440 * 60 * 1000L);
            }
        } else {
            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}