# GitHub Actions 构建指南

## 问题说明

如果您在GitHub Actions中遇到以下错误：

```bash
chmod: cannot access 'gradlew': No such file or directory
```

这是因为项目缺少 Gradle Wrapper 文件。

## 解决方案

### 方案1：使用提供的 GitHub Actions 工作流

我已经为您创建了一个完整的 GitHub Actions 工作流文件 (`.github/workflows/android.yml`)，它会自动处理缺失的 Gradle Wrapper 文件。

### 方案2：本地生成 Gradle Wrapper 文件

如果您想在本地生成完整的 Gradle Wrapper 文件，请按以下步骤操作：

1. **确保已安装 Gradle**
   ```bash
   # 检查 Gradle 是否已安装
   gradle --version
   ```

2. **生成 Wrapper 文件**
   ```bash
   # 在项目根目录执行
   gradle wrapper --gradle-version 8.0
   ```

3. **提交到版本控制**
   ```bash
   git add gradle/
   git add gradlew
   git add gradlew.bat
   git add gradle.properties
   git commit -m "Add Gradle Wrapper files"
   ```

### 方案3：下载官方 Wrapper JAR 文件

如果上述方法不可行，您可以手动下载 `gradle-wrapper.jar` 文件：

```bash
# 创建目录
mkdir -p gradle/wrapper

# 下载官方 JAR 文件
curl -L -o gradle/wrapper/gradle-wrapper.jar \
  https://github.com/gradle/gradle/raw/v8.0.0/gradle/wrapper/gradle-wrapper.jar
```

## 项目结构

完整的 Gradle Wrapper 应该包含以下文件：

```
MySchedulerApp/
├── .github/
│   └── workflows/
│       └── android.yml          # GitHub Actions 工作流
├── app/
│   ├── build.gradle            # 应用模块构建脚本
│   └── src/                    # 源代码目录
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar  # Wrapper JAR 文件
│       └── gradle-wrapper.properties  # Wrapper 配置
├── gradle.properties           # 项目属性
├── gradlew                     # Unix/Linux 脚本
├── gradlew.bat                 # Windows 批处理文件
└── settings.gradle             # 项目设置
```

## GitHub Actions 工作流特性

提供的工作流文件包含以下功能：

- ✅ 自动设置 Java 17 环境
- ✅ 自动设置 Android SDK
- ✅ Gradle 缓存优化
- ✅ 自动生成缺失的 Gradle Wrapper
- ✅ 构建 Debug APK
- ✅ 运行测试
- ✅ 上传构建产物

## 注意事项

1. **文件权限**：在 Linux/Unix 系统中，确保 `gradlew` 文件有执行权限
2. **版本兼容**：确保 Gradle 版本与 Android Gradle Plugin 版本兼容
3. **网络访问**：GitHub Actions 需要网络访问来下载依赖

## 故障排除

如果仍然遇到问题，请检查：

1. **项目结构是否正确**
2. **build.gradle 文件是否存在语法错误**
3. **网络连接是否正常**
4. **GitHub Actions 日志中的详细错误信息**

## 联系支持

如果问题仍然存在，请：
1. 检查 GitHub Actions 的完整日志
2. 确认所有必需的文件都已正确提交到版本控制
3. 验证 Android SDK 版本配置是否正确