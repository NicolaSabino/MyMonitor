package com.nicola.monitor_10;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

/**
 * Created by nicola on 21/11/16.
 */

public class MessageHelper {

    public static void toast(Context c, String message){
        Toast toast = Toast.makeText(c,message,Toast.LENGTH_SHORT);
        toast.show();
    }

    public static void log(String tag,String message){
        Log.i(tag,message);
    }

    public static void snak(View view, String message){

        Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();

    }
}
