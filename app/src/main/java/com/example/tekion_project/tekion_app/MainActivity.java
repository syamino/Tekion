package com.example.tekion_project.tekion_app;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import java.io.IOException;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;


public class MainActivity extends Activity implements View.OnClickListener{
    private SoundSwitch mSoundSwitch;
    private Handler mHandler = new Handler();
    private TextView maintext;
    //private int flag = 0;
    private Button start_button;
    private Button stop_button;
    private boolean rokuon;

    MusicVolume musicVolume = null;
    Intent serviceIntent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        short max =0;
        super.onCreate(savedInstanceState);
        mTimer.schedule(mTimerTask,0,500);


        setContentView(R.layout.activity_main);
        serviceIntent = new Intent(this, MusicVolume.class);
        startService(serviceIntent);

        start_button = (Button)findViewById(R.id.start_button);
        start_button.setOnClickListener(this);

        stop_button = (Button)findViewById(R.id.stop_button);
        stop_button.setOnClickListener(this);


    }
    @Override
    public void onClick(View v) {//ボタンが押された処理すべて
        TextView textView=(TextView)findViewById(R.id.mainText);
        TextView textView2=(TextView)findViewById(R.id.SouonText);//騒音量表示
        //TextView textView3=(TextView)findViewById(R.id.debug);//デバック用

        switch(v.getId()) {
            case R.id.start_button:    //開始ボタン(id=startbutton)が押された時
                if(rokuon != true) {
                    onResume();
                }
                textView.setText("適音開始");
               // textView2.setText("騒音量:"+SoundSwitch.max+"");
                textView2.setText("騒音量:"+ (int)Math.ceil(SoundSwitch.average)+"");
                //textView3.setText(String.valueOf(rokuon));

                break;
            case R.id.stop_button:    //終了ボタン(id=stopbutton)が押された時
                if (rokuon == true) {
                   // onPause();騒音計算を終える
                }
                textView.setText("適音終了");
               // textView2.setText("騒音量:"+SoundSwitch.max+"");
                textView2.setText("騒音量:"+ (int)Math.ceil(SoundSwitch.average)+"");
                // textView3.setText(String.valueOf(flag));
                break;

        }
    }
    @Override
    public void onResume() {
        super.onResume();
        mSoundSwitch = new SoundSwitch();// リスナーを登録して音を感知できるように
        rokuon = mSoundSwitch.start();
        //mTimer.schedule(mTimerTask,0,500);
        mSoundSwitch.setOnVolumeReachedListener(
                new SoundSwitch.OnReachedVolumeListener() {// 音を感知したら呼び出される
                    public void onReachedVolume(short volume)
                    {//別スレッドからUIスレッドに要求するのでHandler.postでエラー回避
                        mHandler.post(new Runnable()
                        {//Runnableに入った要求を順番にLoopでrunを呼び出し処理
                            public void run() {
                                //

                                //ここにもともと画面の色を変えるプログラムが一行あった
                            }
                        });
                    }
                }
        );
        new Thread(mSoundSwitch).start();// 別スレッドとしてSoundSwitchを開始（録音を開始）
    }
    @Override
    public void onPause() {

        //Activityの状態がonPauseの時の処理
        super.onPause();//superクラスのonPauseを呼び出す
        rokuon= mSoundSwitch.stop();// 録音を停止
    }

    public class MainTimeTask extends TimerTask{
        @Override
        public void run(){
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    TextView textView5=(TextView)findViewById(R.id.SouonText);//デバック用
                    //textView5.setText("騒音量:"+SoundSwitch.max+"");
                    textView5.setText("騒音量:"+ (int)Math.ceil(SoundSwitch.average)+"");

                }
            });
        }
    }
    Timer mTimer= new Timer();
    TimerTask mTimerTask=new MainTimeTask();
    Handler getmHandler = new Handler();

    private ServiceConnection serviceConnection = new ServiceConnection(){
        public void onServiceConnected(ComponentName className, IBinder service){
            musicVolume = ((MusicVolume.MusicVolumeBinder)service).getService();
        }
        public void onServiceDisconnected(ComponentName name){
            //TODO Auto-generated method stub
            musicVolume = null;
        }
    };

}