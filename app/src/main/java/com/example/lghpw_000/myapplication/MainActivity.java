package com.example.lghpw_000.myapplication;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SoundLevelMeter.SoundLevelMeterListener{
    private AudioManager audio;
    private int musicMaxVol;
    private int musicVol;
    private TextView curMusicVolTex;
    private TextView textView3;
    private SoundLevelMeter soundLevelMeter;
    static boolean thread_open =true;
    //private double testh;

    static Handler mainHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        soundLevelMeter = new SoundLevelMeter();
        TextView mvol = (TextView) findViewById(R.id.mvol);
        curMusicVolTex = (TextView) findViewById(R.id.crvol);
        textView3 = (TextView) findViewById(R.id.crdb);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        musicMaxVol = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        musicVol = audio.getStreamVolume(AudioManager.STREAM_MUSIC);

        mvol.setText(String.valueOf("変更された音量について"+musicMaxVol));
        curMusicVolTex.setText(String.valueOf(musicVol));
        textView3.setText("現在の騒音量を表示");

        mainHandler = new Handler();

        soundLevelMeter.setListener(this);
        //(new Thread(soundLevelMeter)).start();

        findViewById(R.id.start_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(thread_open) {
                    Log.d("thraedの数","!!!!!!!!");
                    (new Thread(soundLevelMeter)).start();
                    thread_open=false;
                    Log.d("スタートです","thread="+thread_open);
                    musicVol = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
                    curMusicVolTex.setText(String.valueOf(musicVol));
                }

            }
        });
        findViewById(R.id.stop_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(thread_open != true) {
                    soundLevelMeter.stop();
                    //soundLevelMeter.interrupt();
                    thread_open=true;
                    Log.d("ストップです","thread="+thread_open);
                    musicVol = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
                    curMusicVolTex.setText(String.valueOf(musicVol));
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        soundLevelMeter.stop();
    }

    public void onMeasure(double db,double db2){

        //textView3.setText(String.valueOf(db));
        textView3.setText("騒音量:"+ (int)Math.ceil(SoundLevelMeter.average)+"");

        //Log.d("MAです","音量判定");
        if(db2 >= 30.5){
            audio.setStreamVolume(AudioManager.STREAM_MUSIC,5,0);
            musicVol = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
            curMusicVolTex.setText(String.valueOf("適正音量"+musicVol+"に変更されました"));
        }else if(db2 >= 25) {
            audio.setStreamVolume(AudioManager.STREAM_MUSIC,4,0);
            musicVol = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
            curMusicVolTex.setText(String.valueOf("適正音量"+musicVol+"に変更されました"));
        }else {
            audio.setStreamVolume(AudioManager.STREAM_MUSIC,3,0);
            musicVol = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
            curMusicVolTex.setText(String.valueOf("適正音量"+musicVol+"に変更されました"));
        }

      //  testh = db;

    }


}
