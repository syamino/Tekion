package com.example.tekion_project.tekion_app;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import java.util.ArrayList;
import android.content.SharedPreferences;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SoundLevelMeter.SoundLevelMeterListener{
    private AudioManager audio;
    private int musicMaxVol;
    private int musicVol;
    private TextView curMusicVolTex;
    private TextView textView3;
    private TextView ContP;
    private SoundLevelMeter soundLevelMeter;
    static boolean thread_open =true;
    //private double testh;

    static Handler mainHandler;
    static MainActivity mainActInst;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        soundLevelMeter = new SoundLevelMeter();
        TextView mvol = (TextView) findViewById(R.id.mvol);
        curMusicVolTex = (TextView) findViewById(R.id.crvol);
        //ContP =(TextView) findViewById(R.id.ContP);
        textView3 = (TextView) findViewById(R.id.crdb);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        musicMaxVol = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        musicVol = audio.getStreamVolume(AudioManager.STREAM_MUSIC);


        mvol.setText(String.valueOf(""+musicMaxVol));
        curMusicVolTex.setText(String.valueOf(musicVol));
        textView3.setText("現在の騒音量を表示");

        mainHandler = new Handler();
        mainActInst = this;


        soundLevelMeter.setListener(this);
        //(new Thread(soundLevelMeter)).start();

        findViewById(R.id.start_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(thread_open) {
                    //Log.d("thraedの数","!!!!!!!!");
                    (new Thread(soundLevelMeter)).start();
                    thread_open=false;
                    //Log.d("スタートです","thread="+thread_open);
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
                    //Log.d("ストップです","thread="+thread_open);
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
        TextView textView = (TextView) findViewById(R.id.mvol);
        AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        //textView3.setText(String.valueOf(db));
        textView3.setText("騒音量:"+ (int)Math.ceil(SoundLevelMeter.average)+"");
        textView.setText(""+audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)+"" );


        int vol_point= calculateSuitableVolume(db2);
        Log.d("(騒音量/CPより求めた音量)", "" + (int)Math.ceil(SoundLevelMeter.average) +","+vol_point);
        audio.setStreamVolume(AudioManager.STREAM_MUSIC,vol_point,0);
        musicVol = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        curMusicVolTex.setText(String.valueOf(""+musicVol+""));

       // ContP.setText(readControlPoints().toString());//コントロールポイントデバック用

    }



    @Override
    public boolean dispatchKeyEvent(KeyEvent event){
        AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        TextView textView = (TextView) findViewById(R.id.mvol);
        //TextView textView2 = (TextView) findViewById(R.id.sai);

        if(event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP){
            //textView2.setText("現在の音量はupです");
            textView.setText(""+audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)+"" );
            //if(thread_open)
            adjustSuitableVolume(SoundLevelMeter.average,1);
            Log.d("コントロールポイントを変更", "騒音量/変更度/端末音量:" + (int)Math.ceil(SoundLevelMeter.average) +",+1,"+audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
            Log.d("コントロールポイント",""+readControlPoints().toString());

        }

        if(event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN){
            //.setText("現在の音量はdownです");
            textView.setText(""+audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)+"" );
            //if(thread_open)
            adjustSuitableVolume(SoundLevelMeter.average,-1);
            Log.d("コントロールポイントを変更","騒音量/変更度/端末音量:" + (int)Math.ceil(SoundLevelMeter.average) +",-1,"+audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
            Log.d("コントロールポイント",""+readControlPoints().toString());
        }

        if(event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_MUTE){
            //textView2.setText("現在の音量はミュートです");
            //textView.setText("現在の音量は"+audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)+"です!" );
        }
        return super.dispatchKeyEvent(event);
    }


    // コントロールポイントを表すクラス
    class ControlPoint{
        // 騒音量と適正音量を保持する
        public double noiseVolume;
        public double suitableVolume;

        public ControlPoint(double noiseVolume, double suitableVolume){
            this.noiseVolume = noiseVolume;
            this.suitableVolume = suitableVolume;
        }

        public String toString(){
            return "("+this.noiseVolume+", "+this.suitableVolume+")";
        }
    }

    // 騒音量を受け取って、適正音量を計算して返す
    public   int calculateSuitableVolume(double noiseVolume){

        // コントロールポイントをSharedPreferencesから読み込む
        ArrayList<ControlPoint> controlPoints = readControlPoints();

        // 線形補間結果を四捨五入して返す
        int suitableVolume = (int)Math.round(piecewiseLinearInterpolation(controlPoints, noiseVolume));
        return suitableVolume;
    }

    // 与えられた騒音量に対する適正音量をdeltaだけ上げる（deltaを負の数にすれば下がる）
// 内部的にはコントロールポイントの追加か修正のどちらか + 接近しすぎたコントロールポイントの削除を行う
    public   void adjustSuitableVolume(double noiseVolume, int delta){

        // コントロールポイントをSharedPreferencesから読み込む
        ArrayList<ControlPoint> controlPoints = readControlPoints();

        // 排他距離内のコントロールポイントを削除する前に適正音量を計算しておく
        // もしコントロールポイントを削除してから計算すると、結果が変わってしまう
        double suitableVolume = piecewiseLinearInterpolation(controlPoints, noiseVolume);

        // 排他距離内のコントロールポイントを削除する
        deleteTooCloseControlPoints(controlPoints, noiseVolume);

        // 新しいコントロールポイントのリストの内のインデックス
        int indexOfNewControlPoint = calculateIndexOfNewControlPoint(controlPoints, noiseVolume);

        // 変更後の適正音量
        double adjustedSuitableVolume = suitableVolume + delta;

        // コントロールポイントを追加
        controlPoints.add(indexOfNewControlPoint, new ControlPoint(noiseVolume, adjustedSuitableVolume));

        // 単調増加ルール（もしグラフが単調増加になっていなければ、違反しているコントロールポイントを削除する）
         monotoneIncreasingRule(controlPoints, adjustedSuitableVolume, indexOfNewControlPoint, delta);

        writeControlPoints(controlPoints);
    }

    // 文字列からコントロールポイントのリストへ変換する
// 例えば"[(10.0, 3.0), (35.0, 6.0)]"を受け取って変換する
// controlPoints.toString()の逆関数である
    public   ArrayList<ControlPoint> parseControlPoints(String string){
        ArrayList<ControlPoint> controlPoints = new ArrayList<ControlPoint>();

        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\((.*?),(.*?)\\)");
        java.util.regex.Matcher matcher = pattern.matcher(string);
        while(matcher.find()){
            double noiseVolume = Double.parseDouble(matcher.group(1));
            double suitableVolume = Double.parseDouble(matcher.group(2));
            controlPoints.add(new ControlPoint(noiseVolume, suitableVolume));
        }

        return controlPoints;
    }

    // コントロールポイントをSharedPreferencesから読み込む
    public   ArrayList<ControlPoint> readControlPoints(){
        SharedPreferences sharedPreferences = getSharedPreferences("Tekion", MODE_PRIVATE);
        String defaultControlPointsString = "[(10.0, 3.0), (35.0, 6.0)]";
        String controlPointsString = sharedPreferences.getString("controlPoints", defaultControlPointsString);
        return parseControlPoints(controlPointsString);
    }

    // コントロールポイントをSharedPreferencesに書き込む
    public   void writeControlPoints(ArrayList<ControlPoint> controlPoints){
        SharedPreferences sharedPreferences = getSharedPreferences("Tekion", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("controlPoints", controlPoints.toString());
        editor.commit();
    }
    // 排他距離内のコントロールポイントを削除
    public   void deleteTooCloseControlPoints(ArrayList<ControlPoint> controlPoints, double noiseVolume){
        // 排他距離の定数
        double EXCLUSION_DISTANCE = 3.0;

        for(int i=0; i < controlPoints.size(); ++i){
            double distance = Math.abs(controlPoints.get(i).noiseVolume - noiseVolume);
            if(distance <= EXCLUSION_DISTANCE){
                controlPoints.remove(i);
                --i;
            }
        }
    }

    // 2点間の線形補間
    public   double linearInterpolation(ControlPoint left, ControlPoint right, double noiseVolume){
        // このウェブサイトに掲載されている数式そのまま http://qiita.com/niusounds/items/c4af702b06582590c82e
        double x0 = left.noiseVolume;
        double y0 = left.suitableVolume;
        double x1 = right.noiseVolume;
        double y1 = right.suitableVolume;
        double x = noiseVolume;
        return y0 + (y1 - y0) * (x - x0) / (x1 - x0);
    }

    // コントロールポイントのリストと騒音量を受け取って、線形補間結果を返す
// calculateSuitableVolume関数と異なり、線形補間結果を四捨五入せず返す
    public   double piecewiseLinearInterpolation(ArrayList<ControlPoint> controlPoints, double noiseVolume){
        // 3つの場合に分けて処理する

        // ケース1 : 先頭のコントロールポイントより左側
        ControlPoint firstControlPoint = controlPoints.get(0);
        if(noiseVolume <= firstControlPoint.noiseVolume){
            // 水平化を採用する場合
            //return firstControlPoint.suitableVolume;
            // 半直線化を採用する場合
            return linearInterpolation(firstControlPoint, controlPoints.get(1), noiseVolume);
        }

        for(int i = 0; i < controlPoints.size() - 1; ++i){
            // ケース2 : 2つのコントロールポイントの間
            if(noiseVolume <= controlPoints.get(i+1).noiseVolume){
                // 2点間の線形補間を行う
                return linearInterpolation(controlPoints.get(i), controlPoints.get(i+1), noiseVolume);
            }
        }

        // ケース3 : 末尾のコントロールポイントより右側
        ControlPoint lastControlPoint = controlPoints.get(controlPoints.size() - 1);
        // 水平化を採用する場合
        //return lastControlPoint.suitableVolume;
        // 半直線化を採用する場合
        return linearInterpolation(controlPoints.get(controlPoints.size() - 2), lastControlPoint, noiseVolume);
    }

    // 新しく追加されるコントロールポイントのリストの内のインデックスを計算する
    public   int calculateIndexOfNewControlPoint(ArrayList<ControlPoint> controlPoints, double noiseVolume){
        for(int i = 0; i < controlPoints.size(); ++i){
            // 末尾のコントロールポイントより左側なら
            if(noiseVolume < controlPoints.get(i).noiseVolume){
                return i;
            }
        }

        // 末尾のコントロールポイントより右側なら
        return controlPoints.size();
    }

    // 単調増加ルール
    public   void monotoneIncreasingRule(ArrayList<ControlPoint> controlPoints, double suitableVolume, int indexOfNewControlPoint, int delta){
        // 適正音量を上げた場合
        if(0 < delta){
            // 右隣のコントロールポイントのインデックス
            int j = indexOfNewControlPoint + 1;

            while(true){
                // 右隣のコントロールポイントが存在しなければ終了
                boolean isValidIndex = j < controlPoints.size();
                if(isValidIndex == false){
                    break;
                }

                // 右隣のコントロールポイントが単調増加ならば（それ以降もずっと単調増加のはずなので）終了
                if(suitableVolume <= controlPoints.get(j).suitableVolume){
                    break;
                }

                // 右隣のコントロールポイントが存在して単調増加でないならば削除してループ
                controlPoints.remove(j);
            }
        }
        // 適正音量を下げた場合
        else if(delta < 0){
            while(true){
                // 左隣のコントロールポイントのインデックス
                int j = indexOfNewControlPoint - 1;

                // 左隣のコントロールポイントが存在しなければ終了
                boolean isValidIndex = 0 <= j;
                if(isValidIndex == false){
                    break;
                }

                // 左隣のコントロールポイントが単調増加ならば（それ以降もずっと単調増加のはずなので）終了
                if(controlPoints.get(j).suitableVolume <= suitableVolume){
                    break;
                }

                // 左隣のコントロールポイントが存在して単調増加でないならば削除してループ
                controlPoints.remove(j);
                --indexOfNewControlPoint;
            }
        }

        // コントロールポイントが1個になってしまったら2個にする
<<<<<<< HEAD
       if(controlPoints.size() == 1){
            ControlPoint newControlPoint = new ControlPoint( controlPoints.get(0).noiseVolume + 1.0,controlPoints.get(0).suitableVolume);
            controlPoints.add(newControlPoint);
            Log.d("コントロールポイントを足した！！！！！！！！！",""+readControlPoints().toString());

        }

    }

    @Override
    public void onPause(){
        super.onPause();
        if(thread_open == false) {
            Toast.makeText(this, "音量調節がバックグラウンドで動作します.", Toast.LENGTH_LONG).show();
        }
=======
        if(controlPoints.size() == 1){
            ControlPoint newControlPoint = new ControlPoint(controlPoints.get(0).suitableVolume, controlPoints.get(0).noiseVolume + 1.0);
            controlPoints.add(newControlPoint);
        }

>>>>>>> f41580e7f03614ba0097fcabda1e9a0d5bd872c6
    }


}
