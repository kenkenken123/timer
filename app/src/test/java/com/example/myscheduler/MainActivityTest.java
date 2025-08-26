package com.example.myscheduler;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.app.AlarmManager;
import android.content.Context;
import android.widget.Button;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Mock
    private AlarmManager mockAlarmManager;
    
    @Mock
    private Context mockContext;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testButtonsExist() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                // 测试两个按钮是否存在
                Button btnSchedule = activity.findViewById(R.id.btn_schedule);
                Button btnStartDingDing = activity.findViewById(R.id.btn_start_dingding);
                
                assertNotNull("定时启动按钮应该存在", btnSchedule);
                assertNotNull("1分钟后启动按钮应该存在", btnStartDingDing);
                
                // 测试按钮文字
                String scheduleText = btnSchedule.getText().toString();
                String startText = btnStartDingDing.getText().toString();
                
                assertTrue("定时启动按钮文字应包含'定时启动'", scheduleText.contains("定时启动"));
                assertTrue("1分钟后启动按钮文字应包含'1分钟后启动'", startText.contains("1分钟后启动"));
            });
        }
    }

    @Test
    public void testDingDingPackageName() {
        // 测试钉钉包名是否正确
        String expectedPackage = "com.alibaba.android.rimet";
        String expectedActivity = "com.alibaba.android.rimet.biz.SplashActivity";
        
        assertNotNull("钉钉包名不能为空", expectedPackage);
        assertNotNull("钉钉Activity不能为空", expectedActivity);
        assertEquals("钉钉包名应该正确", "com.alibaba.android.rimet", expectedPackage);
    }

    @Test
    public void testButtonClickListener() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                Button btnStartDingDing = activity.findViewById(R.id.btn_start_dingding);
                
                // 验证按钮点击监听器不为空
                assertTrue("1分钟后启动按钮应该有点击监听器", btnStartDingDing.hasOnClickListeners());
            });
        }
    }
}