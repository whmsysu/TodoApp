# 每日固定时间TODO功能

## 功能概述

新增了每日固定时间的TODO功能，允许用户创建每天在固定时间提醒的待办事项。

## 主要特性

### 1. 每日TODO创建
- 在添加/编辑TODO界面中，新增了"每日重复"开关
- 开启后可以设置具体的提醒时间（小时:分钟格式）
- 支持24小时制时间选择

### 2. 界面更新
- 添加TODO界面新增每日重复选项和时间选择器
- 主界面新增"每日"筛选选项，可以查看所有每日TODO
- TODO列表项显示每日提醒时间（格式：每日 HH:MM）

### 3. 通知提醒
- 使用WorkManager实现每日定时通知
- 支持精确时间提醒
- 通知包含TODO标题和描述信息
- 点击通知可直接打开应用

### 4. 数据管理
- 数据库新增字段：`isDaily`、`dailyTime`、`lastCompletedDate`
- 支持数据库版本迁移
- 自动管理通知的创建和取消

## 技术实现

### 数据模型
```kotlin
data class Todo(
    // ... 原有字段
    val isDaily: Boolean = false,
    val dailyTime: String? = null, // Format: "HH:mm"
    val lastCompletedDate: Date? = null
)
```

### 通知系统
- `TodoNotificationWorker`: 处理通知显示
- `TodoNotificationManager`: 管理通知调度
- 使用WorkManager的PeriodicWorkRequest实现每日重复

### 权限要求
- `POST_NOTIFICATIONS`: 发送通知
- `SCHEDULE_EXACT_ALARM`: 精确时间调度
- `USE_EXACT_ALARM`: 使用精确闹钟

## 使用方法

1. **创建每日TODO**：
   - 点击"+"按钮添加新TODO
   - 填写标题和描述
   - 开启"每日重复"开关
   - 选择提醒时间
   - 保存

2. **查看每日TODO**：
   - 在主界面点击"每日"筛选选项
   - 查看所有每日重复的TODO

3. **编辑每日TODO**：
   - 点击TODO项进入编辑界面
   - 可以修改时间或关闭每日重复
   - 保存后自动更新通知

4. **删除每日TODO**：
   - 删除TODO时自动取消相关通知

## 注意事项

- 每日TODO会在设定的时间每天重复提醒
- 通知权限需要在Android 13+设备上手动授权
- 删除TODO时会自动取消所有相关通知
- 修改每日TODO的时间会重新调度通知
