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

        Button btnStartDingDing = findViewById(R.id.btn_start_dingding);
        Button btnTestDingDing = findViewById(R.id.btn_test_dingding);
        
        // 自动设置工作日定时任务
        setupWorkdaySchedule();

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

            Toast.makeText(this, getString(R.string.toast_instant_task_set), Toast.LENGTH_LONG).show();
            
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
            
            Toast.makeText(this, getString(R.string.toast_testing_start), Toast.LENGTH_SHORT).show();
        });
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
                Toast.makeText(this, getString(R.string.toast_notification_granted), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.toast_notification_denied), Toast.LENGTH_LONG).show();
            }
        }
    }
    
    /**
     * 自动设置工作日定时任务
     */
    private void setupWorkdaySchedule() {
        // 检查钉钉应用是否已安装
        if (!AlarmReceiver.isDingDingInstalled(this)) {
            android.util.Log.w("MainActivity", "钉钉应用未安装，跳过设置定时任务");
            return;
        }

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        
        // 先取消已有的定时任务，避免重复设置
        cancelExistingAlarms(alarmManager);
        
        // 设置工作日上班打卡：9:25
        setWorkdayAlarmWithBroadcast(alarmManager, 9, 25, 1001);
        
        // 设置工作日下班打卡：18:35
        setWorkdayAlarmWithBroadcast(alarmManager, 18, 35, 1002);

        android.util.Log.i("MainActivity", "已自动设置工作日定时任务：9:25和18:35");
        
        // 显示下次定时任务的通知
        notificationHelper.showDailyScheduleNotification();
    }
    
    /**
     * 使用广播方式设置工作日定时任务（更可靠）
     */
    private void setWorkdayAlarmWithBroadcast(AlarmManager alarmManager, int hour, int minute, int requestCode) {
        // 创建广播意图
        Intent broadcastIntent = new Intent(this, AlarmReceiver.class);
        broadcastIntent.setAction(AlarmReceiver.ACTION_START_DINGDING);
        broadcastIntent.putExtra(AlarmReceiver.EXTRA_TASK_TYPE, AlarmReceiver.TASK_TYPE_DAILY);
        broadcastIntent.putExtra(AlarmReceiver.EXTRA_HOUR, hour);
        broadcastIntent.putExtra(AlarmReceiver.EXTRA_MINUTE, minute);
        broadcastIntent.putExtra(AlarmReceiver.EXTRA_REQUEST_CODE, requestCode);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, requestCode, broadcastIntent, PendingIntent.FLAG_IMMUTABLE);

        // 获取下一个工作日的指定时间
        Calendar targetTime = WorkdayHelper.getNextWorkdayTime(hour, minute);
        
        // 使用精确且允许在休眠时执行的定时任务（更可靠）
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                targetTime.getTimeInMillis(),
                pendingIntent
            );
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                targetTime.getTimeInMillis(),
                pendingIntent
            );
        }
        
        android.util.Log.i("MainActivity", String.format("设置工作日定时任务: %02d:%02d, 下次执行: %s", 
                hour, minute, android.text.format.DateFormat.format("yyyy-MM-dd HH:mm:ss", targetTime)));
    }
    
    /**
     * 取消已有的定时任务
     */
    private void cancelExistingAlarms(AlarmManager alarmManager) {
        try {
            // 取消9:25的定时任务
            Intent intent1 = new Intent(this, AlarmReceiver.class);
            intent1.setAction(AlarmReceiver.ACTION_START_DINGDING);
            PendingIntent pendingIntent1 = PendingIntent.getBroadcast(
                    this, 1001, intent1, PendingIntent.FLAG_IMMUTABLE);
            alarmManager.cancel(pendingIntent1);
            
            // 取消18:35的定时任务
            Intent intent2 = new Intent(this, AlarmReceiver.class);
            intent2.setAction(AlarmReceiver.ACTION_START_DINGDING);
            PendingIntent pendingIntent2 = PendingIntent.getBroadcast(
                    this, 1002, intent2, PendingIntent.FLAG_IMMUTABLE);
            alarmManager.cancel(pendingIntent2);
            
            android.util.Log.i("MainActivity", "已取消已有的定时任务");
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "取消定时任务失败: " + e.getMessage());
        }
    }
}
