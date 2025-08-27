package com.example.myscheduler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;
import android.widget.Toast;
import android.content.ClipData;
import android.content.ClipboardManager;
import java.util.List;
import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {
    
    private static final String TAG = "AlarmReceiver";
    public static final String ACTION_START_DINGDING = "com.example.myscheduler.START_DINGDING";
    public static final String EXTRA_TASK_TYPE = "task_type";
    public static final String EXTRA_HOUR = "hour";
    public static final String EXTRA_MINUTE = "minute";
    public static final String EXTRA_REQUEST_CODE = "request_code";
    public static final String TASK_TYPE_DAILY = "daily";
    public static final String TASK_TYPE_INSTANT = "instant";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "AlarmReceiver triggered: " + intent.getAction());
        try{
            
        if (ACTION_START_DINGDING.equals(intent.getAction())) {
            String taskType = intent.getStringExtra(EXTRA_TASK_TYPE);
            Log.d(TAG, "Task type: " + taskType);
            
            // 检查是否为工作日（仅对每日定时任务检查）
            if (TASK_TYPE_DAILY.equals(taskType) && WorkdayHelper.shouldSkipCurrentTask()) {
                Log.i(TAG, "Weekend detected, skipping task");
                showToast(context, context.getString(R.string.toast_weekend_skip));
                
                // 即使跳过任务，也要重新设置下一次定时任务
                rescheduleNextWorkdayTask(context, intent);
                return;
            }
            
            // 启动钉钉应用
            boolean success = startDingDingApp(context);
            
            // 记录执行结果
            if (success) {
                Log.i(TAG, "钉钉应用启动成功");
                showToast(context, "✅ 钉钉应用已启动");
                
                // 发送成功通知
                NotificationHelper notificationHelper = new NotificationHelper(context);
                notificationHelper.showExecutionSuccessNotification(taskType);
            } else {
                String errorMsg = "❌ 钉钉应用启动失败，请检查是否已安装";
                Log.e(TAG, "钉钉应用启动失败");
                showToastWithCopy(context, errorMsg);
                
                // 发送失败通知
                NotificationHelper notificationHelper = new NotificationHelper(context);
                notificationHelper.showExecutionFailureNotification();
            }
            
            // 如果是每日定时任务，执行完后重新设置下一次定时任务
            if (TASK_TYPE_DAILY.equals(taskType)) {
                rescheduleNextWorkdayTask(context, intent);
            }
        }
        } catch (Exception e) {
            String errorMsg = "❌ AlarmReceiver处理异常: " + e.getMessage();
            Log.e(TAG, "AlarmReceiver处理异常: " + e.getMessage(), e);
            showToastWithCopy(context, errorMsg);
        }
    }
    
    /**
     * 启动钉钉应用的多种尝试方式
     */
    private boolean startDingDingApp(Context context) {
        String[] possiblePackages = {
            "com.alibaba.android.rimet",      // 钉钉官方包名
            "com.alibaba.android.rimet.free", // 钉钉免费版
            "com.dingtalk.android"            // 备用包名
        };
        
        for (String packageName : possiblePackages) {
            if (tryStartByPackageName(context, packageName)) {
                Log.i(TAG, "成功通过包名启动: " + packageName);
                return true;
            }
        }
        
        // 如果上述方式都失败，尝试通过Intent类型启动
        return tryStartByIntentAction(context);
    }
    
    /**
     * 通过包名启动应用
     */
    private boolean tryStartByPackageName(Context context, String packageName) {
        try {
            PackageManager pm = context.getPackageManager();
            Intent launchIntent = pm.getLaunchIntentForPackage(packageName);
            
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                
                context.startActivity(launchIntent);
                Log.d(TAG, "使用包名启动成功: " + packageName);
                return true;
            }
        } catch (Exception e) {
            String errorMsg = "❌ 包名启动失败: " + packageName + ", 错误: " + e.getMessage();
            Log.w(TAG, errorMsg);
        }
        return false;
    }
    
    /**
     * 通过Intent动作启动（备用方式）
     */
    private boolean tryStartByIntentAction(Context context) {
        try {
            // 尝试通过常用的业务类应用Intent启动
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            
            PackageManager pm = context.getPackageManager();
            List<ResolveInfo> activities = pm.queryIntentActivities(intent, 0);
            
            for (ResolveInfo info : activities) {
                String packageName = info.activityInfo.packageName;
                String appName = info.loadLabel(pm).toString();
                
                // 检查是否是钉钉相关应用
                if (packageName.contains("dingtalk") || packageName.contains("rimet") || 
                    appName.contains("钉钉") || appName.contains("DingTalk")) {
                    
                    Intent launchIntent = new Intent(Intent.ACTION_MAIN);
                    launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                    launchIntent.setClassName(packageName, info.activityInfo.name);
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    
                    context.startActivity(launchIntent);
                    Log.d(TAG, "通过Intent动作启动成功: " + packageName);
                    return true;
                }
            }
        } catch (Exception e) {
            String errorMsg = "❌ Intent动作启动失败: " + e.getMessage();
            Log.e(TAG, errorMsg);
        }
        return false;
    }
    
    /**
     * 显示Toast消息（需要在主线程中执行）
     */
    private void showToast(Context context, String message) {
        try {
            // 创建一个Intent来显示Toast，避免在广播接收器中直接显示
            Intent toastIntent = new Intent(context, ToastService.class);
            toastIntent.putExtra("message", message);
            context.startService(toastIntent);
        } catch (Exception e) {
            Log.e(TAG, "显示Toast失败: " + e.getMessage());
        }
    }
    
    /**
     * 显示Toast消息并复制到剪贴板
     */
    private void showToastWithCopy(Context context, String message) {
        try {
            // 显示Toast
            showToast(context, message);
            
            // 复制错误信息到剪贴板
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                ClipData clip = ClipData.newPlainText("错误信息", message);
                clipboard.setPrimaryClip(clip);
                
                // 延迟显示复制成功的提示
                android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
                handler.postDelayed(() -> {
                    showToast(context, "📋 错误信息已复制到剪贴板");
                }, 1500);
            }
        } catch (Exception e) {
            Log.e(TAG, "显示错误信息失败: " + e.getMessage());
            // 如果复制失败，至少显示原始错误
            showToast(context, message);
        }
    }
    
    /**
     * 重新设置下一次工作日定时任务
     */
    private void rescheduleNextWorkdayTask(Context context, Intent originalIntent) {
        try {
            int hour = originalIntent.getIntExtra(EXTRA_HOUR, 9);
            int minute = originalIntent.getIntExtra(EXTRA_MINUTE, 25);
            int requestCode = originalIntent.getIntExtra(EXTRA_REQUEST_CODE, 1001);
            
            android.app.AlarmManager alarmManager = (android.app.AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) {
                Log.e(TAG, "AlarmManager服务不可用");
                return;
            }
            
            // 创建新的广播意图
            Intent broadcastIntent = new Intent(context, AlarmReceiver.class);
            broadcastIntent.setAction(ACTION_START_DINGDING);
            broadcastIntent.putExtra(EXTRA_TASK_TYPE, TASK_TYPE_DAILY);
            broadcastIntent.putExtra(EXTRA_HOUR, hour);
            broadcastIntent.putExtra(EXTRA_MINUTE, minute);
            broadcastIntent.putExtra(EXTRA_REQUEST_CODE, requestCode);

            android.app.PendingIntent pendingIntent = android.app.PendingIntent.getBroadcast(
                    context, requestCode, broadcastIntent, android.app.PendingIntent.FLAG_IMMUTABLE);

            // 获取下一个工作日的指定时间
            java.util.Calendar targetTime = WorkdayHelper.getNextWorkdayTime(hour, minute);
            
            // 设置下一次定时任务
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    android.app.AlarmManager.RTC_WAKEUP,
                    targetTime.getTimeInMillis(),
                    pendingIntent
                );
            } else {
                alarmManager.setExact(
                    android.app.AlarmManager.RTC_WAKEUP,
                    targetTime.getTimeInMillis(),
                    pendingIntent
                );
            }
            
            Log.i(TAG, String.format("重新设置下一次工作日定时任务: %02d:%02d, 下次执行: %s", 
                    hour, minute, android.text.format.DateFormat.format("yyyy-MM-dd HH:mm:ss", targetTime)));
                    
            // 更新通知显示下次执行时间
            NotificationHelper notificationHelper = new NotificationHelper(context);
            notificationHelper.showDailyScheduleNotification();
            
        } catch (Exception e) {
            String errorMsg = "❌ 重新设置定时任务失败: " + e.getMessage();
            Log.e(TAG, errorMsg, e);
        }
    }

    /**
     * 检查钉钉应用是否已安装
     */
    public static boolean isDingDingInstalled(Context context) {
        String[] possiblePackages = {
            "com.alibaba.android.rimet",
            "com.alibaba.android.rimet.free",
            "com.dingtalk.android"
        };
        
        PackageManager pm = context.getPackageManager();
        for (String packageName : possiblePackages) {
            try {
                pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
                return true;
            } catch (PackageManager.NameNotFoundException e) {
                // 继续检查下一个包名
            }
        }
        return false;
    }
}