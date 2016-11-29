package com.nicola.monitor_10;

import android.content.Context;
import android.media.MediaRecorder;

import java.io.IOException;

/**
 * Created by nicola on 29/11/16.
 */

public class MyMicrophoneSensor {

    private MediaRecorder recorder;

    public MyMicrophoneSensor(Context c){

        //todo controllo sui permessi del microfono

        if(recorder != null){
            recorder.release();
        }

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        String fileName = c.getFilesDir()+"/audio.m4a";
        recorder.setOutputFile(fileName);
        MessageHelper.log("MicrophoneS","Mile:"  + fileName);

    }

    /**
     * registro il listener sul sensore
     * @return true se la registrazione è andata a buon fine
     */
    public boolean registerMicrophoneSensor(){

        try {
            recorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        recorder.start();
       return true;
    }

    /**
     * Scollego il listener al sensore
     * @return
     */
    public boolean unregisterMicrophoneSensor() {

        if(recorder != null){
            recorder.stop();
            recorder.release();
        }
        return true;
    }

    public float getValue(){
        return recorder.getMaxAmplitude();
    }


}
