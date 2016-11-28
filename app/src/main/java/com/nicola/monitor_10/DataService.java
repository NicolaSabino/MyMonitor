package com.nicola.monitor_10;

import android.app.IntentService;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nicola on 21/11/16.
 */

public class DataService extends IntentService  {

        private DbManager       db;
        private boolean         trigger;
        private MyLightSensor   myLightSensor;

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

            initSensori();//inizializzo il SensorManager e dei vari sensori
            while(trigger)
            {
                this.salva();
                pause(1000);
            }

        }


        /*
         * Le classi che astraggono i sensori devono essere NECESSARIAMENTE inizializzate dopo l'handelIntent perchè prima non vi è alcun Context nel quale
         * l'intent adopera, di conseguenza non sono disponibili i servizi dei sensori
         */
        private void initSensori() {
            SensorManager mymanager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
            myLightSensor  = new MyLightSensor(mymanager);
        }

        @Override
        public void onDestroy() {
            MessageHelper.log("Data_SERVICE", "Distruzione Service");
            trigger=false;
            super.onDestroy();
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

