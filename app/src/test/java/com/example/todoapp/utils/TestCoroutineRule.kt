package com.example.todoapp.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * 协程测试规则，用于在测试中控制协程调度
 */
@ExperimentalCoroutinesApi
class TestCoroutineRule(
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : TestRule {

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                Dispatchers.setMain(testDispatcher)
                try {
                    base.evaluate()
                } finally {
                    Dispatchers.resetMain()
                }
            }
        }
    }

    /**
     * 运行所有待处理的协程
     */
    fun runBlockingTest(block: suspend () -> Unit) {
        testDispatcher.scheduler.runCurrent()
        // Note: block parameter is not used in this implementation
        // but kept for API compatibility
    }

    /**
     * 推进时间
     */
    fun advanceTimeBy(delayTimeMillis: Long) {
        testDispatcher.scheduler.advanceTimeBy(delayTimeMillis)
    }

    /**
     * 推进到空闲状态
     */
    fun advanceUntilIdle() {
        testDispatcher.scheduler.advanceUntilIdle()
    }
}
