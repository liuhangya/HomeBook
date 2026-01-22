package com.fanda.homebook.tools

import com.dylanc.mmkv.MMKVOwner

object UserCache : MMKVOwner(mmapID = "UserCache") {
    var ownerId by mmkvInt(1)

    fun clear() {
        kv.clearAll()
    }
}