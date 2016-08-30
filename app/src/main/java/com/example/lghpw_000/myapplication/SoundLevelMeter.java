package com.example.lghpw_000.myapplication;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

/**
 * Created by lghpw_000 on 2016/08/28.
 */
public class SoundLevelMeter implements Runnable {
    private static final int SAMPLE_RATE = 8000;

    private int bufferSize;
    private AudioRecord audioRecord;
    private boolean isRecording;
    private boolean isPausing;
    private double baseValue;

    public double test;
    public double tempo;
    public double avgdb;
    private short avg;
    //騒音計算のために必要な関数
    //static short max = 0;
    static double ave = 0;
    static double average = 0;//最終的に画面に表示する値
    private int ave_count = 0;
    private double temp;


    public interface SoundLevelMeterListener {
        void onMeasure(double db,double db2);
    }

    private SoundLevelMeterListener listener;

    public SoundLevelMeter() {

        baseValue = 12.0;
        test = 0;
        tempo = 0;

    }
    public void setListener(SoundLevelMeterListener l) {
        listener = l;
    }

    public void run() {
       if(MainActivity.thread_open!=true) {
           temp = 0;

           bufferSize = 10*AudioRecord.getMinBufferSize(SAMPLE_RATE,
                   AudioFormat.CHANNEL_CONFIGURATION_MONO,
                   AudioFormat.ENCODING_PCM_16BIT);
           audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                   SAMPLE_RATE, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                   AudioFormat.ENCODING_PCM_16BIT, bufferSize*2);
           start();
           //Log.d("SLMdです", "スタート");
           //Log.d("SLMdです", "isrecording"+isRecording);
           android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
           audioRecord.startRecording();
           short[] buffer = new short[bufferSize];
           while (isRecording) {
               //Log.d("SLMdです", "isrecording");
               int read = audioRecord.read(buffer, 0, bufferSize);
               if (read < 0) {
               }
               int maxValue = 0;
               long sum = 0;
               ave_count =0;
               ave = 0;
               for (int i = 0; i < read; i++) {
                   if(Math.abs(buffer[i])<baseValue){

                   }else {
                       // maxValue = Math.max(maxValue, buffer[i]);
                       //sum += Math.abs(buffer[i]);
                       ave_count++;
                       ave = 20.0 * Math.log10((Math.abs(buffer[i])) / baseValue) + ave;
                   }

               }

               Log.d("debug","騒音量="+average );
               average =(ave/ ave_count) /3   +    temp*2/3;//前回までtemp 騒音量average　
               temp =average;

               //max = 0;
               //avg = (short) (sum / bufferSize);
               //test = 20.0 * Math.log10(maxValue / baseValue);
               //avgdb = 20.0 * Math.log10(avg / baseValue);
               //tempo = tempo + 1;

               /*
               try {
                   Thread.sleep(5000);
               } catch (InterruptedException e) {
                   // TODO Auto-generated catch block
               }*/

               MainActivity.mainHandler.post(new Runnable() {
                   @Override
                   public void run() {
                       listener.onMeasure(average, average);
                   }

               });

           }

           Log.d("audioRecord.stop();の上", "thread" + MainActivity.thread_open);
           audioRecord.stop();
           Log.d("audioRecord.stop();の↓", "thread" + MainActivity.thread_open);
           audioRecord.release();
           if(MainActivity.thread_open==true){
               stop();
           }
       }
    }
    public void start() {
        isRecording = true;
    }

    public void stop() {
        isRecording = false;
    }

    public void pause() {
        if (!isPausing)
            audioRecord.stop();
        isPausing = true;
    }
}
