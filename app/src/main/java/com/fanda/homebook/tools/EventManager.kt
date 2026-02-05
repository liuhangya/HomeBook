package com.fanda.homebook.tools

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

object EventType {
    const val REFRESH = 0
}

data class EventEntity(val type: Int, val data: Any?)

object EventManager {
    // 使用 MutableSharedFlow 作为事件总线
    private val _events = MutableSharedFlow<EventEntity>(
        replay = 0, extraBufferCapacity = 64, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    // 对外暴露只读的 Flow
    val events = _events.asSharedFlow()

    // 发送事件（协程方式）
    suspend fun sendEvent(event: EventEntity) {
        _events.emit(event)
    }

    // 发送事件（非挂起函数，适用于任何地方）
    fun sendEventAsyncDelay(event: EventEntity, delay: Long = 200L) {
        CoroutineScope(Dispatchers.Main.immediate).launch {
            delay(delay)
            _events.emit(event)
        }
    }

    fun sendRefreshEventDelay(id: Int, delay: Long = 200L) {
        sendEventAsyncDelay(EventEntity(EventType.REFRESH, id), delay)
    }
}

