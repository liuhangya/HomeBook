package com.fanda.homebook.tools

import com.dylanc.mmkv.MMKVOwner
import com.fanda.homebook.data.quick.AddQuickEntity

/**
 * 用户缓存管理单例对象
 *
 * 基于 MMKV 实现的应用级用户偏好设置和缓存管理
 * MMKV 是腾讯开源的键值存储组件，相比于 SharedPreferences 有更好的性能和稳定性
 *
 * 功能特点：
 * 1. 跨进程安全访问（支持多进程共享）
 * 2. 高性能读写（避免 ANR）
 * 3. 支持自定义数据类型
 * 4. 自动序列化/反序列化
 *
 * 设计原则：
 * - 存储用户个性化设置和状态
 * - 存储频繁访问但不经常变化的数据
 * - 存储需要持久化的临时状态
 *
 * 注意：不适合存储大量数据或频繁变化的数据，建议使用数据库
 */
object UserCache : MMKVOwner(mmapID = "UserCache") {

    /**
     * 当前选择的物品所有者ID
     *
     * 用于记录用户最近选择的物品所有者（如个人、家庭等）
     * 默认值：1（通常表示默认所有者或用户本人）
     *
     * 使用场景：添加衣物或物品时，默认选中最近使用的所有者
     */
    var ownerId by mmkvInt(default = 1)

    /**
     * 当前选择的货架ID
     *
     * 用于记录用户最近选择的存储位置（如衣柜、储物柜等）
     * 默认值：1（通常表示默认货架或主要存储位置）
     *
     * 使用场景：添加衣物时，默认选中最近使用的货架
     */
    var rackId by mmkvInt(default = 1)

    /**
     * 当前选择的账本ID
     *
     * 用于记录用户最近使用的记账账本（如家庭账本、个人账本等）
     * 默认值：1（通常表示默认账本或主要账本）
     *
     * 使用场景：添加记账记录时，默认选中最近使用的账本
     */
    var bookId by mmkvInt(default = 1)


    /**
     * 快捷添加分类列表
     *
     * 用户常用的快速添加记录分类，提高添加效率
     *
     * 注意：这是一个普通属性，不会自动保存到 MMKV
     * 如果需要持久化，建议使用 mmkvString + JSON 序列化，或者扩展 mmkv 支持复杂对象
     *
     * 当前实现：内存缓存，应用重启后清空
     */
    var categoryQuickList: List<AddQuickEntity> = emptyList()

    /**
     * 清空所有缓存数据
     *
     * 注意：
     * 1. 此操作会删除所有通过 MMKV 存储的数据
     * 2. 通常用于用户注销、清除数据等场景
     * 3. 清空后，所有属性会恢复到默认值
     * 4. 不会清空内存中的属性（如 categoryQuickList）
     *
     * 使用场景：
     * - 用户退出登录
     * - 清除应用数据
     * - 调试和测试
     *
     * 危险操作提示：建议在使用时添加用户确认对话框
     */
    fun clear() {
        kv.clearAll()
        // 注意：这里只清除了 MMKV 中的数据
        // 如果需要清空内存属性，还需要手动重置
        // categoryQuickList = emptyList()
    }

    // ============================
    // 扩展建议和最佳实践
    // ============================

    /**
     * 建议添加的扩展功能：
     */

    /**
     * 1. 支持复杂对象持久化
     *
     * 扩展方法示例：
     * inline fun <reified T> mmkvObject(key: String, default: T): ReadWriteProperty<Any, T> {
     *     return object : ReadWriteProperty<Any, T> {
     *         override fun getValue(thisRef: Any, property: KProperty<*>): T {
     *             val json = kv.decodeString(key) ?: return default
     *             return Gson().fromJson(json, T::class.java)
     *         }
     *
     *         override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
     *             val json = Gson().toJson(value)
     *             kv.encode(key, json)
     *         }
     *     }
     * }
     *
     * 使用方式：
     * var categoryQuickList by mmkvObject("category_quick_list", emptyList<AddQuickEntity>())
     */

    /**
     * 2. 添加数据版本管理
     *
     * 用于数据结构变更时的数据迁移：
     * const val CACHE_VERSION = 1
     * var dataVersion by mmkvInt("data_version", 0)
     *
     * fun checkAndMigrate() {
     *     if (dataVersion < CACHE_VERSION) {
     *         // 执行数据迁移逻辑
     *         migrateFromV0ToV1()
     *         dataVersion = CACHE_VERSION
     *     }
     * }
     */

    /**
     * 3. 添加强类型枚举支持
     *
     * 示例：
     * enum class ThemeMode { LIGHT, DARK, SYSTEM }
     * var themeMode by mmkvEnum("theme_mode", ThemeMode.SYSTEM)
     */

    /**
     * 4. 添加安全存储方法（可选加密）
     *
     * 对于敏感数据（如用户 token）：
     * fun saveSecureToken(token: String) {
     *     // 使用加密存储
     * }
     */

    /**
     * 5. 添加数据变化监听
     *
     * 用于实现响应式更新：
     * interface OnDataChangeListener {
     *     fun onOwnerIdChanged(oldId: Int, newId: Int)
     *     fun onPlanAmountChanged(oldAmount: Float, newAmount: Float)
     * }
     */

    /**
     * 使用示例：
     *
     * 1. 读取数据：
     *    val currentOwnerId = UserCache.ownerId
     *    val budget = UserCache.planAmount
     *
     * 2. 保存数据：
     *    UserCache.ownerId = selectedOwner.id
     *    UserCache.planAmount = 1000.0f
     *
     * 3. 清空数据（退出登录）：
     *    UserCache.clear()
     *
     * 注意事项：
     * 1. 主线程安全：MMKV 支持主线程读写，但建议大量数据操作在子线程执行
     * 2. 数据类型：确保存储的数据类型与读取时一致
     * 3. 默认值：设置合理的默认值，避免空指针异常
     * 4. 内存缓存：对于频繁读取的数据，可考虑添加内存缓存层
     * 5. 数据同步：多进程环境下注意数据同步时机
     */
}

/**
 * 相关概念解释：
 *
 * 1. MMKV (Memory-Mapped Key-Value)
 *    - 高性能键值存储框架
 *    - 基于 mmap 内存映射技术
 *    - 支持多进程并发访问
 *    - 自动增量更新，避免全量写入
 *
 * 2. MMKVOwner
 *    - 是 MMKV 的 Kotlin 委托属性封装
 *    - 通过属性委托简化存取操作
 *    - 提供类型安全的 API
 *
 * 3. 属性委托 (by mmkvXxx)
 *    - Kotlin 语言特性，简化 getter/setter
 *    - 自动处理存储逻辑
 *    - 支持默认值设置
 *
 * 存储位置：
 * - 通常存储在应用私有目录：/data/data/[package]/files/mmkv/
 * - 数据随应用卸载自动清除
 * - 支持自定义存储路径
 */