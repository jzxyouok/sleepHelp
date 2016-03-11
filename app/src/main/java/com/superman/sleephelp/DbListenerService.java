package com.superman.sleephelp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.app.Notification;
import android.app.NotificationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.awt.font.TextAttribute;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

import android.media.MediaRecorder;
import android.os.Handler;
import android.widget.TextView;

public class DbListenerService extends Service {
    int vol;
    double checkTime1;
    int checkTime2;
    int volMax ;
    int sum;
    boolean isMax;
    List<Integer> list;
    @Override
    public void onCreate() {
        super.onCreate();
        Notification.Builder notify = new Notification.Builder(this);
        notify.setSmallIcon(R.drawable.common_full_open_on_phone);
        notify.setTicker("");
        notify.setContentText("打呼检测已启动。。。");
        notify.setContentTitle("午睡小助手");
        startForeground(1, notify.build());
        list = new ArrayList<>();

    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("service", "服务开始");
        Intent bIntent=new Intent("newValue");
        vol = intent.getIntExtra("voll", 0);
        checkTime1 = intent.getExtras().getDouble("checkTime");
        if (checkTime1 == 0.0) {
            checkTime2=1;
        }else{
        checkTime2=(int)(checkTime1*10);}

        volMax = intent.getIntExtra("volMax", 90);
        list.add(vol);
        if (list.size() == 31){
            list.clear();
        }
        if (list.size() == checkTime2) {
            Log.d("service", "在list中");
            for (int array : list) {

                sum = sum + array;
            }
            int lastValue = sum / list.size();
           isMax=lastValue >= volMax;
            Log.d("service", "isMax"+isMax);
            if (isMax) {

                bIntent.putExtra("checked",true);
                Log.d("service", "放入checked为真");
            }

            list.clear();
            sum=0;
        }
//每隔0.1秒发送广播
            AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
            int anHour = 100;
            long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
            PendingIntent pi = PendingIntent.getBroadcast(this, 0, bIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi );
            Log.d("service", "服务结束检查放入的值"+bIntent.getBooleanExtra("checked",false)+"");
        return super.onStartCommand(intent, flags, startId);
    }
}