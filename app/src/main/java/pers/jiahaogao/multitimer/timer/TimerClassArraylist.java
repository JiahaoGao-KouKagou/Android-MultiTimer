package pers.jiahaogao.multitimer.timer;


import android.util.Log;

import java.util.ArrayList;

import pers.jiahaogao.multitimer.Lib;

public class TimerClassArraylist {

    // TimerClass数组
    private static final ArrayList<TimerClass> timerClassArraylist = new ArrayList<>();


    // 获取数组长度
    public static int getLength() {
        return timerClassArraylist.size();
    }

    // 新建TimerClass并放入数组
    public static void addNewByTag(int tag) {
        timerClassArraylist.add(new TimerClass(tag));
    }

    // 加载原有的TimerClass
    public static void load(TimerClass timerClass) {
        timerClassArraylist.add(timerClass);
    }


    // 根据tag找到timer
    public static TimerClass findByTag(int tag) {
        TimerClass result = null;
        for (int i = 0; i < timerClassArraylist.size(); i++) {
            if (tag == timerClassArraylist.get(i).getTag()) {
                result = timerClassArraylist.get(i);
                break;
            }
        }
        return result;
    }

    // 找到距离响铃时间最短的timer
    public static TimerClass findByMinDelay() {
        TimerClass result = null;

        int minIndex = 0;   // 目标timer的index

        int i;
        boolean noOneStarted = true;    // 判断是否有打开的计时器
        for (i = 0; i < timerClassArraylist.size(); i++) {    // 找到第一个打开的计时器
            if (timerClassArraylist.get(i).isStarted()) {
                minIndex = i;
                noOneStarted = false;
                break;
            }
        }

        if (!noOneStarted) {    // 如果有打开的计时器
            while (i < timerClassArraylist.size()) {
                if (timerClassArraylist.get(i).isStarted() && timerClassArraylist.get(i).getDelay() < timerClassArraylist.get(minIndex).getDelay()) {
                    minIndex = i;
                }
                i++;
            }
            Log.d(Lib.LOG_TAG, "TimerClassArraylist findByMinDelay: " + timerClassArraylist.get(minIndex).getName() + " delay " + timerClassArraylist.get(minIndex).getDelay());
            result = timerClassArraylist.get(minIndex);
        }

        return result;
    }
}
