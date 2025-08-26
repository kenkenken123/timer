package com.example.myscheduler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;
import android.widget.Toast;
import java.util.List;

public class AlarmReceiver extends BroadcastReceiver {
    
    private static final String TAG = "AlarmReceiver";
    public static final String ACTION_START_DINGDING = "com.example.myscheduler.START_DINGDING";
    public static final String EXTRA_TASK_TYPE = "task_type";
    public static final String TASK_TYPE_DAILY = "daily";
    public static final String TASK_TYPE_INSTANT = "instant";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "AlarmReceiver triggered: " + intent.getAction());
        
        if (ACTION_START_DINGDING.equals(intent.getAction())) {
            String taskType = intent.getStringExtra(EXTRA_TASK_TYPE);
            Log.d(TAG, "Task type: " + taskType);
            
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
                Log.e(TAG, "钉钉应用启动失败");
                showToast(context, "❌ 钉钉应用启动失败，请检查是否已安装");
                
                // 发送失败通知
                NotificationHelper notificationHelper = new NotificationHelper(context);
                notificationHelper.showExecutionFailureNotification();
            }
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
            Log.w(TAG, "包名启动失败: " + packageName + ", 错误: " + e.getMessage());
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
            Log.e(TAG, "Intent动作启动失败: " + e.getMessage());
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