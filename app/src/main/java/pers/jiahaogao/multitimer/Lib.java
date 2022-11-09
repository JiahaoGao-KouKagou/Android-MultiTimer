package pers.jiahaogao.multitimer;


import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.File;

import pers.jiahaogao.multitimer.dboperation.TimerDBManager;

public class Lib {
    // 通知ChannelID
    public static final String CHANNEL_ID_ALARM = "CHANNEL_ID_ALARM";
    // Log.d的TAG
    public static final String LOG_TAG = "MultiTimer";
    // 数据库管理器
    @SuppressLint("StaticFieldLeak")
    public static TimerDBManager timerDBManager;



    // 将时间字符串解析成秒数（格式必须 1:05 而不能 1:5）
    public static long parseStringToSeconds(String timeString) {
        int[] timeArrHMS = parseStringToTimeArrHMS(timeString);
        return ((long) 3600 * timeArrHMS[0] + (long) 60 * timeArrHMS[1] + timeArrHMS[2]);
    }
    // 提取时间字符串中的 时、分、秒
    public static int[] parseStringToTimeArrHMS(String timeString) {
        int[] result = new int[3];

        // 去冒号，转为数字
        long timeNum = Long.parseLong(timeString.replaceAll("[[\\s-:punct:]]",""));

        result[0] = (int) timeNum / 10000;          // 时
        result[1] = (int) (timeNum % 10000) / 100;  // 分
        result[2] = (int) timeNum % 100;            // 秒

        return result;
    }

    // 将秒数解析成字符串（格式必须 1:05 而不能 1:5）
    public static String parseSecondsToString(long timeSeconds) {
        StringBuilder result = new StringBuilder();
        int[] timeArrHMS = extractTimeArrHMSFromSeconds(timeSeconds);

        // 时
        if (timeArrHMS[0] > 0) {
            result.append(timeArrHMS[0]).append(":");
        }

        // 分
        if (timeArrHMS[1] < 10) {
            result.append("0");
        }
        result.append(timeArrHMS[1]).append(":");

        // 秒
        if (timeArrHMS[2] < 10) {
            result.append("0");
        }
        result.append(timeArrHMS[2]);

        return result.toString();
    }
    // 提取秒数中的 时、分、秒
    public static int[] extractTimeArrHMSFromSeconds(long timeSeconds) {
        int[] result = new int[3];

        result[0] = (int) timeSeconds / 3600;           // 时
        result[1] = (int) (timeSeconds % 3600) / 60;    // 分
        result[2] = (int) (timeSeconds % 3600) % 60;    // 秒

        return result;
    }

    // 将long型tag转换为String[]
    public static String[] tagToStringArr(long tag) {
        return (new String[]{String.valueOf(tag)});
    }


    // 创建通知
    public static Notification createNotification(Context context, Class<?> target, int tag, String title, String text) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Lib.CHANNEL_ID_ALARM)
                .setSmallIcon(R.mipmap.ic_logo)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // 点击通知栏进入原先正在运行的Activity，而不是新创建的Activity
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setComponent(new ComponentName(context, target));    //用ComponentName得到class对象
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED); // 关键的一步，设置启动模式，两种情况
            builder.setContentIntent(PendingIntent.getActivity(context, tag, intent, PendingIntent.FLAG_MUTABLE));
        }

        return builder.build();
    }

    // 显示通知
    public static void showNotification(Context context,  Class<?> target, int tag, String title, String text) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(tag, Lib.createNotification(context, target, tag, title, text));
    }


    // 从uri获取文件名
    // 参考自 https://www.cnblogs.com/zhujiabin/p/6692025.html
    // 参考自 https://www.cnblogs.com/zhujiabin/p/6692833.html
    // 参考自 https://blog.csdn.net/xiaohelloming/article/details/103157197
    // FIXME: 2022-06-03
    public static String getRingtoneNameFromUri(Context context, Uri uri) {
        if ( null == uri ) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if ( scheme == null )
            data = uri.getPath();
        else if ( ContentResolver.SCHEME_FILE.equals( scheme ) ) {
            data = uri.getPath();
        } else if ( ContentResolver.SCHEME_CONTENT.equals( scheme ) ) {
            Cursor cursor = context.getContentResolver().query( uri, new String[] { MediaStore.Audio.Media.DISPLAY_NAME }, null, null, null );
            if ( null != cursor ) {
                if ( cursor.moveToFirst() ) {
                    int index = cursor.getColumnIndex( MediaStore.Audio.Media.DISPLAY_NAME );
                    if ( index > -1 ) {
                        data = cursor.getString( index );
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

}
