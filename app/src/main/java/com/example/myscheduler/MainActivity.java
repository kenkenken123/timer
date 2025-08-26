package com.example.myscheduler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;
import android.Manifest;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1001;
    private NotificationHelper notificationHelper;
    private BatteryOptimizationHelper batteryHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化帮助类
        notificationHelper = new NotificationHelper(this);
        batteryHelper = new BatteryOptimizationHelper(this);
        
        // 启动前台服务保持应用运行
        startKeepAliveService();
        
        // 请求通知权限
        requestNotificationPermission();
        
        // 设置电池优化白名单
        batteryHelper.setupKeepAlive(this);

        Button btn = findViewById(R.id.btn_schedule);
        Button btnStartDingDing = findViewById(R.id.btn_start_dingding);
        Button btnTestDingDing = findViewById(R.id.btn_test_dingding);

        btn.setOnClickListener(v -> {
            // 检查钉钉应用是否已安装
            if (!AlarmReceiver.isDingDingInstalled(this)) {
                Toast.makeText(this, "❌ 未检测到钉钉应用，请先安装钉钉", Toast.LENGTH_LONG).show();
                return;
            }

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            
            // 设置每天9:25的定时任务
            setDailyAlarmWithBroadcast(alarmManager, 9, 25, 1001);
            
            // 设置每天18:35的定时任务
            setDailyAlarmWithBroadcast(alarmManager, 18, 35, 1002);

            Toast.makeText(this, "✅ 已设置每日定时任务：\n9:25 和 18:35 启动钉钉", Toast.LENGTH_LONG).show();
            
            // 显示下次定时任务的通知
            notificationHelper.showDailyScheduleNotification();
        });

        btnStartDingDing.setOnClickListener(v -> {
            // 检查钉钉应用是否已安装
            if (!AlarmReceiver.isDingDingInstalled(this)) {
                Toast.makeText(this, "❌ 未检测到钉钉应用，请先安装钉钉", Toast.LENGTH_LONG).show();
                return;
            }

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            
            // 计算1分钟后的时间
            Calendar targetTime = Calendar.getInstance();
            targetTime.add(Calendar.MINUTE, 1);
            
            // 创建广播意图
            Intent broadcastIntent = new Intent(this, AlarmReceiver.class);
            broadcastIntent.setAction(AlarmReceiver.ACTION_START_DINGDING);
            broadcastIntent.putExtra(AlarmReceiver.EXTRA_TASK_TYPE, AlarmReceiver.TASK_TYPE_INSTANT);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this, 2001, broadcastIntent, PendingIntent.FLAG_IMMUTABLE);

            // 设置一次性闹钟（1分钟后触发）
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                targetTime.getTimeInMillis(),
                pendingIntent
            );

            Toast.makeText(this, "✅ 1分钟后将自动启动钉钉应用", Toast.LENGTH_LONG).show();
            
            // 显示即时定时任务的通知
            notificationHelper.showInstantScheduleNotification();
        });
        
        // 测试按钮：立即启动钉钉应用
        btnTestDingDing.setOnClickListener(v -> {
            // 检查钉钉应用是否已安装
            if (!AlarmReceiver.isDingDingInstalled(this)) {
                Toast.makeText(this, "❌ 未检测到钉钉应用，请先安装钉钉", Toast.LENGTH_LONG).show();
                return;
            }
            
            // 直接调用AlarmReceiver的逻辑来测试启动
            Intent testIntent = new Intent(this, AlarmReceiver.class);
            testIntent.setAction(AlarmReceiver.ACTION_START_DINGDING);
            testIntent.putExtra(AlarmReceiver.EXTRA_TASK_TYPE, "test");
            
            AlarmReceiver receiver = new AlarmReceiver();
            receiver.onReceive(this, testIntent);
            
            Toast.makeText(this, "🗋 正在测试启动钉钉...", Toast.LENGTH_SHORT).show();
        });
    }
    
    /**
     * 使用广播方式设置每日定时任务（更可靠）
     */
    private void setDailyAlarmWithBroadcast(AlarmManager alarmManager, int hour, int minute, int requestCode) {
        // 创建广播意图
        Intent broadcastIntent = new Intent(this, AlarmReceiver.class);
        broadcastIntent.setAction(AlarmReceiver.ACTION_START_DINGDING);
        broadcastIntent.putExtra(AlarmReceiver.EXTRA_TASK_TYPE, AlarmReceiver.TASK_TYPE_DAILY);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, requestCode, broadcastIntent, PendingIntent.FLAG_IMMUTABLE);

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
        
        android.util.Log.i("MainActivity", String.format("设置每日定时任务: %02d:%02d, 下次执行: %s", 
                hour, minute, android.text.format.DateFormat.format("yyyy-MM-dd HH:mm:ss", targetTime)));
    }
    
    /**
     * 请求通知权限
     */
    private void requestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }
    }
    
    /**
     * 启动前台服务保持应用运行
     */
    private void startKeepAliveService() {
        Intent serviceIntent = new Intent(this, SchedulerKeepAliveService.class);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }
    
    /**
     * 处理权限请求结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "通知权限已授予", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "通知权限被拒绝，可能无法显示定时任务通知", Toast.LENGTH_LONG).show();
            }
        }
    }
}
