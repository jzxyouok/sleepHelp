package com.superman.sleephelp;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

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
        Log.d("service", "onCreate方法启动");
        super.onCreate();
        Notification.Builder notify = new Notification.Builder(this);
        notify.setSmallIcon(R.drawable.common_full_open_on_phone);
        notify.setTicker("前台服务成功开启");
        notify.setContentText("声音检测已启动。。。");
        notify.setContentTitle("午睡小助手");
        notify.setSmallIcon(R.drawable.logo);

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
        /*容错使用，万一超过31（一般情况下为30），直接初始化清空*/
        if (list.size() == 31){
            list.clear();
        }

       /*检测逻辑为在多少秒内的平均分贝值超过了设定的灵敏度，即判定为需要报警
       * 每隔0.1秒启动一次服务，所以指定泛型int使用一个list作为容器存放这段时间
       * 内的声音数据，当这个list的长度对等于设定的检测时间时，去求平均值与设定的灵敏度对比。*/
        if (list.size() == checkTime2) {
            Log.d("service", "在list中");
            for (int array : list) {

                sum = sum + array;
            }
            int lastValue = sum / list.size();
           isMax=(lastValue >= volMax);
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
            int anHour = 100;//100毫秒即为0.1秒
            long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
            PendingIntent pi = PendingIntent.getBroadcast(this, 0, bIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi );
            Log.d("service", "服务结束检查放入的值"+bIntent.getBooleanExtra("checked",false)+"");
        return super.onStartCommand(intent, flags, startId);
    }
}