package com.nicola.monitor_10;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
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

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    private boolean stato;
    private FloatingActionButton fab;
    boolean playPauseState;
    private Intent servizio;
    private MenuView.ItemView s;
    private DbManager db;

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
        MessageHelper.log("ON_CREATE", "popolo la tabella");


        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReciver,new IntentFilter("evento-popola-tabella"));
    }

    private BroadcastReceiver mMessageReciver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //ogni volta che ricevo un intent popolo la tabella
            popolaTabella();
        }
    };

    @Override
    protected void onPostResume() {
        super.onPostResume();
        popolaTabella();
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

        //cambio lo stato dell'utente
        stato = !stato;

        //db.changeState(stato);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.s = (MenuView.ItemView) findViewById(R.id.stato);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        if (id == R.id.stato){
            if(checkPermission()){ //se ho i permessi per usare il microfono faccio partire il task in background
                startStop();
                return true;
            }else{ //altrimenti
                requestPermission();
                return true;
            }
        }

        if(id == R.id.refresh){
            popolaTabella();
            return true;
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
                View v = getLayoutInflater().inflate(R.layout.row_table,null);
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

                //stampo le informazioni nella riga della tabella
                TextView elem0 = (TextView) v.findViewById(R.id.idContent);
                TextView elem1 = (TextView) v.findViewById(R.id.lightContent);
                TextView elem2 = (TextView) v.findViewById(R.id.movContent);
                TextView elem3 = (TextView) v.findViewById(R.id.soundContent);
                TextView elem4 = (TextView) v.findViewById(R.id.chargingContent);
                TextView elem5 = (TextView) v.findViewById(R.id.lockContent);

                elem0.setText(id);
                elem1.setText(light);
                elem2.setText(sound);
                elem3.setText(movement);
                elem4.setText(charging);
                elem5.setText(locked);


            }
        };


        ListView listaValori = (ListView) findViewById(R.id.listaValori);
        listaValori.setAdapter(adapter);


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
                        MessageHelper.toast(this,"Permesso garantito!");
                    } else {
                        MessageHelper.toast(this,"Permesso negato");
                    }

                break;
        }
    }
}
