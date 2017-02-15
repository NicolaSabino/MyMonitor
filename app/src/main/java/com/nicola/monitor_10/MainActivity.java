package com.nicola.monitor_10;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.view.menu.MenuView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;


import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;


import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.R.attr.key;

/**
 * Main activity dell'applicazione
 */
public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {


    private boolean stato;
    private FloatingActionButton fab;
    boolean playPauseState;
    private Intent servizio;
    private MenuView.ItemView s;
    private DbManager db;
    private LineGraphSeries<DataPoint> light,sound,movement;
    private GraphView graph1,graph2,graph3;
    private int currentGraphIndex;

    private int frequenza ;
    private int numeroDatiGrafico;
    private boolean AsseX;
    private boolean AsseY;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        readSettings();
        fabConfig();

        initStartStop();


        //settaggio dell'intent che gestisce l'acquisizione dati
        this.servizio = new Intent(this,DataService.class);

        //imposto il db manager
        db = new DbManager(getApplicationContext());

        popolaTabella();

        light = new LineGraphSeries<DataPoint>();
        sound = new LineGraphSeries<DataPoint>();
        movement = new LineGraphSeries<DataPoint>();

        graph1 = (GraphView) findViewById(R.id.graph1);
        graph2 = (GraphView) findViewById(R.id.graph2);
        graph3 = (GraphView) findViewById(R.id.graph3);


        initGrafici();
        currentGraphIndex = 0;
        popolaGrafici();

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReciver,new IntentFilter("evento-popola-tabella"));
    }

    private void fabConfig(){

        fab     = (FloatingActionButton) findViewById(R.id.fab);
        if(!stato){
            fab     .setBackgroundTintList(ColorStateList.valueOf(Color.rgb(255,193,7)));//ARANCIONE
            fab     .setImageDrawable(getResources().getDrawable(R.drawable.sun,getTheme()));
        }else{
            fab.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(48,63,159)));//blu
            fab.setImageDrawable(getResources().getDrawable(R.drawable.moon,getTheme()));
        }

        fab     .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cambiastato(view);
            }
        });
    }

    private BroadcastReceiver mMessageReciver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //ogni volta che ricevo un intent popolo la tabella
            popolaTabella();
            float l = Float.parseFloat(intent.getStringExtra("light"));
            float s = Float.parseFloat(intent.getStringExtra("sound"));
            float m = Float.parseFloat(intent.getStringExtra("motion"));
            appendValuesTabella(l,s,m);
        }
    };

    @Override
    protected void onPostResume() {
        super.onPostResume();
        readSettings();
        popolaTabella();
        MessageHelper.log("RESUME", "popolo la tabella");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
        saveState();

    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReciver);
        super.onDestroy();
    }

    private void cambiastato(View view) {
        //se stavo dormento
        if(stato){
            MessageHelper.snak(view,"Buongiorno");
            fab.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(255,193,7)));//arancione
            fab.setImageDrawable(getResources().getDrawable(R.drawable.sun,getTheme()));

            //notifica
            deleteNotification();
            generateNotification(this,"Ricorda di darmi la buonanotte \u263A",R.drawable.sun);

        }else{
            MessageHelper.snak(view,"Buonanotte");
            fab.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(48,63,159)));//blu
            fab.setImageDrawable(getResources().getDrawable(R.drawable.moon,getTheme()));
            //notifica
            deleteNotification();
            generateNotification(this,"Ricorda di darmi il buongionro \u263b",R.drawable.moon);
        }

        MessageHelper.log("SWITCH_STATE", stato + " -> " + !stato);
        stato = !stato; //cambio lo stato dell'utente
        db.changeState(stato);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if(playPauseState){
            getMenuInflater().inflate(R.menu.menu_main, menu);
        }else{
            getMenuInflater().inflate(R.menu.menu_main_pause, menu);
            MessageHelper.toast(getApplicationContext(),"Acquisizione dati già in esecuzione");
        }

        this.s = (MenuView.ItemView) findViewById(R.id.alarmState);

        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this,SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.alarmState){
            if(checkPermission()){ //se ho i permessi per usare il microfono faccio partire il task in background
                startStop();
                return true;
            }else{ //altrimenti
                requestPermission();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }


    public void initStartStop(){

        ActionMenuItemView x = (ActionMenuItemView) findViewById(R.id.alarmState);

        if(playPauseState){

            if (x != null) {
                x.setIcon(getResources().getDrawable(android.R.drawable.ic_media_pause,getTheme()));
            }else {
                MessageHelper.log("ICONA","NULL");
            }

        }else {

            if (x != null) {
                x.setIcon(getResources().getDrawable(android.R.drawable.ic_media_play, getTheme()));
            } else {
                MessageHelper.log("ICONA", "NULL");
            }
        }
    }

    public void startStop(){

        ActionMenuItemView x = (ActionMenuItemView) findViewById(R.id.alarmState);

        if(playPauseState){

            if (x != null) {
                x.setIcon(getResources().getDrawable(android.R.drawable.ic_media_pause,getTheme()));
            }else {
                MessageHelper.log("ICONA","NULL");
            }

            //this.startService(servizio);
            scheduleAlarm();

            //messaggi
            MessageHelper.log("TOOLBAR","Play -> inizio acquisizione dati");
            MessageHelper.toast(getApplicationContext(),"Inizio acquisizione dati");

            playPauseState = false;
            saveState();

        }else{

            if (x != null) {
                x.setIcon(getResources().getDrawable(android.R.drawable.ic_media_play,getTheme()));
            }else {
                MessageHelper.log("ICONA","NULL");
            }


            //this.stopService(servizio);
            cancelAlarm();

            //messaggi
            MessageHelper.log("TOOLBAR","Pause -> acquisizione dati sospesa");
            MessageHelper.toast(getApplicationContext(),"Acquisizione dati interrotta");

            playPauseState = true;
            saveState();
        }
    }

    public void popolaTabella() {

        //seleziona le info dal database e ripongo il risultato in un oggetto cursore
        final Cursor crs = db.query();

        CursorAdapter adapter = new CursorAdapter(this, crs, 0) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                View v = getLayoutInflater().inflate(R.layout.custom_row_table,null);
                return v;
            }

            @Override
            public void bindView(View v, Context context, Cursor cursor) {

                //prendo le informazioni dalla query
                String id           = crs.getString(crs.getColumnIndex(DatabaseStrings.FIELD_ID));
                String light        = crs.getString(crs.getColumnIndex(DatabaseStrings.FIELD_LIGHT));
                String movement     = crs.getString(crs.getColumnIndex(DatabaseStrings.FIELD_MOVEMENT));
                String sound        = crs.getString(crs.getColumnIndex(DatabaseStrings.FIELD_SOUND));
                String charging     = crs.getString(crs.getColumnIndex(DatabaseStrings.FIELD_CHARGING));
                String locked       = crs.getString(crs.getColumnIndex(DatabaseStrings.FIELD_LOCKED));
                String time         = crs.getString(crs.getColumnIndex(DatabaseStrings.FIELD_TIME));
                String date         = crs.getString(crs.getColumnIndex(DatabaseStrings.FIELD_DATE));






                //stampo le informazioni nella riga della tabella
                TextView elem0 = (TextView) v.findViewById(R.id.idContent);
                TextView elem1 = (TextView) v.findViewById(R.id.lightContent);
                TextView elem2 = (TextView) v.findViewById(R.id.movContent);
                TextView elem3 = (TextView) v.findViewById(R.id.soundContent);
                TextView elem4 = (TextView) v.findViewById(R.id.chargingContent);
                TextView elem5 = (TextView) v.findViewById(R.id.lockContent);
                TextView elem6 = (TextView) v.findViewById(R.id.dateContent);
                TextView elem7 = (TextView) v.findViewById(R.id.timeContent);

                elem0.setText(id);
                elem1.setText(light);
                elem2.setText(movement);
                elem3.setText(sound);
                elem6.setText(date);
                elem7.setText(time);

                //modifico charging e loked
                String _charging, _locked;

                if(Boolean.parseBoolean(charging)){
                    elem4.setText("\u26A1");
                }else{
                    elem4.setText("");
                }


                if(Boolean.parseBoolean(locked)){
                    elem5.setText("\u26BF");
                }else{
                    elem5.setText("");
                }





            }
        };


        ListView listaValori = (ListView) findViewById(R.id.listaValori);
        listaValori.setAdapter(adapter);

    }

    public void initGrafici(){

        //  definisco lo stile dei grafici

        light.setTitle("light");
        light.setColor(Color.rgb(255,193,7));//ambra
        light.setDrawBackground(true);
        light.setBackgroundColor(Color.argb(60,255,193,7));
        light.setDrawDataPoints(true);
        light.setDataPointsRadius(8);
        light.setThickness(5);
        light.setAnimated(true);

        movement.setTitle("movement");
        movement.setColor(Color.rgb(230,74,25));//rosso
        movement.setDrawBackground(true);
        movement.setBackgroundColor(Color.argb(60,230,74,25));
        movement.setDrawDataPoints(true);
        movement.setDataPointsRadius(8);
        movement.setThickness(5);
        movement.setAnimated(true);

        sound.setTitle("sound");
        sound.setColor(Color.rgb(63,81,181));//blu
        sound.setDrawBackground(true);
        sound.setBackgroundColor(Color.argb(60,63,81,181));
        sound.setDrawDataPoints(true);
        sound.setDataPointsRadius(8);
        sound.setThickness(5);
        sound.setAnimated(true);

        graph1.getViewport().setXAxisBoundsManual(true);
        graph1.getViewport().setMinX(1);
        graph1.getViewport().setMaxX(20);
        graph1.getGridLabelRenderer().setHorizontalLabelsVisible(AsseX);
        graph1.getGridLabelRenderer().setVerticalLabelsVisible(AsseY);
        graph1.setTitleColor(Color.rgb(255,193,7));

        graph2.getViewport().setXAxisBoundsManual(true);
        graph2.getViewport().setMinX(1);
        graph2.getViewport().setMaxX(20);
        graph2.getGridLabelRenderer().setHorizontalLabelsVisible(AsseX);
        graph2.getGridLabelRenderer().setVerticalLabelsVisible(AsseY);
        graph2.setTitleColor(Color.rgb(48,63,159));

        graph3.getViewport().setXAxisBoundsManual(true);
        graph3.getViewport().setMinX(1);
        graph3.getViewport().setMaxX(20);
        graph3.getGridLabelRenderer().setHorizontalLabelsVisible(AsseX);
        graph3.getGridLabelRenderer().setVerticalLabelsVisible(AsseY);
        graph3.setTitleColor(Color.rgb(230,74,25));

        graph1.setTitle("Light");
        graph2.setTitle("Sound");
        graph3.setTitle("Movement");

        graph1.getViewport().setScrollable(true);
        graph2.getViewport().setScrollable(true);
        graph3.getViewport().setScrollable(true);

        graph1.addSeries(light);
        graph2.addSeries(sound);
        graph3.addSeries(movement);
    }

    public void popolaGrafici(){

        final Cursor crs = db.reverseQuery();

        if (crs == null) {
            // do nothing
        } else {
            if(crs.moveToFirst()){
                for (int i=0;i<crs.getCount();i++) {
                    String id = crs.getString(0);
                    light.appendData(new DataPoint(Integer.valueOf(id),crs.getDouble(1)),true,numeroDatiGrafico);
                    sound.appendData(new DataPoint(Integer.valueOf(id),crs.getDouble(2)),true,numeroDatiGrafico);
                    movement.appendData(new DataPoint(Integer.valueOf(id),crs.getDouble(3)),true,numeroDatiGrafico);
                    crs.moveToNext();
                    currentGraphIndex=Integer.valueOf(id);
                }
            }
        }
    }

    public void appendValuesTabella(float l,float s,float m){
        currentGraphIndex++;
        light.appendData(new DataPoint(currentGraphIndex,l),true,numeroDatiGrafico);
        sound.appendData(new DataPoint(currentGraphIndex,s),true,numeroDatiGrafico);
        movement.appendData(new DataPoint(currentGraphIndex,m),true,numeroDatiGrafico);

    }


    public boolean checkPermission(){

        int result = ContextCompat.checkSelfPermission(getApplicationContext(),
                RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED ;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new
                String[]{RECORD_AUDIO,WRITE_EXTERNAL_STORAGE}, 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1:

                    boolean RecordPermission = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;

                    if (RecordPermission) {
                        startStop();
                        MessageHelper.toast(this,"Permesso garantito!");
                    } else {
                        MessageHelper.toast(this,"Permesso negato");
                    }

                break;
        }
    }

    @Override
    public void onBackPressed() {
        saveState();
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void saveState(){
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("stato",stato);
        editor.putBoolean("playPauseState",this.playPauseState);
        editor.apply();
    }

    public void scheduleAlarm() {
        // Costruisco un intent che eseguirà l'AlarmReceiver
        Intent intent = new Intent(getApplicationContext(), MyAlarmReceiver.class);

        // Creo un  PendingIntent che verrà attivato quando l'allarme si spegne
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, MyAlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        long firstMillis = System.currentTimeMillis(); // alarm is set right away
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP,firstMillis,
                1000 * 60 * frequenza , pIntent);
    }

    public void cancelAlarm() {
        Intent intent = new Intent(getApplicationContext(), MyAlarmReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, MyAlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
    }

    public void readSettings(){

        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);

        String f            = sharedpreferences.getString("freq","15");
        String nD           = sharedpreferences.getString("rend","100");
        stato               = sharedPref.getBoolean("stato",false);
        playPauseState      = sharedPref.getBoolean("playPauseState",true);
        frequenza           = Integer.parseInt(f);
        numeroDatiGrafico   = Integer.parseInt(nD);
        AsseX               =   sharedpreferences.getBoolean("X",false);
        AsseY               =   sharedpreferences.getBoolean("Y",false);

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

        if (s.equals(frequenza) && !playPauseState) {
            cancelAlarm();
            scheduleAlarm();
        }
    }

    private static void generateNotification(Context context, String message, int res){

        int mNotificationId = 001;

        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(res)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentIntent(intent)
                .setPriority(5) //private static final PRIORITY_HIGH = 5;
                .setContentText(message)
                .setAutoCancel(false);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(001, mBuilder.build());
    }

    public void deleteNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(001);
    }



}
