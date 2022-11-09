package pers.jiahaogao.multitimer.timer;

import android.app.AlertDialog;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import org.w3c.dom.DocumentFragment;

import pers.jiahaogao.multitimer.Lib;
import pers.jiahaogao.multitimer.MainActivity;
import pers.jiahaogao.multitimer.R;

public class TimerClass {

    private final int tag;
    private String name;
    // 开始/暂停
    private boolean isStarted;
    // 监听计时器的时间
    private long passed;
    // 目标时间
    private long target;
    // 该计时器的铃声
    private Uri ringtoneUri;
    private Ringtone ringtone;


    public TimerClass(int tag) {
        this.tag = tag;
        isStarted = false;        // 开始/暂停
        name = "name";
        passed = 0;
        target = 0;
        ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    }

    public TimerClass(int tag, String name, long target, String ringtoneUriStr) {
        isStarted = false;        // 开始/暂停
        this.tag = tag;
        this.name = name;
        this.passed = 0;
        this.target = target;
        this.ringtoneUri = Uri.parse(ringtoneUriStr);
    }

    public int getTag() {
        return tag;
    }
    //public void setTag(long tag) { this.tag = tag; }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
        Lib.timerDBManager.update("name", name, Lib.tagToStringArr(tag));
    }

    public long getPassed() {
        return passed;
    }
    public void setPassed(long passed) {
        this.passed = passed;
    }

    public long getTarget() {
        return target;
    }
    public void setTarget(long target) {
        this.target = target;
        Lib.timerDBManager.update("target", target, Lib.tagToStringArr(tag));
    }

    public boolean isStarted() {
        return isStarted;
    }
    public void setIsStarted(boolean isStarted) {
        this.isStarted = isStarted;
    }

    public Uri getRingtoneUri() { return ringtoneUri; }
    public void setRingtone(Uri uri) {
        ringtoneUri = uri;
        Lib.timerDBManager.update("ringtone", uri.toString(), Lib.tagToStringArr(tag));
    }

    public boolean isTimeUp() {
        return (passed > target);
    }

    public void incTime() {
        ++passed;
    }
    public void decTime() { --passed; }


    // 时间到
    public void timeUpAct(Context context) {
        // 显示通知
        showNotification(context);
        // 播放铃声
        playRingtone(context);
    }


    // 播放铃声（不停循环）直到按下弹框停止按钮
    public void playRingtone(Context context) {
        // 初始时ringtone为空，需要获取ringtone || 如果有ringtone而没在播放，就更新ringtone并播放
        if (null == ringtone || !ringtone.isPlaying()) {
            ringtone = RingtoneManager.getRingtone(context, ringtoneUri);
            ringtone.setLooping(true);
            ringtone.play();
        }
        // 如果已经在播放，则不执行任何操作
    }

    // 弹框
    public void showDialog(Context context) {
        try {
            AlertDialog alertDialog = new AlertDialog.Builder(context)
                    .setTitle(name)
                    .setMessage("is Time Up")
                    .setIcon(R.mipmap.ic_logo_foreground)
                    .setCancelable(false)
                    .setPositiveButton("OK", (dialogInterface, i) -> {
                        // 停止响铃
                        if (ringtone.isPlaying()) {
                            ringtone.stop();
                        }
                    })
                    .create();
            alertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            if (ringtone.isPlaying()) {
                ringtone.stop();
            }
        }
    }

    // 显示通知
    public void showNotification(Context context) {
        Lib.showNotification(context, MainActivity.class, tag, "MultiTimer", "\"" + name + "\"" + " is Time Up");
    }

    // 删除此Timer
    public void deleteThis() {
        Lib.timerDBManager.delTimer(Lib.tagToStringArr(tag));
        Log.d(Lib.LOG_TAG, "TimerClass deleteThis: " + Lib.timerDBManager.selectAllToString());
    }

    // 距离响铃时间还有多久（剩余时间）
    public long getDelay() {
        return (target - passed);
    }

}
