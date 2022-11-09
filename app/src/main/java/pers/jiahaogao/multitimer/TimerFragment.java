package pers.jiahaogao.multitimer;

import android.app.AlertDialog;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.Objects;

import pers.jiahaogao.multitimer.timer.TimerClass;
import pers.jiahaogao.multitimer.timer.TimerClassArraylist;

public class TimerFragment extends Fragment {

    // 用于页面跳转和传输数据
    private ActivityResultLauncher<Intent> setTimerDialogActivityLauncher;
    // 用于打开设置铃声弹框
    private ActivityResultLauncher<Intent> setRingtoneLauncher;

    // 创建fragment用的
    private View root;
    // fragment内的组件
    private ImageButton btn_delete;
    private ImageButton btn_start;
    private ImageButton btn_reset;
    private EditText editText_name;
    private Chronometer chronometer;
    private Button btn_targetTime;
    private ImageButton btn_setRingtone;
    // 计时器
    TimerClass timerClass;

    public TimerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // 指定layout。Inflate the layout for this fragment
        if (null == root) {
            root = inflater.inflate(R.layout.fragment_timerfragment, container, false);
        }

        // fragment中各组件初始化
        init();


        // 名字EditText变更
        editText_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                timerClass.setName(editText_name.getText().toString());
            }
        });


        // 各按钮的配置
        // 开始计时按钮
        btn_start.setOnClickListener(view -> {
            if (timerClass.isStarted()) {
                chronometerPause();
            } else {
                chronometerStart();
            }
        });

        // 重置按钮
        btn_reset.setOnClickListener(view -> chronometerReset(0));

        // 删除按钮
        btn_delete.setOnClickListener(view -> {
            // 弹框，按OK删除
            AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                    .setTitle("Delete")
                    .setMessage("\nDelete \"" + editText_name.getText().toString() + "\" ?")
                    .setIcon(R.mipmap.ic_logo_foreground)
                    .setPositiveButton("OK", (dialogInterface, i) -> {
                        // 按下OK，则删除此Fragment
                        FragmentManager parentFragmentManager = getParentFragmentManager();
                        FragmentTransaction fragmentTransaction = parentFragmentManager.beginTransaction();
                        fragmentTransaction.remove(Objects.requireNonNull(parentFragmentManager.findFragmentByTag(getTag())));
                        fragmentTransaction.commit();
                        // 从数组中暂时不删除对应Timer（ArrayList软删除），下次加载数组时才真正不会将其加载
                        chronometerPause(); // 先将该计时器停止
                        // 从数据库删除对应Timer
                        timerClass.deleteThis();
                    })
                    .setNegativeButton("Cancel", (dialogInterface, i) -> {

                    })
                    .create();
            alertDialog.show();
        });

        // 设置目标时间
        btn_targetTime.setOnClickListener(view -> {
            Intent intent = new Intent(root.getContext(), SetTimerDialogActivity.class);
            intent.putExtra("name", editText_name.getText().toString());
            intent.putExtra("timeString", btn_targetTime.getText());
            // 携带数据跳转
            setTimerDialogActivityLauncher.launch(intent);
        });

        // 设置铃声
        btn_setRingtone.setOnClickListener(view -> {
            // 打开系统铃声设置
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Set Alarm Ringtone");
            setRingtoneLauncher.launch(intent);
        });


        // 计时器chronometer相关
        // 计时器监听
        chronometer.setOnChronometerTickListener(chronometer -> {
            // 时间到
            if (timerClass.isTimeUp()) {
                timeUpAct();
            }
            // 更新时间
            timerClass.setPassed(Lib.parseStringToSeconds(chronometer.getText().toString()));
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(Lib.LOG_TAG, "TimerFragment onResume: " + timerClass.getName());
        // 时间到
        if (timerClass.isTimeUp()) {
            timeUpAct();
        }
    }

    // fragment中各组件初始化
    private void init() {
        // 弹框配置
        // SetTimerDialogActivity弹框
        setTimerDialogActivityLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            // 从SetTimerDialogActivity返回的数据
            Intent data = result.getData();
            if (null != data) {
                // 设置目标时间
                setTargetTime(data.getExtras().getLong("timeSecond"));
                // 修改name
                setName(data.getExtras().getString("name"));
            }
        });
        // setRingtoneLauncher打开铃声列表设置铃声
        setRingtoneLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            // 返回的数据
            Intent data = result.getData();
            if (null != data) {
                // 得到我们选择的铃声,如果选择的是"静音"，那么将会返回null
                Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                if (uri != null) {
                    timerClass.setRingtone(uri);
                }
            }
        });

        // 获取fragment中各组件
        btn_delete = root.findViewById(R.id.btn_delete);
        btn_start = root.findViewById(R.id.btn_start);
        btn_reset = root.findViewById(R.id.btn_reset);
        editText_name = root.findViewById(R.id.editText_name_fragment);
        chronometer = root.findViewById(R.id.chronometer);
        btn_targetTime = root.findViewById(R.id.btn_targetTime);
        btn_setRingtone = root.findViewById(R.id.btn_setRingtone);

        // 根据tag获取对应计时器
        timerClass = TimerClassArraylist.findByTag(Integer.parseInt(Objects.requireNonNull(getTag())));

        // 根据对应计时器填写信息
        editText_name.setText(timerClass.getName());
        btn_targetTime.setText(Lib.parseSecondsToString(timerClass.getTarget()));

    }



    // 设置目标时间
    private void setTargetTime(long timeSeconds) {
        timerClass.setTarget(timeSeconds);
        btn_targetTime.setText(Lib.parseSecondsToString(timeSeconds));
    }
    // 修改name
    private void setName(String name) {
        timerClass.setName(name);
        editText_name.setText(name);
    }


    // chronometer相关
    // 开始计时
    private void chronometerStart() {
        chronometerReset(timerClass.getPassed());
        chronometer.start();
        btn_start.setImageResource(R.drawable.ic_pause);
        timerClass.setIsStarted(true);
    }
    // 停止计时
    private void chronometerPause() {
        //timerClass.decTime();
        chronometer.stop();
        btn_start.setImageResource(R.drawable.ic_start);
        timerClass.setIsStarted(false);
    }
    // 重置
    private void chronometerReset(long base) {
        timerClass.setPassed(base);
        //setBase 设置基准时间
        chronometer.setBase(SystemClock.elapsedRealtime() - 1000 * base);
    }


    // 时间到
    private void timeUpAct() {
        // 先播放铃声，后显示弹框
        timerClass.playRingtone(getContext());
        timerClass.showDialog(getContext());
        // 停止计时器
        chronometerPause();
        // 时间清零
        chronometerReset(0);
    }
}