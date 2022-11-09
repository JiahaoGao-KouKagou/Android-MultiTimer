package pers.jiahaogao.multitimer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.NumberPicker;


public class SetTimerDialogActivity extends Activity {

    private EditText editText_name;
    private NumberPicker numberPicker_hour;
    private NumberPicker numberPicker_minute;
    private NumberPicker numberPicker_second;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settimer_dialog);

        // 获取标题框
        editText_name = findViewById(R.id.editText_name);
        // 将参数(Timer 的 name)显示到标题框
        editText_name.setText(getIntent().getExtras().getString("name"));

        // 获取NumberPicker
        numberPicker_hour = findViewById(R.id.numberPicker_hour);
        numberPicker_minute = findViewById(R.id.numberPicker_minute);
        numberPicker_second = findViewById(R.id.numberPicker_second);
        // 初始化NumberPicker
        int[] timeArrHMS = Lib.parseStringToTimeArrHMS(getIntent().getExtras().getString("timeString"));
        initNumberPicker(numberPicker_hour, 0, 23, timeArrHMS[0]);
        initNumberPicker(numberPicker_minute, 0, 59, timeArrHMS[1]);
        initNumberPicker(numberPicker_second, 0, 59, timeArrHMS[2]);


        // 取消按钮
        findViewById(R.id.btn_cancel_setTimer).setOnClickListener(view -> finish());

        // OK按钮
        // 返回带参数: name, time
        findViewById(R.id.btn_ok_setTimer).setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.putExtra("name", editText_name.getText().toString());
            intent.putExtra("timeSecond", getNumberPickerTimeSecond());
            setResult(Activity.RESULT_OK, intent);
            finish();
        });

    }


    //点击退出
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        finish();
        return true;
    }

    // 初始化NumberPicker
    private void initNumberPicker(NumberPicker numberPicker, int min, int max, int presets) {
        numberPicker.setMinValue(min);
        numberPicker.setMaxValue(max);
        // 格式：把 1 变成 01
        numberPicker.setFormatter(i -> {
            StringBuilder result = new StringBuilder();
            if (i < 10) {
                result.append("0");
            }
            result.append(i);
            return result.toString();
        });
        // 显示之前设置的时间
        numberPicker.setValue(presets);
    }

    // 获取numberPicker的时间
    private long getNumberPickerTimeSecond() {
        return (long) 3600 * numberPicker_hour.getValue() + (long) 60 * numberPicker_minute.getValue() + numberPicker_second.getValue();
    }

}