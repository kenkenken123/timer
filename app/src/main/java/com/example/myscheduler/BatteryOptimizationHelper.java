package com.example.myscheduler;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import androidx.appcompat.app.AlertDialog;

public class BatteryOptimizationHelper {
    
    private Context context;
    
    public BatteryOptimizationHelper(Context context) {
        this.context = context;
    }
    
    /**
     * 检查是否已经在电池优化白名单中
     */
    public boolean isIgnoringBatteryOptimizations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (powerManager != null) {
                return powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
            }
        }
        return true; // 低版本Android默认返回true
    }
    
    /**
     * 请求忽略电池优化
     */
    public void requestIgnoreBatteryOptimizations(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!isIgnoringBatteryOptimizations()) {
                showBatteryOptimizationDialog(activity);
            }
        }
    }
    
    /**
     * 显示电池优化说明对话框
     */
    private void showBatteryOptimizationDialog(Activity activity) {
        new AlertDialog.Builder(activity)
                .setTitle("⚡ 电池优化设置")
                .setMessage("为了确保定时任务正常执行，建议将本应用加入电池优化白名单。\\n\\n" +
                           "这样可以防止系统在后台清理应用，确保定时启动钉钉功能正常工作。")
                .setPositiveButton("前往设置", (dialog, which) -> {
                    openBatteryOptimizationSettings(activity);
                })
                .setNegativeButton("稍后再说", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
    }
    
    /**
     * 打开电池优化设置页面
     */
    private void openBatteryOptimizationSettings(Activity activity) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // 方式1：直接请求忽略电池优化权限
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                activity.startActivityForResult(intent, 1001);
            }
        } catch (Exception e) {
            // 如果上述方式失败，尝试打开应用设置页面
            try {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                activity.startActivity(intent);
            } catch (Exception ex) {
                // 最后的备选方案：打开电池优化设置
                try {
                    Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                    activity.startActivity(intent);
                } catch (Exception exx) {
                    exx.printStackTrace();
                }
            }
        }
    }
    
    /**
     * 显示自启动设置引导（针对不同厂商）
     */
    public void showAutoStartGuide(Activity activity) {
        String manufacturer = Build.MANUFACTURER.toLowerCase();
        String message = getAutoStartMessage(manufacturer);
        
        new AlertDialog.Builder(activity)
                .setTitle("🚀 自启动设置")
                .setMessage(message)
                .setPositiveButton("知道了", (dialog, which) -> dialog.dismiss())
                .show();
    }
    
    /**
     * 根据手机厂商返回对应的自启动设置说明
     */
    private String getAutoStartMessage(String manufacturer) {
        switch (manufacturer) {
            case "xiaomi":
                return "小米手机设置路径：\\n设置 → 应用设置 → 授权管理 → 自启动管理 → 找到MyScheduler并开启";
            case "huawei":
            case "honor":
                return "华为/荣耀手机设置路径：\\n设置 → 应用和服务 → 应用启动管理 → 找到MyScheduler → 手动管理 → 开启自动启动";
            case "oppo":
                return "OPPO手机设置路径：\\n设置 → 电池 → 应用耗电管理 → 应用自启动 → 找到MyScheduler并开启";
            case "vivo":
                return "VIVO手机设置路径：\\n设置 → 电池 → 后台应用管理 → 找到MyScheduler → 开启后台自启动";
            case "meizu":
                return "魅族手机设置路径：\\n设置 → 应用管理 → 权限管理 → 后台管理 → 找到MyScheduler并允许后台运行";
            case "samsung":
                return "三星手机设置路径：\\n设置 → 应用程序 → MyScheduler → 电池 → 优化电池使用 → 选择不优化";
            default:
                return "请在手机设置中找到应用管理或电池优化相关选项，将MyScheduler添加到白名单或允许自启动，以确保定时功能正常工作。";
        }
    }
    
    /**
     * 一键设置所有防杀死选项
     */
    public void setupKeepAlive(Activity activity) {
        // 检查并请求电池优化白名单
        if (!isIgnoringBatteryOptimizations()) {
            requestIgnoreBatteryOptimizations(activity);
        } else {
            // 如果已经在白名单中，显示自启动设置引导
            showAutoStartGuide(activity);
        }
    }
}