package pers.jiahaogao.multitimer.dboperation;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import pers.jiahaogao.multitimer.Lib;


public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d(Lib.LOG_TAG, "DBHelper onCreate");
        sqLiteDatabase.execSQL("CREATE TABLE timer (tag INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR NOT NULL DEFAULT 'name', target BIGINT NOT NULL DEFAULT 0, ringtone VARCHAR)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }


}
