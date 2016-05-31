package com.superman.sleephelp;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    AudioRecordDemo audioRecordDemo;
    int progress1;
    int progress2;
    TextView setVolText;
    SeekBar seekBar1;
    Button startButton;
    MyReceiver myReceiver;
    boolean isReg;
    TextView textView;
    Button stopButton;
    int status ;
    final int TURNON = 1;
    final int TURNOFF = 0;
    SeekBar timeSeek;
    TextView timeText;
    int voll;
    Timer timer;
    MyTimerTask task;
    Intent ser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("m", "88");
        audioRecordDemo = new AudioRecordDemo();
        startButton = (Button) findViewById(R.id.start_button);
        setVolText = (TextView) findViewById(R.id.setVolText);
        timeText = (TextView) findViewById(R.id.setTimeText);
        textView = (TextView) findViewById(R.id.textView);
        seekBar1 = (SeekBar) findViewById(R.id.setVolSeek);
        timeSeek = (SeekBar) findViewById(R.id.setTimeSeek);
        timeSeek.setMax(30);
        timeSeek.setProgress(10);
        stopButton = (Button) findViewById(R.id.stop_button);
        seekBar1.setMax(90);
        seekBar1.setProgress(65);
        progress1 = seekBar1.getProgress();
        progress2 = timeSeek.getProgress();
        setVolText.setText("灵敏度：" + progress1);
        timeText.setText("检测间隔：" + 1.0 + "秒");
        ser = new Intent(MainActivity.this, DbListenerService.class);
        timeSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                timeText.setText("检测间隔：" + progress / 10d + "秒");
                if (status == TURNON) {
                    ser.putExtra("voll", voll);//检测到的分贝值
                    ser.putExtra("volMax", seekBar1.getProgress());//灵敏度
                    ser.putExtra("checkTime", progress / 10d);//检测间隔
                    startService(ser);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

//        设置灵敏度SeekBar方法
        seekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setVolText.setText("灵敏度：" + progress);
                if (status == TURNON) {
                    ser.putExtra("voll", voll);
                    ser.putExtra("volMax", seekBar1.getProgress());
                    ser.putExtra("checkTime", timeSeek.getProgress() / 10d);
                    startService(ser);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
//        设置开始按钮点击功能
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                status = TURNON;
                //        动态注册广播接收器
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction("newValue");
                myReceiver = new MyReceiver();
                registerReceiver(myReceiver, intentFilter);
                isReg=true;
                showDb();
                Log.d("activity", "按下开始按钮1");
                ser.putExtra("voll", voll);
                ser.putExtra("volMax", seekBar1.getProgress());
                ser.putExtra("checkTime", timeSeek.getProgress() / 10d);
                startService(ser);
                Log.d("activity", "按下开始按钮2");
            }

        });

//        设置停止按钮点击功能
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (status == TURNON) {
                    status = TURNOFF;
                    stopService(ser);
                    if (isReg==true){
                    unregisterReceiver(myReceiver);
                        isReg=false;}
                    if (timer != null) {
                        timer.cancel();
                    }
                    if (task != null) {
                        task.cancel();
                    }

                } else if (status == TURNOFF) {
                    Toast.makeText(MainActivity.this, "已经停止检测了哦~", Toast.LENGTH_SHORT).show();
                }
                Log.d("activity", "按下停止按钮");
            }
        });


    }

    private void showDb() {

        audioRecordDemo.getNoiseLevel(new Callback() {
            @Override
            public void onFinish(final double vol) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (status==TURNON) {
                            textView.setText((int) vol + "");
                            voll = (int) vol;
                        }
                    }
                });
            }
        });
    }

    class MyTimerTask extends TimerTask{
        @Override
        public void run() {
//            Intent ser = new Intent(MainActivity.this, DbListenerService.class);
            ser.putExtra("voll", voll);
            ser.putExtra("volMax", seekBar1.getProgress());
            ser.putExtra("checkTime", timeSeek.getProgress() / 10d);
            startService(ser);
            Log.d("activity", "延迟一秒启动服务成功");
            if(timer!=null){
            timer.cancel();}
            if(task!=null){
            task.cancel();}
        }
    }

//    TimerTask timerTask=new TimerTask() {
//        @Override
//        public void run() {
//
//
//        }
//    };

    //  接收到广播就启动服务
    class MyReceiver extends BroadcastReceiver {


        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d("activity",  "活动开始检查checked值："+intent.getBooleanExtra("checked", false) );
            if (intent.getBooleanExtra("checked", false)) {
                Log.d("activity", "checked值为真，发送震动通知");
                sendNotify();
                 task=new MyTimerTask();
                timer = new Timer();
                timer.schedule(task, 1000);
                Log.d("activity", "checked值为真，准备延迟1秒启动服务3");

            } else {
                Log.d("activity4", "checked为假时立即启动服务");
//                Intent ser = new Intent(context, DbListenerService.class);
                ser.putExtra("voll", voll);
                ser.putExtra("volMax", seekBar1.getProgress());
                ser.putExtra("checkTime", timeSeek.getProgress() / 10d);
                startService(ser);
            }
        }
    }
    //    复写活动的销毁方法，在活动销毁时取消广播接收器的注册
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isReg==true) {
            unregisterReceiver(myReceiver);

        }
        if (status == TURNOFF) {
            stopService(ser);
        }
        audioRecordDemo.isGetVoiceRun=false;

    }


    public void sendNotify() {
        Log.d("通知执行", "震动通知");
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification.Builder notify = new Notification.Builder(this);
        notify.setContentTitle("警告！");
        notify.setContentText("声音太大了。。。");
        notify.setTicker("警告!警告！！警告！！！");
        notify.setSmallIcon(R.drawable.no);
        long[] vibrates = {000, 1000};
        notify.setVibrate(vibrates);
        manager.notify(1, notify.build());
    }
}