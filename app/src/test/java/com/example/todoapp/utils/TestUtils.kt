package com.example.todoapp.utils

import com.example.todoapp.data.Priority
import com.example.todoapp.data.Todo
import java.util.Date

/**
 * 测试工具类，提供测试数据创建和验证方法
 */
object TestUtils {

    /**
     * 创建测试用的Todo对象
     */
    fun createTestTodo(
        id: Int = 1,
        title: String = "Test Todo",
        priority: Priority = Priority.MEDIUM,
        createdAt: Date = Date(),
        dueDate: Date? = null,
        dueTime: String? = null,
        isDaily: Boolean = false,
        dailyTime: String? = null,
        dailyEndDate: Date? = null,
        completedAt: Date? = null
    ): Todo {
        return Todo(
            id = id,
            title = title,
            priority = priority,
            createdAt = createdAt,
            dueDate = dueDate,
            dueTime = dueTime,
            isDaily = isDaily,
            dailyTime = dailyTime,
            dailyEndDate = dailyEndDate,
            completedAt = completedAt
        )
    }

    /**
     * 创建多个测试Todo对象
     */
    fun createTestTodos(count: Int): List<Todo> {
        return (1..count).map { index ->
            createTestTodo(
                id = index,
                title = "Test Todo $index",
                priority = when (index % 3) {
                    0 -> Priority.HIGH
                    1 -> Priority.MEDIUM
                    else -> Priority.LOW
                }
            )
        }
    }

    /**
     * 创建每日任务测试数据
     */
    fun createDailyTestTodo(
        id: Int = 1,
        title: String = "Daily Test Todo",
        dailyTime: String = "09:00",
        dailyEndDate: Date? = null
    ): Todo {
        return createTestTodo(
            id = id,
            title = title,
            isDaily = true,
            dailyTime = dailyTime,
            dailyEndDate = dailyEndDate
        )
    }

    /**
     * 创建已完成的测试Todo
     */
    fun createCompletedTestTodo(
        id: Int = 1,
        title: String = "Completed Test Todo",
        completedAt: Date = Date()
    ): Todo {
        return createTestTodo(
            id = id,
            title = title,
            completedAt = completedAt
        )
    }

    /**
     * 验证Todo对象的基本属性
     */
    fun assertTodoEquals(expected: Todo, actual: Todo) {
        assert(expected.id == actual.id) { "Todo ID mismatch: expected ${expected.id}, actual ${actual.id}" }
        assert(expected.title == actual.title) { "Todo title mismatch: expected ${expected.title}, actual ${actual.title}" }
        assert(expected.priority == actual.priority) { "Todo priority mismatch: expected ${expected.priority}, actual ${actual.priority}" }
        assert(expected.isDaily == actual.isDaily) { "Todo isDaily mismatch: expected ${expected.isDaily}, actual ${actual.isDaily}" }
    }

    /**
     * 验证Todo列表
     */
    fun assertTodoListEquals(expected: List<Todo>, actual: List<Todo>) {
        assert(expected.size == actual.size) { "Todo list size mismatch: expected ${expected.size}, actual ${actual.size}" }
        expected.zip(actual).forEach { (expectedTodo, actualTodo) ->
            assertTodoEquals(expectedTodo, actualTodo)
        }
    }
}
