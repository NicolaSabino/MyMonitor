package com.nicola.monitor_10;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nicola on 28/11/16.
 */

public class MyLightSensor implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor sensore;

    public MyLightSensor(SensorManager sm) {
        mSensorManager = sm;

        //istanzio il sensore della luminosità
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null){

            sensore = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        }
        else {
            MessageHelper.log("MyLightSensor","Errore durante l'inizializzazione del sensore di luminosità");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //non fare niente
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //non fare niente
    }

    public float getCampione(){
        //acquisizione dati a singola chiamata
        return 0;
    }
}
