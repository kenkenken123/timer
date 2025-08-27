package com.example.myscheduler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;
import android.Manifest;
import android.app.TimePickerDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import androidx.appcompat.app.AlertDialog;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.view.View;
import android.provider.Settings;
import android.net.Uri;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int EXACT_ALARM_PERMISSION_REQUEST_CODE = 1002;
    private NotificationHelper notificationHelper;
    private BatteryOptimizationHelper batteryHelper;
    private boolean permissionRequested = false; // 添加权限请求状态标记

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        android.util.Log.d("MainActivity", "开始初始化 MainActivity");
        
        // 安全地初始化帮助类
        try {
            notificationHelper = new NotificationHelper(this);
            android.util.Log.d("MainActivity", "NotificationHelper 初始化成功");
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "NotificationHelper 初始化失败: " + e.getMessage());
            notificationHelper = null;
        }
        
        try {
            batteryHelper = new BatteryOptimizationHelper(this);
            android.util.Log.d("MainActivity", "BatteryOptimizationHelper 初始化成功");
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "BatteryOptimizationHelper 初始化失败: " + e.getMessage());
            batteryHelper = null;
        }
        
        // 延迟请求权限，避免启动时的权限请求循环
        android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        mainHandler.postDelayed(() -> {
            requestNotificationPermission();
            requestExactAlarmPermission();
            // 暂时注释掉电池优化设置，避免权限请求冲突
            // setupBatteryOptimizationLater();
        }, 1000); // 延迟1秒请求权限
        
        // 初始化UI组件
        initializeUI();
        
        android.util.Log.d("MainActivity", "MainActivity 初始化完成");
    }
    
    /**
     * 初始化UI组件
     */
    private void initializeUI() {
        LinearLayout btnStartDingDing = findViewById(R.id.btn_start_dingding);
        LinearLayout btnTestDingDing = findViewById(R.id.btn_test_dingding);
        
        if (btnStartDingDing == null || btnTestDingDing == null) {
            android.util.Log.e("MainActivity", "无法找到按钮控件");
            return;
        }
        
        // 设置按钮点击事件
        btnStartDingDing.setOnClickListener(v -> {
            android.util.Log.d("MainActivity", "点击定时启动按钮");
            showTimeSelectionDialog();
        });
        
        btnTestDingDing.setOnClickListener(v -> {
            android.util.Log.d("MainActivity", "点击测试按钮");
            testStartDingDing();
        });
        
        // 延迟设置工作日定时任务，避免启动时的权限问题
        android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        mainHandler.postDelayed(() -> {
            setupWorkdaySchedule();
        }, 2000); // 延迟2秒设置定时任务
    }
    
    /**
     * 显示时间选择对话框
     */
    private void showTimeSelectionDialog() {
        try {
            // 检查钉钉应用是否已安装
            if (!AlarmReceiver.isDingDingInstalled(this)) {
                showErrorWithCopy("❌ 未检测到钉钉应用，请先安装钉钉");
                return;
            }

            // 创建自定义的时间选择对话框
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View dialogView = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, null);
            
            // 创建简单的时间选择界面
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(50, 50, 50, 50);
            
            TextView titleText = new TextView(this);
            titleText.setText("选择延迟启动时间");
            titleText.setTextSize(18);
            titleText.setPadding(0, 0, 0, 30);
            layout.addView(titleText);
            
            // 分钟选择器
            NumberPicker minutePicker = new NumberPicker(this);
            minutePicker.setMinValue(1);
            minutePicker.setMaxValue(60);
            minutePicker.setValue(5);  // 默认值改为5分钟
            minutePicker.setWrapSelectorWheel(false);
            
            // 添加分钟标签
            TextView minuteLabel = new TextView(this);
            minuteLabel.setText("分钟后启动钉钉");
            minuteLabel.setTextSize(16);
            minuteLabel.setPadding(0, 20, 0, 0);
            
            layout.addView(minutePicker);
            layout.addView(minuteLabel);
            
            builder.setView(layout)
                .setTitle("定时启动钉钉")
                .setPositiveButton("确定", (dialog, which) -> {
                    int selectedMinutes = minutePicker.getValue();
                    startDelayedTask(selectedMinutes);
                })
                .setNegativeButton("取消", null)
                .show();
                
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "显示时间选择对话框失败: " + e.getMessage());
            showErrorWithCopy("❌ 对话框显示失败: " + e.getMessage());
        }
    }
    
    /**
     * 启动延迟定时任务
     */
    private void startDelayedTask(int delayMinutes) {
        try {
            // 检查精确闹钟权限
            if (!checkExactAlarmPermission()) {
                showErrorWithCopy("❌ 缺少精确闹钟权限，无法设置定时任务");
                requestExactAlarmPermission();
                return;
            }
            
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) {
                showErrorWithCopy("❌ 系统闹钟服务不可用");
                return;
            }
            
            // 计算延迟时间
            Calendar targetTime = Calendar.getInstance();
            targetTime.add(Calendar.MINUTE, delayMinutes);
            
            // 创建广播意图
            Intent broadcastIntent = new Intent(this, AlarmReceiver.class);
            broadcastIntent.setAction(AlarmReceiver.ACTION_START_DINGDING);
            broadcastIntent.putExtra(AlarmReceiver.EXTRA_TASK_TYPE, AlarmReceiver.TASK_TYPE_INSTANT);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this, 2001, broadcastIntent, PendingIntent.FLAG_IMMUTABLE);

            // 设置一次性闹钟
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

            String message = String.format("✅ %d分钟后将自动启动钉钉应用", delayMinutes);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            
            // 显示即时定时任务的通知
            if (notificationHelper != null) {
                notificationHelper.showInstantScheduleNotification();
            }
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "延迟任务设置失败: " + e.getMessage());
            showErrorWithCopy("❌ 设置失败: " + e.getMessage());
        }
    }
    
    /**
     * 显示错误信息并提供复制到剪贴板功能
     */
    private void showErrorWithCopy(String errorMessage) {
        try {
            // 显示Toast
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            
            // 复制错误信息到剪贴板
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                ClipData clip = ClipData.newPlainText("错误信息", errorMessage);
                clipboard.setPrimaryClip(clip);
                
                // 延迟显示复制成功的提示
                android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
                handler.postDelayed(() -> {
                    Toast.makeText(this, "📋 错误信息已复制到剪贴板", Toast.LENGTH_SHORT).show();
                }, 1500);
            }
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "显示错误信息失败: " + e.getMessage());
            // 如果复制失败，至少显示原始错误
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * 测试启动钉钉
     */
    private void testStartDingDing() {
        try {
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
            
            Toast.makeText(this, "🗂️ 正在测试启动钉钉...", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "测试启动失败: " + e.getMessage());
            showErrorWithCopy("❌ 测试失败: " + e.getMessage());
        }
    }

    /**
     * 请求通知权限
     */
    private void requestNotificationPermission() {
        // 避免重复请求权限
        if (permissionRequested) {
            android.util.Log.d("MainActivity", "权限已经请求过，跳过");
            return;
        }
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionRequested = true;
                android.util.Log.d("MainActivity", "请求通知权限");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE);
            } else {
                android.util.Log.d("MainActivity", "通知权限已经授予");
            }
        } else {
            android.util.Log.d("MainActivity", "Android版本低于13，无需请求通知权限");
        }
    }
    
    /**
     * 请求精确闹钟权限
     */
    private void requestExactAlarmPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                android.util.Log.d("MainActivity", "需要请求精确闹钟权限");
                try {
                    Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, EXACT_ALARM_PERMISSION_REQUEST_CODE);
                } catch (Exception e) {
                    android.util.Log.e("MainActivity", "无法打开精确闹钟权限设置页面: " + e.getMessage());
                    Toast.makeText(this, "⚠️ 请手动在设置中允许此应用设置精确闹钟", Toast.LENGTH_LONG).show();
                }
            } else {
                android.util.Log.d("MainActivity", "精确闹钟权限已经授予");
            }
        }
    }
    
    /**
     * 检查精确闹钟权限
     */
    private boolean checkExactAlarmPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            return alarmManager != null && alarmManager.canScheduleExactAlarms();
        }
        return true; // Android 12 以下不需要此权限
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
                android.util.Log.d("MainActivity", "通知权限被授予");
                Toast.makeText(this, "✅ 通知权限已授予", Toast.LENGTH_SHORT).show();
                
                // 权限授予后，尝试启动前台服务
                startKeepAliveServiceSafely();
                
                // 延迟设置电池优化，避免多个权限请求冲突
                android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
                handler.postDelayed(() -> {
                    setupBatteryOptimizationLater();
                }, 3000); // 延迟3秒
            } else {
                android.util.Log.d("MainActivity", "通知权限被拒绝");
                Toast.makeText(this, "⚠️ 通知权限被拒绝，可能无法显示定时任务通知", Toast.LENGTH_LONG).show();
                // 即使权限被拒绝，也不再重复请求其他权限
            }
        }
    }
    
    /**
     * 处理活动结果（用于处理精确闹钟权限设置）
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EXACT_ALARM_PERMISSION_REQUEST_CODE) {
            if (checkExactAlarmPermission()) {
                android.util.Log.d("MainActivity", "精确闹钟权限已授予");
                Toast.makeText(this, "✅ 精确闹钟权限已授予。现在可以设置定时任务了！", Toast.LENGTH_LONG).show();
            } else {
                android.util.Log.d("MainActivity", "精确闹钟权限仍未授予");
                Toast.makeText(this, "⚠️ 精确闹钟权限未授予，无法设置定时任务", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    /**
     * 安全地启动前台服务
     */
    private void startKeepAliveServiceSafely() {
        try {
            Intent serviceIntent = new Intent(this, SchedulerKeepAliveService.class);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
            android.util.Log.d("MainActivity", "前台服务启动成功");
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "前台服务启动失败: " + e.getMessage());
        }
    }
    
    /**
     * 延迟设置电池优化，避免权限请求冲突
     */
    private void setupBatteryOptimizationLater() {
        try {
            if (batteryHelper != null && !batteryHelper.isIgnoringBatteryOptimizations()) {
                android.util.Log.d("MainActivity", "尝试设置电池优化白名单");
                // 不直接调用setupKeepAlive，而是显示一个提示
                Toast.makeText(this, "⚠️ 建议将应用加入电池优化白名单，点击按钮设置", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "电池优化设置失败: " + e.getMessage());
        }
    }
    
    /**
     * 自动设置工作日定时任务
     */
    private void setupWorkdaySchedule() {
        try {
            android.util.Log.d("MainActivity", "开始设置工作日定时任务");
            
            // 检查钉钉应用是否已安装
            if (!AlarmReceiver.isDingDingInstalled(this)) {
                android.util.Log.w("MainActivity", "钉钉应用未安装，跳过设置定时任务");
                return;
            }

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) {
                android.util.Log.e("MainActivity", "AlarmManager服务不可用");
                return;
            }
            
            // 先取消已有的定时任务，避免重复设置
            cancelExistingAlarms(alarmManager);
            
            // 设置工作日上班打卡：9:25
            setWorkdayAlarmWithBroadcast(alarmManager, 9, 25, 1001);
            
            // 设置工作日下班打卡：18:35
            setWorkdayAlarmWithBroadcast(alarmManager, 18, 35, 1002);

            android.util.Log.i("MainActivity", "已自动设置工作日定时任务：9:25和18:35");
            
            // 显示下次定时任务的通知
            if (notificationHelper != null) {
                notificationHelper.showDailyScheduleNotification();
            }
            
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "设置工作日定时任务失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 使用广播方式设置工作日定时任务（更可靠）
     */
    private void setWorkdayAlarmWithBroadcast(AlarmManager alarmManager, int hour, int minute, int requestCode) {
        // 检查精确闹钟权限
        if (!checkExactAlarmPermission()) {
            android.util.Log.w("MainActivity", "缺少精确闹钟权限，跳过设置工作日定时任务");
            return;
        }
        
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
