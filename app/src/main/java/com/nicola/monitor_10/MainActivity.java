package com.nicola.monitor_10;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
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

/**
 * Main activity dell'applicazione
 */
public class MainActivity extends AppCompatActivity {


    private boolean stato;
    private FloatingActionButton fab;
    boolean playPauseState;
    private Intent servizio;
    private MenuView.ItemView s;
    private DbManager db;
    private LineGraphSeries<DataPoint> light,sound,movement;
    private GraphView graph1,graph2,graph3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        fab     = (FloatingActionButton) findViewById(R.id.fab);
        stato   = false;
        fab     .setBackgroundTintList(ColorStateList.valueOf(Color.rgb(255,153,0)));//ARANCIONE
        fab     .setImageDrawable(getResources().getDrawable(R.drawable.sun,getTheme()));
        fab     .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cambiastato(view);
            }
        });

        //settaggio dello stato del bottone playPause
        playPauseState = true; //il bottone è posizionato su play

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

        light.setColor(Color.GREEN);
        sound.setColor(Color.RED);
        movement.setColor(Color.BLUE);

        initGrafici();

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReciver,new IntentFilter("evento-popola-tabella"));
    }

    private BroadcastReceiver mMessageReciver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //ogni volta che ricevo un intent popolo la tabella
            popolaTabella();
            appendValuesTabella();
        }
    };

    @Override
    protected void onPostResume() {
        super.onPostResume();
        popolaTabella();
        deleteNotification();
        MessageHelper.log("RESUME", "popolo la tabella");
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
            fab.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(255,153,0)));//arancione
            fab.setImageDrawable(getResources().getDrawable(R.drawable.sun,getTheme()));

        }else{
            MessageHelper.snak(view,"Buonanotte");
            fab.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(63,81,181)));//blu
            fab.setImageDrawable(getResources().getDrawable(R.drawable.moon,getTheme()));
        }

        MessageHelper.log("SWITCH_STATE", stato + " -> " + !stato);
        stato = !stato; //cambio lo stato dell'utente
        /*db.changeState(stato); */ //salvo il nuovo stato nel database
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.s = (MenuView.ItemView) findViewById(R.id.stato);
        return true;
    }


    /**
     * Metodo che cattura la selezione degli elementi sulla ActionBar da parte dell'utente
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.clearData){
            db.deleteTables();
            popolaTabella();
            MessageHelper.toast(this,"Informazioni eliminate correttamente");
            return true;
        }

        if (id == R.id.stato){
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

    /**
     * Metodo che gestisce il pulsante start stop della toolbar
     */
    public void startStop(){

        ActionMenuItemView x = (ActionMenuItemView) findViewById(R.id.stato);

        if(playPauseState){

            if (x != null) {
                x.setIcon(getResources().getDrawable(android.R.drawable.ic_media_pause,getTheme()));
            }else {
                MessageHelper.log("ICONA","NULL");
            }


            this.startService(servizio);

            //messaggi
            MessageHelper.log("TOOLBAR","Play -> inizio acquisizione dati");
            MessageHelper.toast(getApplicationContext(),"Inizio acquisizione dati");

        }else{

            if (x != null) {
                x.setIcon(getResources().getDrawable(android.R.drawable.ic_media_play,getTheme()));
            }else {
                MessageHelper.log("ICONA","NULL");
            }


            this.stopService(servizio);

            //messaggi
            MessageHelper.log("TOOLBAR","Pause -> acquisizione dati sospesa");
            MessageHelper.toast(getApplicationContext(),"Acquisizione dati interrotta");
        }

        playPauseState = !playPauseState;
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
                //TextView elem0 = (TextView) v.findViewById(R.id.idContent);
                TextView elem1 = (TextView) v.findViewById(R.id.lightContent);
                TextView elem2 = (TextView) v.findViewById(R.id.movContent);
                TextView elem3 = (TextView) v.findViewById(R.id.soundContent);
                TextView elem4 = (TextView) v.findViewById(R.id.chargingContent);
                TextView elem5 = (TextView) v.findViewById(R.id.lockContent);
                TextView elem6 = (TextView) v.findViewById(R.id.dateContent);
                TextView elem7 = (TextView) v.findViewById(R.id.timeContent);

                //elem0.setText(id);
                elem1.setText(light);
                elem2.setText(sound);
                elem3.setText(movement);
                elem4.setText(charging);
                elem5.setText(locked);
                elem6.setText(date);
                elem7.setText(time);


            }
        };


        ListView listaValori = (ListView) findViewById(R.id.listaValori);
        listaValori.setAdapter(adapter);

    }

    public void initGrafici(){

        final Cursor crs = db.reverseQuery();

        if (crs == null) {
            // do nothing
        } else {
            if(crs.moveToFirst()){
                for (int i=0;i<crs.getCount();i++) {

                    String id = crs.getString(0);
                    light.appendData(new DataPoint(Integer.valueOf(id),crs.getDouble(1)),true,30);
                    sound.appendData(new DataPoint(Integer.valueOf(id),crs.getDouble(2)),true,30);
                    movement.appendData(new DataPoint(Integer.valueOf(id),crs.getDouble(3)),true,30);
                    crs.moveToNext();
                }
            }
        }

        light.setTitle("light");
        light.setColor(Color.rgb(255,153,0));
        light.setDrawBackground(true);
        light.setBackgroundColor(Color.argb(60,255,153,0));
        light.setDrawDataPoints(true);
        light.setDataPointsRadius(8);
        light.setThickness(5);

        movement.setTitle("movement");
        movement.setColor(Color.RED);
        movement.setDrawBackground(true);
        movement.setBackgroundColor(Color.argb(60,255,0,0));
        movement.setDrawDataPoints(true);
        movement.setDataPointsRadius(8);
        movement.setThickness(5);

        sound.setTitle("sound");
        sound.setColor(Color.BLUE);
        sound.setDrawBackground(true);
        sound.setBackgroundColor(Color.argb(60,0,0,255));
        sound.setDrawDataPoints(true);
        sound.setDataPointsRadius(8);
        sound.setThickness(5);

        graph1.addSeries(light);
        graph2.addSeries(sound);
        graph3.addSeries(movement);

        graph1.setTitle("light");
        graph2.setTitle("sound");
        graph3.setTitle("movement");

        graph1.getViewport().setScrollable(true);
        graph2.getViewport().setScrollable(true);
        graph3.getViewport().setScrollable(true);
    }

    public void appendValuesTabella(){
        final Cursor crs = db.lastValue();

        if (crs == null) {
            // do nothing
        } else {
            if(crs.moveToFirst()){
                for (int i=0;i<crs.getCount();i++) {

                    String id = crs.getString(0);
                    light.appendData(new DataPoint(Integer.valueOf(id),crs.getDouble(1)),true,30);
                    sound.appendData(new DataPoint(Integer.valueOf(id),crs.getDouble(2)),true,30);
                    movement.appendData(new DataPoint(Integer.valueOf(id),crs.getDouble(3)),true,30);
                    crs.moveToNext();
                }
            }
        }

        graph1.addSeries(light);
        graph2.addSeries(sound);
        graph3.addSeries(movement);
        
    }


    public boolean checkPermission(){

        int result = ContextCompat.checkSelfPermission(getApplicationContext(),
                RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED ;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new
                String[]{RECORD_AUDIO}, 1);
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

        if(!playPauseState){
            generateNotification(this.getApplicationContext(),
                    "L'applicazione sta continuando ad acquisire dati in background!");
            moveTaskToBack(true);
        }else{
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        if(!playPauseState){
            generateNotification(this.getApplicationContext(),
                    "L'applicazione sta continuando ad acquisire dati in background!");
        }
        super.onPause();
    }



    private static void generateNotification(Context context, String message){

        int mNotificationId = 001;

        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentIntent(intent)
                .setPriority(5) //private static final PRIORITY_HIGH = 5;
                .setContentText(message)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_VIBRATE);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(001, mBuilder.build());
    }


    public void deleteNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(001);
    }
}
