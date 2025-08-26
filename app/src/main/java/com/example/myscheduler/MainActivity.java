package com.example.myscheduler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn = findViewById(R.id.btn_schedule);

        btn.setOnClickListener(v -> {
            // 设置钉钉应用的包名和Activity
            String targetPackage = "com.alibaba.android.rimet";
            String targetActivity = "com.alibaba.android.rimet.biz.SplashActivity";

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            
            // 设置每天9:25的定时任务
            setDailyAlarm(alarmManager, targetPackage, targetActivity, 9, 25, 1001);
            
            // 设置每天18:35的定时任务
            setDailyAlarm(alarmManager, targetPackage, targetActivity, 18, 35, 1002);

            Toast.makeText(this, "已设置每日定时任务：\n9:25 和 18:35 启动钉钉", Toast.LENGTH_LONG).show();
        });
    }
    
    private void setDailyAlarm(AlarmManager alarmManager, String targetPackage, String targetActivity, int hour, int minute, int requestCode) {
        Intent intent = new Intent();
        intent.setClassName(targetPackage, targetActivity);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, requestCode, intent, PendingIntent.FLAG_IMMUTABLE);

        // 获取当前时间
        Calendar calendar = Calendar.getInstance();
        Calendar targetTime = Calendar.getInstance();
        
        // 设置目标时间（今天的指定时间）
        targetTime.set(Calendar.HOUR_OF_DAY, hour);
        targetTime.set(Calendar.MINUTE, minute);
        targetTime.set(Calendar.SECOND, 0);
        targetTime.set(Calendar.MILLISECOND, 0);
        
        // 如果今天的指定时间已经过了，设置为明天的同一时间
        if (targetTime.getTimeInMillis() <= calendar.getTimeInMillis()) {
            targetTime.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        // 设置每日重复的闹钟
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            targetTime.getTimeInMillis(),
            AlarmManager.INTERVAL_DAY,  // 每天重复
            pendingIntent
        );
    }
}
