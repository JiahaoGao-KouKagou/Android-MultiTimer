package pers.jiahaogao.multitimer.dboperation;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.RingtoneManager;

import java.util.ArrayList;
import java.util.Objects;

import pers.jiahaogao.multitimer.Lib;
import pers.jiahaogao.multitimer.TimerFragment;
import pers.jiahaogao.multitimer.timer.TimerClass;


public class TimerDBManager {

    public static final String dbName = "multitimer.db";

    private final Context context;
    @SuppressLint("StaticFieldLeak")
    private static TimerDBManager instance;
    // 操作表的对象，进行增删改查
    private final SQLiteDatabase writableDatabase;

    private TimerDBManager(Context context) {
        this.context = context;
        DBHelper dbHelper = new DBHelper(this.context, dbName, null, 1);
        writableDatabase = dbHelper.getWritableDatabase();
    }

    public static TimerDBManager getInstance(Context context) {
        if (instance == null) {
            synchronized (TimerDBManager.class) {
                if (instance == null) {
                    instance = new TimerDBManager(context);
                }
            }
        }
        return instance;
    }

    // 成功时返回主键tag，不成功时返回-1
    public int addTimer() {
        long result;
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", "name");
        contentValues.put("ringtone", String.valueOf(RingtoneManager.getRingtone(this.context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))));
        result = writableDatabase.insert("timer", null, contentValues);
        // 最多只能有Integer.MAX_VALUE条记录
        if (result >= Integer.MAX_VALUE) {
            delTimer(Lib.tagToStringArr(result));
            result = -1;
        }
        return (int) result;
    }

    public void delTimer(String[] tag) {
        writableDatabase.delete("timer", "tag = ?", tag);
    }


    public void update(String columnIndex, String value, String[] tag) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(columnIndex, value);
        writableDatabase.update("timer", contentValues, "tag = ?", tag);
    }
    public void update(String columnIndex, long value, String[] tag) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(columnIndex, value);
        writableDatabase.update("timer", contentValues, "tag = ?", tag);
    }

    public TimerFragment selectTimer(String tag) {
        @SuppressLint("Recycle") Cursor cursor = writableDatabase.query("timer", null, null, null, null, null, null, null);
        TimerFragment result = null;
        while (cursor.moveToNext()) {
            if (Objects.equals(cursor.getString(Integer.parseInt("tag")), tag)) {
                result = new TimerFragment();
                break;
            }
        }
        return result;
    }

    @SuppressLint("Range")
    public String selectAllToString() {
        @SuppressLint("Recycle") Cursor cursor = writableDatabase.query("timer", null, null, null, null, null, null, null);
        StringBuilder result = new StringBuilder();
        while (cursor.moveToNext()) {
            @SuppressLint("Range") int tag = cursor.getInt(cursor.getColumnIndex("tag"));
            @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex("name"));
            @SuppressLint("Range") long target = cursor.getLong(cursor.getColumnIndex("target"));
            @SuppressLint("Range") String ringtoneUriStr = cursor.getString(cursor.getColumnIndex("ringtone"));

            result.append(String.format("tag = %s, name = %s, target = %s, ringtone = %s",
                    tag, name, target, ringtoneUriStr)).append("\n");
        }
        return result.toString();
    }

    public ArrayList<TimerClass> selectAllToArr() {
        @SuppressLint("Recycle") Cursor cursor = writableDatabase.query("timer", null, null, null, null, null, null, null);
        ArrayList<TimerClass> result = new ArrayList<>();
        while (cursor.moveToNext()) {
            result.add(extractTimerClassByCursor(cursor));
        }
        return result;
    }

    @SuppressLint("Range")
    public ArrayList<TimerClass> selectAllStartedToArr() {
        @SuppressLint("Recycle") Cursor cursor = writableDatabase.query("timer", null, null, null, null, null, null, null);
        ArrayList<TimerClass> result = new ArrayList<>();
        while (cursor.moveToNext()) {
            if ((cursor.getInt(cursor.getColumnIndex("isstarted")) != 0)) {
                result.add(extractTimerClassByCursor(cursor));
            }
        }
        return result;
    }

    private TimerClass extractTimerClassByCursor(Cursor cursor) {
        @SuppressLint("Range") int tag = cursor.getInt(cursor.getColumnIndex("tag"));
        @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex("name"));
        @SuppressLint("Range") long target = cursor.getLong(cursor.getColumnIndex("target"));
        @SuppressLint("Range") String ringtoneUriStr = cursor.getString(cursor.getColumnIndex("ringtone"));
        return (new TimerClass(tag, name, target, ringtoneUriStr));
    }

    public Cursor query() {
        return writableDatabase.query("timer", null, null, null, null, null, null, null);
    }

}
