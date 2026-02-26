package com.fanda.homebook.tools

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

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

    /**
     * 粘性事件起始值
     * 所有粘性事件类型都应 >= STICKY_EVENT_START
     * 这样可以清晰区分普通事件和粘性事件
     */
    const val STICKY_EVENT_START = 1000

    // 可以继续添加其他粘性事件类型，例如：
     const val REFRESH_STICKY_EVENT = STICKY_EVENT_START + 1
    // const val SETTINGS_CHANGED = STICKY_EVENT_START + 2
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
 * 5. 支持粘性事件：新订阅者可以收到最后一次发送的粘性事件
 */
object EventManager {
    /**
     * 内部可变共享流，作为普通事件总线的核心
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
     * 组件通过监听此流来接收普通事件
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
     * 粘性事件存储
     * 使用 MutableStateFlow 来保存最后一次发送的粘性事件
     * key: 事件类型
     * value: 对应的事件实体
     *
     * 使用 Mutex 保证线程安全
     */
    private val stickyEventsMutex = Mutex()
    private val _stickyEvents = mutableMapOf<Int, EventEntity>()

    /**
     * 粘性事件流映射
     * 为每个粘性事件类型创建一个单独的 StateFlow
     * 这样新订阅者可以立即获取到最新的值
     */
    private val stickyEventFlows = mutableMapOf<Int, MutableStateFlow<EventEntity?>>()

    /**
     * 获取指定类型的粘性事件流
     * 如果该类型的流不存在，则创建一个新的
     *
     * @param type 事件类型
     * @return 该类型的粘性事件流
     */
    private fun getStickyEventFlow(type: Int): MutableStateFlow<EventEntity?> {
        return stickyEventFlows.getOrPut(type) {
            // 从存储中恢复已有的事件
            val existingEvent = _stickyEvents[type]
            MutableStateFlow(existingEvent)
        }
    }

    /**
     * 获取指定类型的粘性事件（一次性）
     * 获取后会清除该粘性事件，确保只接收一次
     *
     * @param type 事件类型
     * @return 粘性事件实体，如果没有则返回null
     */
    suspend fun getStickyEvent(type: Int): EventEntity? {
        return stickyEventsMutex.withLock {
            val event = _stickyEvents.remove(type)
            // 更新对应的StateFlow
            stickyEventFlows[type]?.tryEmit(null)
            event
        }
    }

    /**
     * 获取指定类型的粘性事件流（可观察）
     * 订阅后会立即收到最后一次发送的该类型事件（如果有）
     * 之后每次有新事件都会收到
     *
     * @param type 事件类型
     * @return 该类型的粘性事件流
     */
    fun observeStickyEvent(type: Int): kotlinx.coroutines.flow.StateFlow<EventEntity?> {
        return getStickyEventFlow(type).asStateFlow()
    }

    /**
     * 获取指定类型的粘性事件数据（带类型转换）
     * 获取后会清除该粘性事件
     *
     * @param type 事件类型
     * @return 转换后的数据，如果没有事件或类型不匹配则返回null
     */
    suspend inline fun <reified T> getStickyEventData(type: Int): T? {
        return getStickyEvent(type)?.data as? T
    }

    /**
     * 发送普通事件（挂起函数）
     * 适用于协程作用域内调用，会等待事件成功发送
     *
     * @param event 要发送的事件实体
     *
     * 使用场景：在 ViewModel 或 Repository 的协程中调用
     */
    suspend fun sendEvent(event: EventEntity) {
        // 如果是粘性事件类型，同时作为粘性事件发送
        if (event.type >= EventType.STICKY_EVENT_START) {
            sendStickyEvent(event)
        } else {
            _events.emit(event)
        }
    }

    /**
     * 发送粘性事件
     * 粘性事件会被保存，新订阅者可以立即收到最后发送的事件
     *
     * @param event 要发送的粘性事件实体
     */
    suspend fun sendStickyEvent(event: EventEntity) {
        require(event.type >= EventType.STICKY_EVENT_START) {
            "Sticky event type must be >= ${EventType.STICKY_EVENT_START}"
        }

        stickyEventsMutex.withLock {
            // 保存粘性事件
            _stickyEvents[event.type] = event
            // 更新对应的StateFlow
            getStickyEventFlow(event.type).emit(event)
        }
        // 同时也发送到普通事件流，便于统一监听
        _events.emit(event)
    }

    /**
     * 移除指定类型的粘性事件
     *
     * @param type 事件类型
     */
    suspend fun removeStickyEvent(type: Int) {
        stickyEventsMutex.withLock {
            _stickyEvents.remove(type)
            stickyEventFlows[type]?.tryEmit(null)
        }
    }

    /**
     * 清除所有粘性事件
     */
    suspend fun clearAllStickyEvents() {
        stickyEventsMutex.withLock {
            _stickyEvents.clear()
            stickyEventFlows.values.forEach { it.tryEmit(null) }
        }
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
            if (event.type >= EventType.STICKY_EVENT_START) {
                sendStickyEvent(event)
            } else {
                _events.emit(event)
            }
        }
    }

    /**
     * 发送粘性刷新事件的便捷方法
     *
     * @param id 刷新的目标ID
     * @param delay 延迟发送的时间（毫秒）
     */
    fun sendStickyRefreshEventDelay(id: Int, delay: Long = 0L) {
        // 为粘性刷新事件定义一个专门的类型
        val stickyRefreshType = EventType.REFRESH_STICKY_EVENT
        sendEventAsyncDelay(EventEntity(stickyRefreshType, id), delay)
    }
}

/**
 * 扩展函数：方便在 ViewModel 或 Activity/Fragment 中收集粘性事件
 *
 * 使用示例：
 * lifecycleScope.launch {
 *     EventManager.observeStickyEvent(EventType.STICKY_EVENT_START).collect { event ->
 *         event?.let {
 *             // 处理粘性事件
 *             val data = it.data as? Int
 *             handleRefresh(data)
 *         }
 *     }
 * }
 */

/**
 * 扩展函数：一次性获取粘性事件
 *
 * 使用示例：
 * lifecycleScope.launch {
 *     val event = EventManager.getStickyEvent(EventType.STICKY_EVENT_START)
 *     event?.let {
 *         // 处理粘性事件
 *     }
 * }
 */