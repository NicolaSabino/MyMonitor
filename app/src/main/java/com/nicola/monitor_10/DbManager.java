package com.nicola.monitor_10;

/**
 * Created by nicola on 27/10/16.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;



public class DbManager {

    private DBhelper dbhelper;

    public DbManager(Context ctx)
    {
        dbhelper=new DBhelper(ctx);
    }

    public void save(String lum, String mov, String s, String c, String l)
    {
        SQLiteDatabase db=dbhelper.getWritableDatabase();

        ContentValues cv=new ContentValues();
        cv.put(DatabaseStrings.FIELD_LIGHT, lum);
        cv.put(DatabaseStrings.FIELD_MOVEMENT, mov);
        cv.put(DatabaseStrings.FIELD_SOUND, s);
        cv.put(DatabaseStrings.FIELD_CHARGING,c);
        cv.put(DatabaseStrings.FIELD_LOCKED,l);
        cv.put(DatabaseStrings.FIELD_TIME,this.getTime());

        try
        {
            db.insert(DatabaseStrings.TBL_NAME, null,cv);
            Log.i("DBMANAGER","Nuovo salvataggio avvenuto con successo");
        }
        catch (SQLiteException sqle)
        {
            sqle.printStackTrace();
        }

    }


    public boolean deleteTables()
    {
        SQLiteDatabase db=dbhelper.getWritableDatabase();
        try
        {
            db.execSQL("delete from "+ DatabaseStrings.TBL_NAME);
            db.execSQL("delete from "+ DatabaseStrings.TBL_NAME_2);
            return true;
        }
        catch (SQLiteException sqle)
        {
            sqle.printStackTrace();
            return false;
        }

    }


    public Cursor query() {
        Cursor crs=null;
        try
        {
            SQLiteDatabase db=dbhelper.getReadableDatabase();
            String myQuery = "SELECT * FROM " + DatabaseStrings.TBL_NAME + " order by " + DatabaseStrings.FIELD_ID  + " DESC ";
            crs=db.rawQuery(myQuery,null);
        }
        catch(SQLiteException sqle)
        {
            return null;
        }
        return crs;
    }

    public void changeState(boolean state){

        SQLiteDatabase db=dbhelper.getWritableDatabase();

        ContentValues cv=new ContentValues();
        cv.put(DatabaseStrings.FIELD_STATE, state);
        cv.put(DatabaseStrings.FIELD_TIME,this.getTime());

        try
        {
            db.insert(DatabaseStrings.TBL_NAME_2, null,cv);
            Log.i("DBMANAGER","Cambio di stato salvato con successo");
        }
        catch (SQLiteException sqle)
        {
            sqle.printStackTrace();
        }
    }

    private String getTime(){
        java.util.Calendar c = java.util.Calendar.getInstance();
        java.util.Date d = c.getTime();
        return d.toString();
    }

}