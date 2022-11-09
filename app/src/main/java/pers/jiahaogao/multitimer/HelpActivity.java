package pers.jiahaogao.multitimer;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class HelpActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);


        // 获取ContentProvider提供的数据
        ContentResolver contentResolver = getContentResolver();
        Uri uri = Uri.parse("content://pers.jiahaogao.multitimer/timer/1");
        @SuppressLint("Recycle") Cursor cursor = contentResolver.query(uri, null, null, null, null);
        ArrayList<String> DBData = new ArrayList<>();
        while (cursor.moveToNext()) {
            @SuppressLint("Range") int tag = cursor.getInt(cursor.getColumnIndex("tag"));
            @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex("name"));
            @SuppressLint("Range") long target = cursor.getLong(cursor.getColumnIndex("target"));
            @SuppressLint("Range") String ringtone = cursor.getString(cursor.getColumnIndex("ringtone"));

            DBData.add(String.format(" tag = %s\n name = %s\n target = %s\n ringtone = %s",
                    tag, name, target, ringtone));
        }
        //Log.d(Lib.LOG_TAG, "HelpActivity onCreate: " + DBData);
        // 将数据放入表格
        ArrayAdapter<String> listArrayAdapter= new ArrayAdapter<>(HelpActivity.this, android.R.layout.simple_list_item_1, DBData);
        ((ListView) findViewById(R.id.listView_dbViewer)).setAdapter(listArrayAdapter);

    }
}
