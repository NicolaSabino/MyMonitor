package com.nicola.monitor_10;

import android.app.IntentService;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by nicola on 21/11/16.
 */

public class DataService extends IntentService  {

        private DbManager           db;
        private boolean             trigger;
        private MyLightSensor       myLightSensor;
        private MyMotionSensor      myMotionSensor;
        private SoundMeter          soundMeter;

        //costruttore
        public DataService() {
            super("DataService");
            db = new DbManager(this);

        }


        /**
         * Corpo principale dell'intent dove si orchestrano le acquisizioni e il delay
         */
        @Override
         protected void onHandleIntent(Intent i) {



            initSensori();//inizializzo il SensorManager e dei vari sensori
            MessageHelper.log("DataService","Acquisizione dati in corso");
            myLightSensor       .registerLightSensor();
            myMotionSensor      .registerMotionSensor();
            soundMeter          .start();

            try {
                    Thread.sleep(100);//mezzo secondo per permetere di registrare corretamente i sensori
                }catch (Exception ex){
                    ex.printStackTrace();
                }



            String light    = String.valueOf(myLightSensor.getValue());
            String motion   = String.valueOf(myMotionSensor.getValue());
            String sound    = String.valueOf(soundMeter.getAmplitude());
            String plugged  = String.valueOf(isPhonePluggedIn());
            String locked   = String.valueOf(isPhoneLocked());


            this.salva(light, motion, sound, plugged, locked);

            //popolo la tabella mandando un messaggio di broadcast
            Intent intent = new Intent("evento-popola-tabella");
            intent.putExtra("light",light);
            intent.putExtra("motion",motion);
            intent.putExtra("sound",sound);

            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

            myLightSensor       .unregisterLightSensor();
            myMotionSensor      .unregisterMotionSensor();
            soundMeter          .stop();

            MessageHelper.log("MainLoop DataService","Fine Acquisizione");
            //pause(600000);//aspetto

        }


        /*
         * Le classi che astraggono i sensori devono essere NECESSARIAMENTE inizializzate dopo l'handelIntent perchè prima non vi è alcun Context nel quale
         * l'intent adopera, di conseguenza non sono disponibili i servizi dei sensori
         */
        private void initSensori() {
            SensorManager mymanager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
            myLightSensor       = new MyLightSensor(mymanager);
            myMotionSensor      = new MyMotionSensor(mymanager);
            soundMeter          = new SoundMeter();
        }

        @Override
        public void onDestroy() {
            MessageHelper.log("Data_SERVICE", "Distruzione Service");
            super.onDestroy();
        }


        /*private void pause(int milllisec) {
            try {
                Thread.sleep(milllisec);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }*/

        public void salva(String light,String movement,String sound,String charging,String locked)
        {
            db.save(light,movement,sound,charging,locked);
        }

        public boolean isPhonePluggedIn(){

            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = registerReceiver(null, ifilter);
            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;
            return isCharging;
        }


        public boolean isPhoneLocked(){
            KeyguardManager myKM = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            if( myKM.inKeyguardRestrictedInputMode()) {
                return true;
            } else {
                return false;
            }
        }

}

