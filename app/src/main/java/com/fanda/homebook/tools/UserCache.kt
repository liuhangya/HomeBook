package com.fanda.homebook.tools

import com.dylanc.mmkv.MMKVOwner

object UserCache : MMKVOwner(mmapID = "UserCache") {
    var ownerId by mmkvInt(1)

    var rackId by mmkvInt(1)

    var bookId by mmkvInt(1)

    // 预算
    var planAmount by mmkvFloat(0.0f)

    fun clear() {
        kv.clearAll()
    }
}