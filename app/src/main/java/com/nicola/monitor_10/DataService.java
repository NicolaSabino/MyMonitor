package com.nicola.monitor_10;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

/**
 * Created by nicola on 21/11/16.
 */

public class DataService extends IntentService {

        private DbManager db;
        private boolean trigger;

        //costruttore
        public DataService() {
            super("DataService");
            db = new DbManager(this);
            trigger = true;
        }

        @Override
        protected void onHandleIntent(Intent i) {
            Uri uri = i.getData();
            while(trigger)
            {
                this.salva();
                try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }

        }

        @Override
        public void onDestroy() {
            Log.i("Data_SERVICE", "Distruzione Service");
            trigger=false;
            super.onDestroy();
        }

        public void salva()
        {
            //TODO sostituire le tre stringhe con dati veri!
            db.save(Math.random(),Math.random(),Math.random(),true,true);
        }



}

