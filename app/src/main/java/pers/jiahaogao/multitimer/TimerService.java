package pers.jiahaogao.multitimer;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import pers.jiahaogao.multitimer.timer.TimerClass;

public class TimerService extends Service {

    public static TimerClass timerClass = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(Lib.LOG_TAG, "TimerService onCreate");

        super.onCreate();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (null != timerClass) {
            // 开启前台服务
            startForeground((int) timerClass.getTag(), Lib.createNotification(getApplicationContext(), MainActivity.class, 0, "MultiTimer", "Running Background"));

            // 让线程在阻塞相应时间后响铃
            new Thread(() -> {
                try {
                    Thread.sleep(1000 * timerClass.getDelay());
                    timerClass.showNotification(getApplicationContext());
                    timerClass.playRingtone(getApplicationContext());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            Log.e(Lib.LOG_TAG, "TimerService onStartCommand: \n\tError: timerClass == null , onStartCommand ," + getApplicationInfo().className);
            stopSelf(); // 停止Service
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(Lib.LOG_TAG, "TimerService onDestroy");
        stopForeground(true);   // 停止前台服务
        super.onDestroy();
    }
}
