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
    private static final String CHANNEL_NAME = "定时任务通知";
    private static final String CHANNEL_DESCRIPTION = "显示下次定时任务的执行时间";
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
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
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
     * 显示每日定时任务的通知
     */
    public void showDailyScheduleNotification() {
        // 计算下次9:25或18:35的时间
        Calendar now = Calendar.getInstance();
        Calendar next925 = getNextScheduleTime(9, 25);
        Calendar next1835 = getNextScheduleTime(18, 35);
        
        // 选择最近的一个时间
        Calendar nextTime;
        if (next925.getTimeInMillis() < next1835.getTimeInMillis()) {
            nextTime = next925;
        } else {
            nextTime = next1835;
        }
        
        showNextScheduleNotification(nextTime.getTimeInMillis(), "启动钉钉应用");
    }
    
    /**
     * 计算指定时间的下次触发时间
     */
    private Calendar getNextScheduleTime(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        Calendar targetTime = Calendar.getInstance();
        
        targetTime.set(Calendar.HOUR_OF_DAY, hour);
        targetTime.set(Calendar.MINUTE, minute);
        targetTime.set(Calendar.SECOND, 0);
        targetTime.set(Calendar.MILLISECOND, 0);
        
        // 如果今天的指定时间已经过了，设置为明天的同一时间
        if (targetTime.getTimeInMillis() <= calendar.getTimeInMillis()) {
            targetTime.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        return targetTime;
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