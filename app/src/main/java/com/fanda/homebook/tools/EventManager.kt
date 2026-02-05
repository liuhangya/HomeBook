package com.fanda.homebook.tools

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * 事件类型常量定义
 * 用于标识不同的事件，便于在接收端进行区分和处理
 */
object EventType {
    /**
     * 刷新事件
     * 通常用于通知UI更新或重新加载数据
     */
    const val REFRESH = 0
    // 可以继续添加其他事件类型，例如：
    // const val LOGIN_SUCCESS = 1
    // const val DATA_CHANGED = 2
    // const val NETWORK_STATE_CHANGED = 3
}

/**
 * 事件实体类
 * 用于封装事件信息，包含事件类型和附加数据
 *
 * @property type 事件类型，对应 EventType 中定义的常量
 * @property data 事件携带的数据，可以为任意类型，使用 Any? 以支持多种数据类型
 *               建议：在实际使用时进行类型安全转换
 */
data class EventEntity(
    val type: Int,
    val data: Any?
)

/**
 * 事件管理单例对象
 * 基于 Kotlin Flow 实现的轻量级事件总线
 * 用于组件间通信，解耦发送者和接收者
 *
 * 特性：
 * 1. 线程安全：所有操作都在协程上下文中进行
 * 2. 缓冲机制：防止事件丢失，可配置缓冲策略
 * 3. 支持延迟发送：避免事件发送过于频繁
 * 4. 支持多个订阅者：每个订阅者都会收到事件
 */
object EventManager {
    /**
     * 内部可变共享流，作为事件总线的核心
     *
     * 配置参数说明：
     * - replay = 0：新订阅者不会收到历史事件，只接收订阅后发送的事件
     * - extraBufferCapacity = 64：设置额外的缓冲区容量为64个事件
     * - onBufferOverflow = BufferOverflow.DROP_OLDEST：缓冲区满时丢弃最旧的事件
     *
     * 适用场景：适合高频事件，如UI刷新、滚动事件等
     */
    private val _events = MutableSharedFlow<EventEntity>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /**
     * 对外暴露的只读共享流
     * 组件通过监听此流来接收事件
     *
     * 使用示例：
     * lifecycleScope.launch {
     *     EventManager.events.collect { event ->
     *         when (event.type) {
     *             EventType.REFRESH -> handleRefresh(event.data as? Int)
     *         }
     *     }
     * }
     */
    val events = _events.asSharedFlow()

    /**
     * 发送事件（挂起函数）
     * 适用于协程作用域内调用，会等待事件成功发送
     *
     * @param event 要发送的事件实体
     *
     * 使用场景：在 ViewModel 或 Repository 的协程中调用
     */
    suspend fun sendEvent(event: EventEntity) {
        _events.emit(event)
    }

    /**
     * 异步延迟发送事件（非挂起函数）
     * 适用于任何地方调用，无需协程作用域
     * 默认延迟200毫秒发送，避免短时间内频繁触发
     *
     * @param event 要发送的事件实体
     * @param delay 延迟发送的时间（毫秒），默认200ms
     *
     * 适用场景：在UI事件回调中调用，避免阻塞UI线程
     */
    fun sendEventAsyncDelay(event: EventEntity, delay: Long = 200L) {
        CoroutineScope(Dispatchers.Main.immediate).launch {
            // 延迟发送，可用于防抖处理
            delay(delay)
            _events.emit(event)
        }
    }

    /**
     * 发送刷新事件的便捷方法
     * 封装了创建刷新事件和延迟发送的逻辑
     *
     * @param id 刷新的目标ID，例如要刷新的数据项ID
     * @param delay 延迟发送的时间（毫秒），默认200ms
     *
     * 使用示例：EventManager.sendRefreshEventDelay(itemId)
     */
    fun sendRefreshEventDelay(id: Int, delay: Long = 200L) {
        sendEventAsyncDelay(EventEntity(EventType.REFRESH, id), delay)
    }

    // 可以考虑添加以下功能：
    // 1. 移除特定类型的订阅者
    // 2. 批量发送事件
    // 3. 事件发送失败的重试机制
    // 4. 事件优先级支持
}