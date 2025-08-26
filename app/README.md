# MySchedulerApp - Android 应用定时启动器

## 📱 项目概述

MySchedulerApp 是一个专业的 Android 应用程序，专为实现定时启动钉钉应用而设计。该应用提供了一个简单易用的界面，支持每日定时启动和即时定时启动两种模式。

## ✨ 核心功能

- **每日定时启动**：每天9:25和18:35自动启动钉钉应用
- **🆕 即时定时启动**：点击按钮后1分钟自动启动钉钉应用 ⭐
- **🔔 智能通知栏**：实时显示下次触发时间和动作 🆕
- **🛡️ 防杀死保护**：多重机制确保应用不被系统杀死 🆕
- **智能时间管理**：如果当天时间已过，自动设置为次日同一时间
- **系统级调度**：使用 Android 原生 `AlarmManager` 确保可靠的定时执行
- **双时间点支持**：支持一天内两个不同时间点的定时任务
- **简洁界面**：Material Design 风格的用户界面
- **即时反馈**：通过 Toast 和通知消息提供用户操作反馈

## 🎯 使用场景

### 1. 每日定时模式
- **工作日打卡**：固定时间自动启动钉钉进行打卡
- **日程提醒**：定时启动钉钉查看当日工作安排
- **习惯养成**：培养固定时间查看工作信息的习惯

### 2. 即时定时模式 🆕
- **临时延迟**：需要稍后启动钉钉但不想忘记
- **会议准备**：设置1分钟缓冲时间准备加入会议
- **快速测试**：验证定时启动功能是否正常工作

## 🏗️ 技术架构

### 技术栈
- **开发语言**：Java
- **框架**：Android 原生开发
- **UI库**：AndroidX + Material Design
- **最小支持版本**：Android 7.0 (API 24)
- **目标版本**：Android 14 (API 34)

### 项目结构
```
app/
├── src/main/
│   ├── java/com/example/myscheduler/
│   │   └── MainActivity.java          # 主活动类
│   ├── res/layout/
│   │   └── activity_main.xml         # 主界面布局
│   └── AndroidManifest.xml           # 应用清单文件
├── build.gradle                      # 模块构建配置
└── README.md                        # 项目文档
```

### 核心组件

#### MainActivity.java
- **功能**：应用主界面控制器
- **职责**：
  - 初始化UI组件（包括新增的1分钟启动按钮）
  - 处理双按钮点击事件
  - 配置双时间点的 AlarmManager 定时任务
  - 🆕 处理1分钟后启动的一次性定时任务
  - 创建 PendingIntent 用于启动钉钉应用
  - 智能处理时间计算（当日时间已过则设为次日）
- **关键特性**：
  - 使用 `AlarmManager.setRepeating()` 实现每日重复
  - 🆕 使用 `AlarmManager.setExact()` 实现精确的一次性定时
  - 支持多个定时任务（9:25、18:35、即时1分钟）
  - 使用不同的requestCode区分不同任务
  - 自动计算下次触发时间

#### activity_main.xml
- **功能**：主界面布局定义
- **设计**：
  - 简洁的垂直线性布局
  - 居中对齐的双按钮界面
  - Material Design 风格按钮
  - 🆕 绿色突出显示的1分钟启动按钮
  - 按钮文字明确显示功能描述

## ⚙️ 配置说明

### 构建配置 (build.gradle)
```gradle
android {
    compileSdk 34           # 编译SDK版本
    minSdk 24              # 最低支持版本
    targetSdk 34           # 目标SDK版本
    versionCode 1          # 版本号
    versionName "1.0"      # 版本名称
}

dependencies {
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
}
```

### 钉钉应用配置
当前配置启动钉钉应用：
```java
String targetPackage = "com.alibaba.android.rimet";
String targetActivity = "com.alibaba.android.rimet.biz.SplashActivity";
```

### 定时时间配置
当前设置的两个定时时间点：
- **上班时间**：每天 9:25
- **下班时间**：每天 18:35

## 🚀 快速开始

### 环境要求
- Android Studio (推荐最新版本)
- JDK 11 或更高版本
- Android SDK API 34
- 测试设备或模拟器 (Android 7.0+)

### 安装步骤
1. **克隆项目**
   ```bash
   # 如果有Git仓库
   git clone [项目地址]
   ```

2. **打开项目**
   - 使用 Android Studio 打开项目根目录
   - 等待 Gradle 同步完成

3. **构建项目**
   ```bash
   ./gradlew assembleDebug
   ```

4. **运行应用**
   - 连接 Android 设备或启动模拟器
   - 在 Android Studio 中点击 "Run" 按钮

## 📝 使用方法

### 每日定时启动模式
1. **启动应用**：在设备上打开 MySchedulerApp
2. **设置定时任务**：点击"设置钉钉定时启动(每日9:25和18:35)"按钮
3. **确认设置**：查看 Toast 提示信息确认任务已设置
4. **查看通知**：通知栏将显示下次执行时间和倒计时 🆕
5. **自动执行**：系统将在每天9:25和18:35自动启动钉钉应用

### 即时定时启动模式 🆕
1. **点击绿色按钮**：点击"1分钟后启动钉钉"按钮
2. **查看通知**：通知栏立即显示1分钟倒计时 🆕
3. **等待执行**：系统将在1分钟后自动启动钉钉应用
4. **Toast确认**：查看"1分钟后将自动启动钉钉应用"提示
5. **一次性执行**：该定时任务仅执行一次，不会重复

