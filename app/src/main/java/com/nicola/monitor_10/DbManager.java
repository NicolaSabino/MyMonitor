package com.nicola.monitor_10;

/**
 * Created by nicola on 27/10/16.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class DbManager {

    private DBhelper dbhelper;
    private Context context;

    public DbManager(Context ctx) {
        context = ctx;
        dbhelper = new DBhelper(context);
    }

    public void save(String lum, String mov, String s, String c, String l) {
        SQLiteDatabase db = dbhelper.getWritableDatabase();


        ContentValues cv = new ContentValues();
        cv.put(DatabaseStrings.FIELD_LIGHT, lum);
        cv.put(DatabaseStrings.FIELD_MOVEMENT, mov);
        cv.put(DatabaseStrings.FIELD_SOUND, s);
        cv.put(DatabaseStrings.FIELD_CHARGING, c);
        cv.put(DatabaseStrings.FIELD_LOCKED, l);
        cv.put(DatabaseStrings.FIELD_DATE,getDate());
        cv.put(DatabaseStrings.FIELD_TIME, getTime());


        try {

            /*db.execSQL("insert into " + DatabaseStrings.TBL_NAME + "( " +
                    DatabaseStrings.FIELD_LIGHT + " , " +
                    DatabaseStrings.FIELD_SOUND+ " , " +
                    DatabaseStrings.FIELD_MOVEMENT + " , " +
                    DatabaseStrings.FIELD_LOCKED + " , " +
                    DatabaseStrings.FIELD_CHARGING + " , " +
                    DatabaseStrings.FIELD_DATE + " , " +
                    DatabaseStrings.FIELD_TIME + " ) values( "
                    + lum  + " , "
                    + s  + " , "
                    + mov    + " , "
                    + l    + " , "
                    + c    + " , "
                    + " CURDATE() "
                    + ", CURTIME() )");*/
            db.insert(DatabaseStrings.TBL_NAME, null, cv);
            Log.i("DBMANAGER", "Nuovo salvataggio avvenuto con successo");
        } catch (SQLiteException sqle) {
            sqle.printStackTrace();
        }

    }


    public boolean deleteTables() {
        SQLiteDatabase db = dbhelper.getWritableDatabase();
        try {
            db.execSQL("delete from " + DatabaseStrings.TBL_NAME);
            db.execSQL("delete from " + DatabaseStrings.TBL_NAME_2);
            return true;
        } catch (SQLiteException sqle) {
            sqle.printStackTrace();
            return false;
        }

    }


    public Cursor query(){
        Cursor crs = null;
        try {
            SQLiteDatabase db = dbhelper.getReadableDatabase();
            String myQuery = "SELECT * FROM " + DatabaseStrings.TBL_NAME + " order by " + DatabaseStrings.FIELD_ID + " DESC ";
            crs = db.rawQuery(myQuery, null);
        } catch (SQLiteException sqle) {
            sqle.printStackTrace();
        }
        return crs;
    }

    public Cursor reverseQuery(){
        Cursor crs = null;
        try {
            SQLiteDatabase db = dbhelper.getReadableDatabase();
            String myQuery = "SELECT * FROM " + DatabaseStrings.TBL_NAME + " order by " + DatabaseStrings.FIELD_ID;
            crs = db.rawQuery(myQuery, null);
        } catch (SQLiteException sqle) {
            sqle.printStackTrace();
        }
        return crs;
    }

    public Cursor lastValue(){
        Cursor c = null;
        try {
            SQLiteDatabase db = dbhelper.getReadableDatabase();
            String myQuery = "SELECT * FROM " + DatabaseStrings.TBL_NAME + " order by " + DatabaseStrings.FIELD_ID + " DESC limit 1";
            c = db.rawQuery(myQuery, null);
        } catch (SQLiteException sqle){
            sqle.printStackTrace();
        }
        return c;
    }

    /*public void changeState(boolean state) {

        SQLiteDatabase db = dbhelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(DatabaseStrings.FIELD_STATE, state);
        cv.put(DatabaseStrings.FIELD_TIME, "");

        try {
            db.insert(DatabaseStrings.TBL_NAME_2, null, cv);
            Log.i("DBMANAGER", "Cambio di stato salvato con successo");
        } catch (SQLiteException sqle) {
            sqle.printStackTrace();
        }
    }*/

    /*private String getTime() {
        java.util.Calendar c = java.util.Calendar.getInstance();
        java.util.Date d = c.getTime().
        return d.toString();
    }*/


    /**
     * Metodo che serve a condividere il file sql esternamente
     */
    public void shareDB(){
        //todo da implementare
    }

    /**
     * Metodo che serve a condividere il file contenente le informazioni del db esternamente
     */
    public void sharingFile(){
        //  CONFIGURAZIONE DELLE VARIABILI
        String filename = "Data.csv";
        File file = new File(context.getFilesDir(), filename);
        FileOutputStream fileOutputStream;
        String information = ""; // stringa che verrà convertita nei byte da scrivere sul file


        //QUERY

        Cursor c  = query();

        for (int i = 0; i<c.getCount();i++){

            //prendo le informazioni dalla query
            String id           = c.getString(c.getColumnIndex(DatabaseStrings.FIELD_ID));
            String light        = c.getString(c.getColumnIndex(DatabaseStrings.FIELD_LIGHT));
            String movement     = c.getString(c.getColumnIndex(DatabaseStrings.FIELD_MOVEMENT));
            String sound        = c.getString(c.getColumnIndex(DatabaseStrings.FIELD_SOUND));
            String charging     = c.getString(c.getColumnIndex(DatabaseStrings.FIELD_CHARGING));
            String locked       = c.getString(c.getColumnIndex(DatabaseStrings.FIELD_LOCKED));
            String date         = c.getString(c.getColumnIndex(DatabaseStrings.FIELD_DATE));
            String time         = c.getString(c.getColumnIndex(DatabaseStrings.FIELD_TIME));

            //faccio un apend sulla stringa information
            information += id +
                    ";" + light +
                    ";" + movement +
                    ";" + sound +
                    ";" + charging +
                    ";" + locked +
                    ";" + time + "\n";

            //scorro al prossimo elemento
            c.moveToNext();
        }

        //BLOCCO SCRITTURA
        try {
            fileOutputStream = context.openFileOutput(filename,Context.MODE_APPEND);
            fileOutputStream.write(information.getBytes());
            fileOutputStream.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /*
        Bisogna liberare memoria
     */
    public void deleteFile(){

    }

    public String getDate(){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String formattedDate = df.format(c.getTime());
        return formattedDate;
    }

    public String getTime(){

        Calendar c = Calendar.getInstance();
        System.out.println("Current time => "+c.getTime());
        SimpleDateFormat df = new SimpleDateFormat("HH:mm");
        String formattedTime = df.format(c.getTime());
        return formattedTime;
    }

}
