package com.example.todoapp.utils

import com.example.todoapp.core.database.data.Priority
import com.example.todoapp.core.database.data.Todo
import java.util.Calendar
import java.util.Date

/**
 * Mock数据工厂，用于创建测试数据
 */
object MockDataFactory {

    /**
     * 创建高优先级Todo
     */
    fun createHighPriorityTodo(title: String = "High Priority Todo"): Todo {
        return TestUtils.createTestTodo(
            title = title,
            priority = Priority.HIGH
        )
    }

    /**
     * 创建中等优先级Todo
     */
    fun createMediumPriorityTodo(title: String = "Medium Priority Todo"): Todo {
        return TestUtils.createTestTodo(
            title = title,
            priority = Priority.MEDIUM
        )
    }

    /**
     * 创建低优先级Todo
     */
    fun createLowPriorityTodo(title: String = "Low Priority Todo"): Todo {
        return TestUtils.createTestTodo(
            title = title,
            priority = Priority.LOW
        )
    }

    /**
     * 创建带截止日期的Todo
     */
    fun createTodoWithDueDate(
        title: String = "Todo with Due Date",
        daysFromNow: Int = 1
    ): Todo {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, daysFromNow)
        return TestUtils.createTestTodo(
            title = title,
            dueDate = calendar.time
        )
    }

    /**
     * 创建带截止时间的Todo
     */
    fun createTodoWithDueTime(
        title: String = "Todo with Due Time",
        time: String = "14:30"
    ): Todo {
        return TestUtils.createTestTodo(
            title = title,
            dueTime = time
        )
    }

    /**
     * 创建每日任务
     */
    fun createDailyTodo(
        title: String = "Daily Todo",
        dailyTime: String = "09:00",
        endDateDaysFromNow: Int? = null
    ): Todo {
        val endDate = endDateDaysFromNow?.let { days ->
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_MONTH, days)
            calendar.time
        }
        
        return TestUtils.createTestTodo(
            title = title,
            isDaily = true,
            dailyTime = dailyTime,
            dailyEndDate = endDate
        )
    }

    /**
     * 创建已完成的Todo
     */
    fun createCompletedTodo(
        title: String = "Completed Todo",
        completedDaysAgo: Int = 1
    ): Todo {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, -completedDaysAgo)
        return TestUtils.createTestTodo(
            title = title,
            completedAt = calendar.time
        )
    }

    /**
     * 创建混合类型的Todo列表
     */
    fun createMixedTodoList(): List<Todo> {
        return listOf(
            createHighPriorityTodo("Urgent Task"),
            createMediumPriorityTodo("Regular Task"),
            createLowPriorityTodo("Low Priority Task"),
            createTodoWithDueDate("Task with Due Date", 2),
            createDailyTodo("Morning Exercise", "07:00"),
            createCompletedTodo("Finished Task")
        )
    }

    /**
     * 创建待办任务列表
     */
    fun createPendingTodoList(): List<Todo> {
        return listOf(
            createHighPriorityTodo("Pending High Priority"),
            createMediumPriorityTodo("Pending Medium Priority"),
            createLowPriorityTodo("Pending Low Priority"),
            createTodoWithDueDate("Pending with Due Date", 1)
        )
    }

    /**
     * 创建已完成任务列表
     */
    fun createCompletedTodoList(): List<Todo> {
        return listOf(
            createCompletedTodo("Completed Task 1", 1),
            createCompletedTodo("Completed Task 2", 2),
            createCompletedTodo("Completed Task 3", 3)
        )
    }

    /**
     * 创建每日任务列表
     */
    fun createDailyTodoList(): List<Todo> {
        return listOf(
            createDailyTodo("Morning Exercise", "07:00"),
            createDailyTodo("Evening Walk", "18:00"),
            createDailyTodo("Read Book", "20:00", 30) // 30天后结束
        )
    }
}
