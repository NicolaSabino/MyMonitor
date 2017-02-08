package com.nicola.monitor_10;

/**
 * Created by nicola on 27/10/16.
 */

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;


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


    public Cursor reverseQuery2(){
        Cursor crs = null;
        try {
            SQLiteDatabase db = dbhelper.getReadableDatabase();
            String myQuery = "SELECT * FROM " + DatabaseStrings.TBL_NAME_2 + " order by " + DatabaseStrings.FIELD_ID;
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


    public void changeState(boolean state) {

        SQLiteDatabase db = dbhelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(DatabaseStrings.FIELD_STATE, state);
        cv.put(DatabaseStrings.FIELD_TIME, getTime());
        cv.put(DatabaseStrings.FIELD_DATE, getDate());

        try {
            db.insert(DatabaseStrings.TBL_NAME_2, null, cv);
            Log.i("DBMANAGER", "Cambio di stato salvato con successo");
        } catch (SQLiteException sqle) {
            sqle.printStackTrace();
        }
    }


    public File esportaDB() {

        File exportDir = new File(Environment.getExternalStorageDirectory(), "");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        File file = new File(exportDir, getDate() + ".csv");

        try {
            file.createNewFile();
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
            SQLiteDatabase db = dbhelper.getReadableDatabase();
            Cursor curCSV = reverseQuery();
            csvWrite.writeNext(curCSV.getColumnNames());
            while (curCSV.moveToNext()) {

                String arrStr[] = {
                        curCSV.getString(curCSV.getColumnIndex(DatabaseStrings.FIELD_ID)),
                        curCSV.getString(curCSV.getColumnIndex(DatabaseStrings.FIELD_LIGHT)),
                        curCSV.getString(curCSV.getColumnIndex(DatabaseStrings.FIELD_SOUND)),
                        curCSV.getString(curCSV.getColumnIndex(DatabaseStrings.FIELD_MOVEMENT)),
                        curCSV.getString(curCSV.getColumnIndex(DatabaseStrings.FIELD_LOCKED)),
                        curCSV.getString(curCSV.getColumnIndex(DatabaseStrings.FIELD_CHARGING)),
                        curCSV.getString(curCSV.getColumnIndex(DatabaseStrings.FIELD_DATE)),
                        curCSV.getString(curCSV.getColumnIndex(DatabaseStrings.FIELD_TIME))
                };
                csvWrite.writeNext(arrStr);
            }
            csvWrite.close();
            curCSV.close();
            MessageHelper.toast(context,file.getPath());
        } catch (Exception sqlEx) {
            sqlEx.printStackTrace();
        }

        return file;
    }

    public File esportaDB2() {

        File exportDir = new File(Environment.getExternalStorageDirectory(), "");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        File file = new File(exportDir, getDate() + "_check.csv");

        try {
            file.createNewFile();
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
            SQLiteDatabase db = dbhelper.getReadableDatabase();
            Cursor curCSV = reverseQuery2();
            csvWrite.writeNext(curCSV.getColumnNames());
            while (curCSV.moveToNext()) {

                String arrStr[] = {
                        curCSV.getString(curCSV.getColumnIndex(DatabaseStrings.FIELD_ID)),
                        curCSV.getString(curCSV.getColumnIndex(DatabaseStrings.FIELD_STATE)),
                        curCSV.getString(curCSV.getColumnIndex(DatabaseStrings.FIELD_DATE)),
                        curCSV.getString(curCSV.getColumnIndex(DatabaseStrings.FIELD_TIME))
                };
                csvWrite.writeNext(arrStr);
            }
            csvWrite.close();
            curCSV.close();
            MessageHelper.toast(context,file.getPath());
        } catch (Exception sqlEx) {
            sqlEx.printStackTrace();
        }

        return file;
    }

    public void sharing(){

        Intent sharingIntent = new Intent();
        sharingIntent.setAction(Intent.ACTION_SEND);
        sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(esportaDB()));
        sharingIntent.setType("text/csv");
        sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(sharingIntent, "Scegli come condividere il file"));
    }

    public void sharing2(){

        Intent sharingIntent = new Intent();
        sharingIntent.setAction(Intent.ACTION_SEND);
        sharingIntent.putExtra(Intent.EXTRA_STREAM,Uri.fromFile(esportaDB2()));
        sharingIntent.setType("text/csv");
        sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(sharingIntent, "Scegli come condividere il file"));
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
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        String formattedTime = df.format(c.getTime());
        return formattedTime;
    }



}
