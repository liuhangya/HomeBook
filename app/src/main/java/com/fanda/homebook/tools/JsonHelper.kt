package com.fanda.homebook.tools

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

/**
 * Gson序列化/反序列化工具类
 * 提供简洁的扩展函数，用于对象与JSON字符串之间的转换
 *
 * 特点：
 * 1. 使用懒加载的单例Gson实例，避免重复创建
 * 2. 提供泛型安全的扩展函数，减少模板代码
 * 3. 支持空值序列化和HTML字符转义控制
 */

/**
 * 全局共享的Gson实例（懒加载）
 * 配置说明：
 * - serializeNulls(): 序列化null值字段（默认会忽略null值）
 * - disableHtmlEscaping(): 禁用HTML字符转义（如<, >, &等）
 *
 * 应用场景：适合大多数JSON处理需求，如果特殊需求可创建新的Gson实例
 */
val gson: Gson by lazy {
    GsonBuilder()
        .serializeNulls()        // 序列化null值，保持JSON字段结构
        .disableHtmlEscaping()   // 不转义HTML字符，保持原始文本
        .create()
}

/**
 * 将任意对象转换为JSON字符串
 *
 * 使用示例：
 * val user = User("John", 25)
 * val json = user.toJson() // 输出：{"name":"John","age":25}
 *
 * @receiver 要转换的对象实例
 * @return JSON格式的字符串
 */
inline fun <reified T : Any> T.toJson(): String = gson.toJson(this)

/**
 * 将List集合转换为JSON字符串
 *
 * 注意：由于Java的类型擦除，需要使用TypeToken保留泛型信息
 *
 * 使用示例：
 * val list = listOf(User("John", 25), User("Jane", 30))
 * val json = list.toJson() // 输出：[{"name":"John","age":25},{"name":"Jane","age":30}]
 *
 * @receiver 要转换的对象列表
 * @return JSON格式的字符串
 */
inline fun <reified T : Any> List<T>.toJson(): String =
    gson.toJson(this, object : TypeToken<List<T>>() {}.type)

/**
 * 将JSON字符串解析为指定类型的对象
 *
 * 注意：此函数只适用于简单对象类型，不适用于包含泛型的类型
 *
 * 使用示例：
 * val json = "{\"name\":\"John\",\"age\":25}"
 * val user = json.fromJson<User>() // 返回User对象
 *
 * @receiver JSON格式的字符串
 * @return 解析后的对象实例
 * @throws JsonSyntaxException 如果JSON格式错误或与类型不匹配
 */
inline fun <reified T : Any> String.fromJson(): T =
    gson.fromJson(this, T::class.java)

/**
 * 将JSON字符串解析为复杂泛型类型（泛型安全）
 *
 * 注意：此函数可以处理包含泛型的复杂类型
 *
 * 使用示例：
 * val json = "{\"key\":\"value\",\"count\":10}"
 * val map = json.fromJsonByType<Map<String, Any>>()
 *
 * @receiver JSON格式的字符串
 * @return 解析后的对象实例
 * @throws JsonSyntaxException 如果JSON格式错误或与类型不匹配
 */
inline fun <reified T> String.fromJsonByType(): T {
    // 注意：这里每次调用都会创建新的Gson实例，有性能考虑
    // 建议修改为使用全局gson实例
    return Gson().fromJson(this, object : TypeToken<T>() {}.type)
}

/**
 * 将JSON字符串解析为指定类型的List集合
 *
 * 注意：返回的是ArrayList类型，如果需要不可变列表，可自行转换
 *
 * 使用示例：
 * val json = "[{\"name\":\"John\"},{\"name\":\"Jane\"}]"
 * val users = json.fromJsonList<User>() // 返回ArrayList<User>
 *
 * @receiver JSON格式的字符串
 * @return 解析后的ArrayList集合
 * @throws JsonSyntaxException 如果JSON格式错误或与类型不匹配
 */
inline fun <reified T : Any> String.fromJsonList(): ArrayList<T> =
    gson.fromJson(this, object : TypeToken<List<T>>() {}.type) as ArrayList<T>

/**
 * 最佳实践建议：
 *
 * 1. 性能优化：
 *    - fromJsonByType() 函数中创建新的Gson实例，建议修改为使用全局gson实例
 *    - 频繁调用JSON转换时，复用TypeToken实例
 *
 * 2. 错误处理：
 *    - 建议在业务层添加try-catch处理解析异常
 *    - 对于用户输入的JSON，进行格式验证
 *
 * 3. 类型安全：
 *    - 使用@SerializedName注解处理JSON字段名与属性名不一致的情况
 *    - 对于可选字段，使用可空类型（如String?）
 *
 * 4. 扩展建议：
 *    - 可添加日期格式化的配置
 *    - 可添加自定义序列化/反序列化适配器
 *    - 可添加JSON格式美化（setPrettyPrinting）用于调试
 *
 * 修改建议（针对fromJsonByType函数）：
 *
 * // 修改前（性能较差，每次调用都创建新实例）：
 * inline fun <reified T> String.fromJsonByType(): T {
 *     return Gson().fromJson(this, object : TypeToken<T>() {}.type)
 * }
 *
 * // 修改后（使用全局gson实例）：
 * inline fun <reified T> String.fromJsonByType(): T {
 *     return gson.fromJson(this, object : TypeToken<T>() {}.type)
 * }
 */