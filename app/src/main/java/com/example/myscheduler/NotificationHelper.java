package com.example.myscheduler;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class NotificationHelper {
    
    private static final String CHANNEL_ID = "scheduler_notifications";
    private static final int NOTIFICATION_ID = 1001;
    
    private Context context;
    private NotificationManagerCompat notificationManager;
    
    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = NotificationManagerCompat.from(context);
        createNotificationChannel();
    }
    
    /**
     * 创建通知渠道（Android 8.0+必需）
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelName = context.getString(R.string.notification_channel_name);
            String channelDescription = context.getString(R.string.notification_channel_description);
            
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    channelName,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(channelDescription);
            channel.setShowBadge(true);
            channel.enableLights(true);
            channel.enableVibration(false); // 不震动，避免打扰
            
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
    
    /**
     * 显示下次定时任务的通知
     */
    public void showNextScheduleNotification(long nextTriggerTime, String action) {
        // 格式化时间显示
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM月dd日 HH:mm", Locale.CHINA);
        String formattedTime = dateFormat.format(nextTriggerTime);
        
        // 计算距离下次触发的时间
        long currentTime = System.currentTimeMillis();
        long timeDiff = nextTriggerTime - currentTime;
        String timeLeft = formatTimeLeft(timeDiff);
        
        // 创建点击通知时打开应用的Intent
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        
        // 构建通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_recent_history) // 使用系统图标
                .setContentTitle("📅 下次定时任务")
                .setContentText(String.format("🕐 %s %s\n⏰ 还有 %s", formattedTime, action, timeLeft))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(String.format("🕐 执行时间：%s\n🎯 执行动作：%s\n⏰ 倒计时：%s", 
                                formattedTime, action, timeLeft)))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false) // 不自动取消，保持显示
                .setOngoing(true) // 设为持续通知，不能被滑动删除
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        
        // 显示通知
        try {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        } catch (SecurityException e) {
            // 处理通知权限被拒绝的情况
            e.printStackTrace();
        }
    }
    
    /**
     * 显示即时定时任务（1分钟后启动）的通知
     */
    public void showInstantScheduleNotification() {
        Calendar targetTime = Calendar.getInstance();
        targetTime.add(Calendar.MINUTE, 1);
        showNextScheduleNotification(targetTime.getTimeInMillis(), "启动钉钉应用");
    }
    
    /**
     * 显示工作日定时任务的通知
     */
    public void showWorkdayScheduleNotification() {
        // 获取下次工作日定时任务时间
        Calendar nextTime = WorkdayHelper.getNextWorkdayScheduleTime();
        showNextScheduleNotification(nextTime.getTimeInMillis(), "启动钉钉应用（仅工作日）");
    }
    
    /**
     * 显示每日定时任务的通知（保持兼容性，但实际使用工作日逻辑）
     */
    public void showDailyScheduleNotification() {
        showWorkdayScheduleNotification();
    }
    

    
    /**
     * 格式化剩余时间显示
     */
    private String formatTimeLeft(long timeDiffMs) {
        if (timeDiffMs <= 0) {
            return "即将执行";
        }
        
        long seconds = timeDiffMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return String.format("%d天%d小时", days, hours % 24);
        } else if (hours > 0) {
            return String.format("%d小时%d分钟", hours, minutes % 60);
        } else if (minutes > 0) {
            return String.format("%d分钟", minutes);
        } else {
            return String.format("%d秒", seconds);
        }
    }
    
    /**
     * 取消所有通知
     */
    public void cancelAllNotifications() {
        notificationManager.cancel(NOTIFICATION_ID);
    }
    
    /**
     * 显示任务执行成功通知
     */
    public void showExecutionSuccessNotification(String taskType) {
        String title = "✅ 定时任务执行成功";
        String content = String.format("🚀 %s任务已成功启动钉钉应用", 
                TASK_TYPE_DAILY.equals(taskType) ? "每日定时" : "即时");
        
        showSimpleNotification(title, content, true);
    }
    
    /**
     * 显示任务执行失败通知
     */
    public void showExecutionFailureNotification() {
        String title = "❌ 定时任务执行失败";
        String content = "🚫 无法启动钉钉应用，请检查是否已安装";
        
        showSimpleNotification(title, content, false);
    }
    
    /**
     * 显示简单通知
     */
    private void showSimpleNotification(String title, String content, boolean autoCancel) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH) // 提高优先级确保显示
                .setContentIntent(pendingIntent)
                .setAutoCancel(autoCancel)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        
        try {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
    
    // 添加常量定义
    private static final String TASK_TYPE_DAILY = "daily";
    private static final String TASK_TYPE_INSTANT = "instant";
    
    /**
     * 创建前台服务通知
     */
    public Notification createForegroundServiceNotification() {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        
        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_recent_history)
                .setContentTitle("🛡️ 定时任务保护服务")
                .setContentText("正在运行以确保定时任务正常执行")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false)
                .setOngoing(true)
                .build();
    }
}