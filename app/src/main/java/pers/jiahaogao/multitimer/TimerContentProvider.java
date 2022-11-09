package pers.jiahaogao.multitimer;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import pers.jiahaogao.multitimer.dboperation.TimerDBManager;

public class TimerContentProvider extends ContentProvider {


    private TimerDBManager timerDBManager;

    //匹配成功后的匹配码
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);        //匹配不成功返回NO_MATCH(-1)


    static {
        uriMatcher.addURI("pers.jiahaogao.multitimer","/timer", 1);
        uriMatcher.addURI("pers.jiahaogao.multitimer","/timer/#", 2);
    }


    @Override
    public boolean onCreate() {

        timerDBManager = TimerDBManager.getInstance(getContext());
        Log.d(Lib.LOG_TAG, "TimerContentProvider onCreate");
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] strings, @Nullable String s, @Nullable String[] strings1, @Nullable String s1) {
        Cursor result = null;
        //Log.d(Lib.LOG_TAG, "query: Cursor get ...");
        if (uriMatcher.match(uri) != UriMatcher.NO_MATCH){
            result = timerDBManager.query();
            //Log.d(Lib.LOG_TAG, "query: Cursor get successfully");
        }
        return result;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
