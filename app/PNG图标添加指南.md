# PNG图标文件添加指南

## 📋 需要添加的文件列表

### 标准图标 (ic_launcher.png)
请将你的图片文件调整为以下尺寸，并保存为PNG格式：

| 目录位置 | 文件名 | 尺寸 |
|---------|-------|------|
| `src/main/res/mipmap-mdpi/` | `ic_launcher.png` | 48×48 像素 |
| `src/main/res/mipmap-hdpi/` | `ic_launcher.png` | 72×72 像素 |
| `src/main/res/mipmap-xhdpi/` | `ic_launcher.png` | 96×96 像素 |
| `src/main/res/mipmap-xxhdpi/` | `ic_launcher.png` | 144×144 像素 |
| `src/main/res/mipmap-xxxhdpi/` | `ic_launcher.png` | 192×192 像素 |

### 圆形图标 (ic_launcher_round.png) - 可选
如果你想支持圆形图标，请添加相同尺寸的圆形版本：

| 目录位置 | 文件名 | 尺寸 |
|---------|-------|------|
| `src/main/res/mipmap-mdpi/` | `ic_launcher_round.png` | 48×48 像素 |
| `src/main/res/mipmap-hdpi/` | `ic_launcher_round.png` | 72×72 像素 |
| `src/main/res/mipmap-xhdpi/` | `ic_launcher_round.png` | 96×96 像素 |
| `src/main/res/mipmap-xxhdpi/` | `ic_launcher_round.png` | 144×144 像素 |
| `src/main/res/mipmap-xxxhdpi/` | `ic_launcher_round.png` | 192×192 像素 |

## 🛠️ 创建PNG文件的方法

### 方法1：使用在线工具
1. 访问 https://romannurik.github.io/AndroidAssetStudio/icons-launcher.html
2. 上传你的原始图片
3. 调整设置（颜色、形状、边距等）
4. 下载生成的ZIP文件
5. 解压并将文件放入对应目录

### 方法2：使用图片编辑软件
1. **准备原始图片**：确保图片质量高，背景透明或纯色
2. **调整尺寸**：使用Photoshop、GIMP或Paint.NET调整为所需尺寸
3. **保存为PNG**：确保保持透明背景
4. **重复所有尺寸**：为每个密度创建对应尺寸

### 方法3：使用Android Studio
1. 在Android Studio中右键点击 `res` 目录
2. 选择 `New > Image Asset`
3. 选择 `Launcher Icons (Adaptive and Legacy)`
4. 上传你的图片并调整设置
5. 自动生成所有尺寸

## 📁 目录结构
完成后，你的目录结构应该是：
```
src/main/res/
├── mipmap-mdpi/
│   ├── ic_launcher.png (48×48)
│   └── ic_launcher_round.png (48×48)
├── mipmap-hdpi/
│   ├── ic_launcher.png (72×72)
│   └── ic_launcher_round.png (72×72)
├── mipmap-xhdpi/
│   ├── ic_launcher.png (96×96)
│   └── ic_launcher_round.png (96×96)
├── mipmap-xxhdpi/
│   ├── ic_launcher.png (144×144)
│   └── ic_launcher_round.png (144×144)
└── mipmap-xxxhdpi/
    ├── ic_launcher.png (192×192)
    └── ic_launcher_round.png (192×192)
```

## ✅ 完成后检查
1. 确保所有PNG文件都已放置在正确位置
2. AndroidManifest.xml已配置好图标引用
3. 构建项目确认没有错误

## 💡 设计建议
- **保持一致性**：确保所有尺寸的图标视觉一致
- **适配性**：考虑图标在不同背景下的可见性
- **简洁性**：避免过于复杂的细节，小尺寸时可能看不清
- **品牌性**：确保图标体现应用的特色和功能

添加完PNG文件后，你的应用就会使用新的图标了！