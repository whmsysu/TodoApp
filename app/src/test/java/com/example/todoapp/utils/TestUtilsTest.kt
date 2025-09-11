package com.example.todoapp.utils

import com.example.todoapp.data.Priority
import org.junit.Test
import org.junit.Assert.*

class TestUtilsTest {

    @Test
    fun `createTestTodo should create todo with correct properties`() {
        // Given
        val title = "Test Todo"
        val priority = Priority.HIGH

        // When
        val todo = TestUtils.createTestTodo(
            title = title,
            priority = priority
        )

        // Then
        assertEquals(title, todo.title)
        assertEquals(priority, todo.priority)
        assertFalse(todo.isDaily)
        assertNull(todo.completedAt)
    }

    @Test
    fun `createTestTodos should create correct number of todos`() {
        // Given
        val count = 5

        // When
        val todos = TestUtils.createTestTodos(count)

        // Then
        assertEquals(count, todos.size)
        todos.forEachIndexed { index, todo ->
            assertEquals("Test Todo ${index + 1}", todo.title)
        }
    }

    @Test
    fun `createDailyTestTodo should create daily todo`() {
        // Given
        val title = "Daily Task"
        val dailyTime = "09:00"

        // When
        val todo = TestUtils.createDailyTestTodo(
            title = title,
            dailyTime = dailyTime
        )

        // Then
        assertEquals(title, todo.title)
        assertTrue(todo.isDaily)
        assertEquals(dailyTime, todo.dailyTime)
    }

    @Test
    fun `createCompletedTestTodo should create completed todo`() {
        // Given
        val title = "Completed Task"

        // When
        val todo = TestUtils.createCompletedTestTodo(title = title)

        // Then
        assertEquals(title, todo.title)
        assertNotNull(todo.completedAt)
    }
}
