package com.example.myscheduler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn = findViewById(R.id.btn_schedule);

        btn.setOnClickListener(v -> {
            // TODO: 修改这里，换成你要启动的 App 包名 + Activity
            String targetPackage = "com.tencent.mm";
            String targetActivity = "com.tencent.mm.ui.LauncherUI";

            Intent intent = new Intent();
            intent.setClassName(targetPackage, targetActivity);

            PendingIntent pendingIntent = PendingIntent.getActivity(
                    this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            long triggerAtMillis = System.currentTimeMillis() + 60 * 1000; // 1分钟后
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);

            Toast.makeText(this, "已设置任务：1分钟后启动目标应用", Toast.LENGTH_LONG).show();
        });
    }
}
