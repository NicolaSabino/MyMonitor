package com.nicola.monitor_10;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by nicola on 27/10/16.
 */

/**
 * Classe che costruisce il database se assente
 */
public class DBhelper extends SQLiteOpenHelper {

    public static final String DBNAME="Monitoraggio";

    public DBhelper(Context context) {
        super(context, DBNAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String q="CREATE TABLE "+
                DatabaseStrings.TBL_NAME        + " ( " +
                DatabaseStrings.FIELD_ID        + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                DatabaseStrings.FIELD_LIGHT     + " DOUBLE,"    +
                DatabaseStrings.FIELD_SOUND     + " DOUBLE,"    +
                DatabaseStrings.FIELD_MOVEMENT  + " DOUBLE,"    +
                DatabaseStrings.FIELD_LOCKED    + " BOOLEAN,"   +
                DatabaseStrings.FIELD_CHARGING  + " BOOLEAN,"   +
                DatabaseStrings.FIELD_DATE      + " VARCHAR,"   +
                DatabaseStrings.FIELD_TIME      + " VARCHAR )"
                ;
        db.execSQL(q);

        Log.i("DBHELPER","Creazione del db con la tabella " + DatabaseStrings.TBL_NAME );

        String q2="CREATE TABLE "+
                DatabaseStrings.TBL_NAME_2      + " ( " +
                DatabaseStrings.FIELD_ID        + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                DatabaseStrings.FIELD_STATE     + " BOOLEAN,"   +
                DatabaseStrings.FIELD_DATE      + " VARCHAR,"   +
                DatabaseStrings.FIELD_TIME      + " VARCHAR )"
                ;
        db.execSQL(q2);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {  }



}
