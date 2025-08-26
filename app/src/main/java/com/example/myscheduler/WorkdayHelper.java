package com.example.myscheduler;

import java.util.Calendar;

public class WorkdayHelper {
    
    /**
     * 检查指定日期是否为工作日（周一到周五）
     */
    public static boolean isWorkday(Calendar calendar) {
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        // Calendar.SUNDAY = 1, Calendar.MONDAY = 2, ..., Calendar.SATURDAY = 7
        // 工作日为周一(2)到周五(6)
        return dayOfWeek >= Calendar.MONDAY && dayOfWeek <= Calendar.FRIDAY;
    }
    
    /**
     * 检查当前是否为工作日
     */
    public static boolean isCurrentWorkday() {
        return isWorkday(Calendar.getInstance());
    }
    
    /**
     * 获取下一个工作日的指定时间
     * @param hour 小时 (0-23)
     * @param minute 分钟 (0-59)
     * @return 下一个工作日的指定时间的Calendar对象
     */
    public static Calendar getNextWorkdayTime(int hour, int minute) {
        Calendar now = Calendar.getInstance();
        Calendar target = Calendar.getInstance();
        
        // 设置目标时间
        target.set(Calendar.HOUR_OF_DAY, hour);
        target.set(Calendar.MINUTE, minute);
        target.set(Calendar.SECOND, 0);
        target.set(Calendar.MILLISECOND, 0);
        
        // 如果今天是工作日且时间未过，使用今天
        if (isWorkday(now) && target.getTimeInMillis() > now.getTimeInMillis()) {
            return target;
        }
        
        // 否则寻找下一个工作日
        target.add(Calendar.DAY_OF_MONTH, 1);
        while (!isWorkday(target)) {
            target.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        return target;
    }
    
    /**
     * 获取下一个工作日的9:25时间
     */
    public static Calendar getNext925Workday() {
        return getNextWorkdayTime(9, 25);
    }
    
    /**
     * 获取下一个工作日的18:35时间
     */
    public static Calendar getNext1835Workday() {
        return getNextWorkdayTime(18, 35);
    }
    
    /**
     * 获取最近的下一个工作日定时任务时间（9:25或18:35中最近的一个）
     */
    public static Calendar getNextWorkdayScheduleTime() {
        Calendar next925 = getNext925Workday();
        Calendar next1835 = getNext1835Workday();
        
        if (next925.getTimeInMillis() < next1835.getTimeInMillis()) {
            return next925;
        } else {
            return next1835;
        }
    }
    
    /**
     * 格式化工作日说明文本
     */
    public static String getWorkdayDescription() {
        return "周一至周五";
    }
    
    /**
     * 获取工作日时间说明
     */
    public static String getWorkdayTimeDescription() {
        return "工作日 9:25 和 18:35";
    }
    
    /**
     * 检查是否应该跳过当前时间的任务（周末跳过）
     */
    public static boolean shouldSkipCurrentTask() {
        return !isCurrentWorkday();
    }
    
    /**
     * 获取跳过任务的原因说明
     */
    public static String getSkipReason() {
        if (!isCurrentWorkday()) {
            return "今天是周末，跳过打卡任务";
        }
        return "";
    }
}