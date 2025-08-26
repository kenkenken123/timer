package com.example.myscheduler;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import androidx.annotation.Nullable;

public class SchedulerKeepAliveService extends Service {
    
    private static final int FOREGROUND_SERVICE_ID = 2001;
    private NotificationHelper notificationHelper;
    private PowerManager.WakeLock wakeLock;
    
    @Override
    public void onCreate() {
        super.onCreate();
        notificationHelper = new NotificationHelper(this);
        
        // 获取WakeLock确保服务不被休眠
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "MyScheduler::KeepAliveWakeLock"
            );
            wakeLock.acquire();
        }
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 启动前台服务
        startForeground(FOREGROUND_SERVICE_ID, 
                notificationHelper.createForegroundServiceNotification());
        
        // 返回START_STICKY确保服务被系统杀死后会重启
        return START_STICKY;
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // 不支持绑定
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        // 释放WakeLock
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        
        // 停止前台服务
        stopForeground(true);
    }
    
    /**
     * 重启服务时保持前台状态
     */
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        
        // 当应用被从最近任务中移除时，重启服务
        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
        restartServiceIntent.setPackage(getPackageName());
        startService(restartServiceIntent);
    }
}