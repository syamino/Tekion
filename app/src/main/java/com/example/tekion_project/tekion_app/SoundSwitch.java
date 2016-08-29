
package com.example.tekion_project.tekion_app;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

/**
 * Created by fuji_16 on 2016/08/22.
 */
public class SoundSwitch implements Runnable {

    // ボリューム感知リスナー
    private OnReachedVolumeListener mListener;

    // 録音中フラグ
    private boolean isRecoding = false;

    // サンプリングレート
    private static final int SAMPLE_RATE = 8000;//80.0KHz

    // ボーダー音量
    private short mBorderVolume = 100;

    static short max = 0;
    static double ave = 0;
    static double average = 0;//最終的に画面に表示する値
    private int ave_count = 0;
    private double temp;
    private double baseValue = 12.0;
    // ボーダー音量をセット
    public void setBorderVolume(short volume) {
        mBorderVolume = volume;
    }

    // ボーダー音量を取得
    public short getBorderVolume() {
        return mBorderVolume;
    }

    // 録音を停止
    public boolean stop() {
        isRecoding = false;
        return isRecoding;
    }
    public boolean start() {
        isRecoding = true;
        return isRecoding;
    }


    // OnReachedVolumeListenerをセット
    public void setOnVolumeReachedListener(OnReachedVolumeListener listener) {
        mListener = listener;
    }
    // ボーダー音量を検知した時のためのリスナー
    public interface OnReachedVolumeListener {//ボーダー音量を超える音量を検知した時に呼び出されるメソッドです。

        void onReachedVolume(short volume);

    }
    // スレッド開始（録音を開始）
    public void run() {
        temp = 0;
        android.os.Process.setThreadPriority(
                android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        int bufferSize = 10*AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        AudioRecord audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize);
        short[] buffer = new short[bufferSize];
        audioRecord.startRecording();
        while(isRecoding) {
            audioRecord.read(buffer, 0, bufferSize);
            //short max = 0;
            ave_count =0;

                for (int i = 0; i < bufferSize; i++) {// 最大音量を計算
                    if(buffer[i]<baseValue){

                    }else {
                        ave_count++;
                        ave = 20.0 * Math.log10((Math.abs(buffer[i])) / baseValue)+ave;

                       // Log.d("ave", "ave=" + ave);デバック用
                       // Log.d("abs", "abs=" + buffer[i]);デバック用
                       // max = (short) Math.max(max, buffer[i]);// 最大音量がボーダーを超えていたら
                    }
                }

            average =(ave/ ave_count) /3   +    temp*2/3;//前回までtemp 騒音量average　
            temp =average;

            Log.d("ave","average="+average );

                ave = 0;
                max = 0;
                ave_count=0;

        }
        audioRecord.stop();
        audioRecord.release();
    }
}