package com.example.tekion_project.tekion_app;


        import android.app.Service;
        import android.content.Context;
        import android.content.Intent;
        import android.media.AudioManager;
        import android.media.MediaPlayer;
        import android.os.Binder;
        import android.os.Bundle;
        import android.os.Handler;
        import android.os.IBinder;

        import java.io.IOException;
        import java.lang.annotation.IncompleteAnnotationException;

public class MusicVolume extends Service {

    private MediaPlayer player = new MediaPlayer();
    private AudioManager manager;

    private final Handler handler = new Handler();
    int dB = 80;

    private final Runnable runnable = new Runnable(){
        @Override
        public void run(){
            dB = 80;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        handler.postDelayed(runnable, 30000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        /** Called when the activity is first created. */
        try {

            // 最大音量値を取得
            int vol = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC); // Level 0-15 (Nexus7)
            // 音量を設定
            /** dBで分類バージョン */
            if(dB < 70) {
                manager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (vol / 4), 0);
            }else if(dB >= 70){
                manager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (vol / 2), 0);
            }else{
                manager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (vol / 3), 0);
            }

            player.prepare();
            player.start();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    public class MusicVolumeBinder extends Binder {
        MusicVolume getService() { return MusicVolume.this; }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}