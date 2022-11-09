package pers.jiahaogao.multitimer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class TimerBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(Lib.LOG_TAG, "TimerBroadcastReceiver onReceive");
        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            Lib.showNotification(context, MainActivity.class, 0, "MultiTimer", "Running Background\nScreen On");
        } else {
            Lib.showNotification(context, MainActivity.class, 0, "MultiTimer", "Running Background\nScreen Off");
        }

    }

}