package pers.jiahaogao.multitimer;

import static android.app.NotificationManager.IMPORTANCE_HIGH;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;

import pers.jiahaogao.multitimer.dboperation.TimerDBManager;
import pers.jiahaogao.multitimer.timer.TimerClass;
import pers.jiahaogao.multitimer.timer.TimerClassArraylist;

public class MainActivity extends AppCompatActivity {

    private FragmentManager fragmentManager;    // fragment管理器
    private final TimerBroadcastReceiver[] timerBroadcastReceiver = new TimerBroadcastReceiver[2];  // 广播接收者


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 打开通知权限
        enableNotifications();
        // 创建通知渠道
        createNotificationChannel();

        // 加入后台白名单
        requestIgnoreBatteryOptimizations();

        // 注册广播接收者
        if (null != timerBroadcastReceiver[0]) {
            // 注销原有广播接收者
            for (int i = 0; i < timerBroadcastReceiver.length; i++) {
                unregisterReceiver(timerBroadcastReceiver[i]);
            }
        } else {
            for (int i = 0; i < timerBroadcastReceiver.length; i++) {
                timerBroadcastReceiver[i] = new TimerBroadcastReceiver();
            }
        }
        registerReceiver(timerBroadcastReceiver[0], new IntentFilter(Intent.ACTION_SCREEN_ON));
        registerReceiver(timerBroadcastReceiver[1], new IntentFilter(Intent.ACTION_SCREEN_OFF));

        // 获取数据库管理器
        Lib.timerDBManager = TimerDBManager.getInstance(this);
        // 获取FragmentManager
        fragmentManager = getSupportFragmentManager();

        // 加载数据库中的Timer
        ArrayList<TimerClass> arrayListToLoad = Lib.timerDBManager.selectAllToArr();
        for (int i = 0; i < arrayListToLoad.size(); i++) {
            loadTimer(arrayListToLoad.get(i));
        }

        // 点击"+"按钮新建Timer
        findViewById(R.id.btn_add).setOnClickListener(view -> addNewTimer());

        // 跳转到DB Viewer界面
        findViewById(R.id.btn_dbViewer).setOnClickListener(view -> startActivity(new Intent(MainActivity.this, HelpActivity.class)));

        Log.d(Lib.LOG_TAG, "MainActivity onCreate: \n" + Lib.timerDBManager.selectAllToString());

    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.d(Lib.LOG_TAG, "MainActivity onPause");

        // 找到距离响铃时间最短的timer
        TimerService.timerClass = TimerClassArraylist.findByMinDelay();
        if (null != TimerService.timerClass) {
            startService(new Intent(this, TimerService.class));  // 开启Service
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(Lib.LOG_TAG, "MainActivity onResume");

        stopService(new Intent(this, TimerService.class));   // 显示UI时即关闭Service
    }


    // 新建Timer
    private void addNewTimer() {
        // 在数据库中插入Timer
        int tag = Lib.timerDBManager.addTimer();
        if (tag < 0) {
            // 计时器数量超过最大限制，插入失败
            Toast.makeText(MainActivity.this, "Too Much Timer", Toast.LENGTH_LONG).show();
        } else {
            // 放入数组
            TimerClassArraylist.addNewByTag(tag);
            // 新建Fragment
            addFragment(Integer.toString(tag));
        }
    }
    // 加载原有的Timer
    private void loadTimer(TimerClass timerClass) {
        // 放入数组
        TimerClassArraylist.load(timerClass);
        // 新建Fragment
        addFragment(Long.toString(timerClass.getTag()));
    }
    // 新建Fragment
    private void addFragment(String tag) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.linearLayout_timers, new TimerFragment(), tag);
        fragmentTransaction.commit();
    }


    // 将APP加入后台白名单
    // 参考自 https://cloud.tencent.com/developer/article/1760087
    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean isIgnoringBatteryOptimizations() {
        boolean isIgnoring = false;
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if(powerManager != null) {
            isIgnoring = powerManager.isIgnoringBatteryOptimizations(getPackageName());
        }
        return isIgnoring;
    }
    @SuppressLint("ObsoleteSdkInt")
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void requestIgnoreBatteryOptimizations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isIgnoringBatteryOptimizations()) {
            try{
                @SuppressLint("BatteryLife") Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:"+ getPackageName()));
                startActivity(intent);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }


    // 通知相关
    // 创建通知渠道
    @SuppressLint("ObsoleteSdkInt")
    private void createNotificationChannel() {
        // 安卓版本判断，需要大于Android 8.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            CharSequence name = "Alarm";
            String description = "When time is up";
            NotificationChannel channel = new NotificationChannel(Lib.CHANNEL_ID_ALARM, name, IMPORTANCE_HIGH);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    // 打开通知权限
    private void enableNotifications() {
        NotificationManagerCompat notification = NotificationManagerCompat.from(this);
        boolean isEnabled = notification.areNotificationsEnabled();
        // 未打开通知时弹框提示，并跳转到通知设置界面
        if (!isEnabled) {
            @SuppressLint("ObsoleteSdkInt") AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("Notifications are NOT enabled")
                    .setMessage("\nNotifications are sent when time up.\n\nIt's a necessary function that you should allow it.")
                    .setCancelable(false)
                    .setPositiveButton("go to set", (dialog, which) -> {
                        dialog.cancel();
                        Intent intent = new Intent();
                        // 不同Android版本的操作
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                            intent.putExtra("android.provider.extra.APP_PACKAGE", getPackageName());
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {  //5.0
                            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                            intent.putExtra("app_package", getPackageName());
                            intent.putExtra("app_uid", getApplicationInfo().uid);
                            startActivity(intent);
                        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {  //4.4
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.addCategory(Intent.CATEGORY_DEFAULT);
                            intent.setData(Uri.parse("package:" + getPackageName()));
                        } else if (Build.VERSION.SDK_INT >= 15) {
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                            intent.setData(Uri.fromParts("package", getPackageName(), null));
                        }
                        startActivity(intent);
                    })
                    .create();
            alertDialog.show();
        }
    }

}