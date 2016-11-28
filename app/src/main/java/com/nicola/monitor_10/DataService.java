package com.nicola.monitor_10;

import android.app.IntentService;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.util.Log;

/**
 * Created by nicola on 21/11/16.
 */

public class DataService extends IntentService implements SensorEventListener {

        private DbManager       db;
        private boolean         trigger;
        private SensorManager   mSensorManager;
        private Sensor          mSensor;

        //costruttore
        public DataService() {
            super("DataService");
            db = new DbManager(this);
            trigger = true;

        }


        /**
         * Corpo principale dell'intent dove si orchestrano le acquisizioni e il delay
         */
        @Override
        protected void onHandleIntent(Intent i) {

            initManager();
            lightSensor();
            while(trigger)
            {
                this.salva();
                pause(1000);
            }

        }

    /*
     * il SensorManager deve essere NECESSARIAMENTE inizializzado dopo l'handelIntent perchè prima non vi è alcun Context nel quale
     * l'intent adopera, di conseguenza non sono disponibili i servizi dei sensori
     */
    private void initManager() {
        mSensorManager =(SensorManager) this.getSystemService(SENSOR_SERVICE);
    }

    private void lightSensor() {


        //istanzio il sensore della luminosità
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null){
            MessageHelper.log("sensore di luminosità","SENSORE INIZIALIZZATO CORRETTAMENTE");
        }
        else {
            MessageHelper.log("sensore di luminosità","errore nell'inizializzazione");
        }
    }

    @Override
        public void onDestroy() {
            Log.i("Data_SERVICE", "Distruzione Service");
            trigger=false;
            super.onDestroy();
        }


        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }


        private void pause(int milllisec) {
            try {
                Thread.sleep(milllisec);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        public void salva()
        {
            //TODO sostituire le tre stringhe con dati veri!
            db.save(Math.random(),Math.random(),Math.random(),true,true);
        }



}

