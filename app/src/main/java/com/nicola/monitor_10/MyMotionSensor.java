package com.nicola.monitor_10;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.FloatMath;

/**
 * Created by nicola on 28/11/16.
 */

public class MyMotionSensor {

    private SensorManager mSensorManager;
    private Sensor sensore;
    private float[] mGravity;
    private float mAccel;
    private float mAccelLast;
    private float mAccelCurrent;


    public MyMotionSensor(SensorManager sm) {
        mSensorManager = sm;
    }
    /**
     * registro il listener sul sensore
     * @return true se la registrazione è andata a buon fine
     */
    public boolean registerMotionSensor(){
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
            sensore = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mSensorManager.registerListener(LightSensorListener,sensore, SensorManager.SENSOR_DELAY_GAME);
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
    public boolean unregisterMotionSensor() {
        mSensorManager.unregisterListener(this.LightSensorListener,sensore);
        return true;
    }

    public float getValue(){
        return mAccel;
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
            //shake detection
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float val = (float) Math.abs(Math.sqrt((double) (x*x + y*y + z*z)));
            mAccel = val - 9.8f;

        }

    };

}
