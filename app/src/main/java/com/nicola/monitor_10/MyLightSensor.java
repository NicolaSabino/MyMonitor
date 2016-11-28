package com.nicola.monitor_10;

import android.app.IntentService;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nicola on 28/11/16.
 */

public class MyLightSensor{

    private SensorManager mSensorManager;
    private Sensor sensore;
    private float value;

    public MyLightSensor(SensorManager sm) {
        mSensorManager = sm;
    }

    /**
     * registro il listener sul sensore
     * @return true se la registrazione è andata a buon fine
     */
    public boolean registerLightSensor(){
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null){
            sensore = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            mSensorManager.registerListener(LightSensorListener,sensore, SensorManager.SENSOR_DELAY_NORMAL);
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Scollego il listener al sensore
     * @return
     */
    public boolean unregisterLightSensor() {
        mSensorManager.unregisterListener(this.LightSensorListener,sensore);
        return true;
    }

    public float getValue(){
        return value;
    }

    /**
     * Listner che registro ogni volta che mi serve [necessario per risparmiare la batteria]
     */
    private final SensorEventListener LightSensorListener = new SensorEventListener(){

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            value = event.values[0];
        }

    };
}