### 智能通知栏功能 🆕
- **📅 执行时间显示**：清楚显示下次定时任务的具体时间
- **⏰ 实时倒计时**：显示距离执行还有多长时间
- **🎯 动作说明**：明确显示将要执行的动作（启动钉钉）
- **📌 持续提醒**：通知保持显示，不会被意外清除
- **👆 快速访问**：点击通知可直接打开应用

### 防杀死保护功能 🆕
1. **自动启动前台服务**：应用启动时自动开启保护服务
2. **电池优化设置**：首次使用时引导加入白名单
3. **厂商适配指导**：针对小米、华为等提供具体设置路径
4. **多重保护机制**：
   - 🛡️ 前台服务保持运行
   - 🔋 电池优化白名单
   - 🚀 自启动权限设置
   - 💤 WakeLock防休眠

### 权限授予流程 🆕
1. **通知权限**：首次启动时请求通知显示权限
2. **电池优化**：引导用户将应用加入白名单
3. **自启动设置**：根据手机品牌提供设置指导
4. **完成设置**：所有权限设置完成后功能正常工作

## ⚙️ 权限配置

### AndroidManifest.xml 权限 🆕
```
<!-- 设置闹钟权限 -->
<uses-permission android:name="android.permission.SET_ALARM" />
<!-- 查询包信息权限（Android 11+） -->
<uses-permission android:name="android.permission.QUERY_ALL_PACKAGES" />
<!-- 启动其他应用的权限 -->
<uses-permission android:name="android.permission.WAKE_LOCK" />
```

这些权限确保应用能够：
- 设置系统级定时任务
- 查询并启动钉钉应用
- 在设备休眠时唤醒执行任务

## 🔧 自定义配置

### 修改目标应用
在 `MainActivity.java` 中修改以下代码：
```java
// 更改为你要启动的应用包名和Activity
String targetPackage = "your.target.package";
String targetActivity = "your.target.activity.ClassName";
```

### 修改定时时间
调整定时时间点：
```java
// 修改上班时间（小时，分钟）
setDailyAlarm(alarmManager, targetPackage, targetActivity, 9, 25, 1001);

// 修改下班时间（小时，分钟）
setDailyAlarm(alarmManager, targetPackage, targetActivity, 18, 35, 1002);
```

## ⚠️ 权限说明

当前应用无需特殊权限，使用的都是系统标准API：
- `AlarmManager`：用于设置定时任务
- `PendingIntent`：用于延时执行Intent
- 启动其他应用：使用标准Intent机制

## 🛠️ 开发计划

### 已实现功能 ✅
- [x] 每日定时启动钉钉功能（9:25和18:35）
- [x] 🆕 1分钟后即时启动钉钉功能
- [x] 智能时间计算和管理
- [x] 双时间点定时任务支持
- [x] 🆕 一次性精确定时任务支持
- [x] 简洁的用户界面
- [x] 🆕 双按钮布局设计
- [x] Toast反馈机制
- [x] 🆕 完整的Android权限配置
- [x] 🆕 单元测试覆盖

### 计划功能 📋
- [ ] 自定义定时时间选择界面
- [ ] 支持更多目标应用配置
- [ ] 定时任务管理和取消功能
- [ ] 任务执行历史记录
- [ ] 周末/工作日不同设置
- [ ] 节假日智能跳过功能
- [ ] 🆕 可配置的延迟时间（不限于1分钟）

## 🧪 测试说明

### 单元测试 🆕
项目包含完整的单元测试用例：
```
# 运行测试
./gradlew test

# 测试覆盖内容
- 按钮存在性验证
- 按钮文字内容检查
- 点击监听器验证
- 钉钉包名正确性测试
```

### 手动测试建议
1. **即时启动测试**：点击绿色按钮，1分钟后验证钉钉是否启动
2. **每日定时测试**：设置系统时间接近9:25或18:35进行验证
3. **权限测试**：首次安装后确认所有权限正常授予

## 🐛 已知问题

1. **固定延迟时间**：即时启动功能目前固定为1分钟，未来计划支持自定义
2. **时间点固定**：每日定时时间点（9:25和18:35）写死在代码中
3. **无任务管理**：无法查看或取消已设置的定时任务
4. **无节假日处理**：不会自动跳过周末或法定节假日

## 📈 版本历史

### v1.1.0 - 即时启动版本 🆕
- ✅ 新增"1分钟后启动钉钉"功能
- ✅ 改进UI布局，添加绿色按钮设计
- ✅ 添加完整的Android权限配置
- ✅ 完善单元测试覆盖
- ✅ 更新项目文档

### v1.0.0 - 基础版本
- ✅ 每日定时启动钉钉功能（9:25和18:35）
- ✅ 智能时间计算和管理
- ✅ 简洁的用户界面
- ✅ Toast反馈机制

## 📄 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request 来改进项目：

1. Fork 本仓库
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 📞 联系方式

如有问题或建议，请通过以下方式联系：
- 提交 GitHub Issue
- 发送邮件至：[您的邮箱]

---

**注意**：本应用仅供学习和研究使用，请遵守相关法律法规和第三方应用的使用条款。