package com.fanda.homebook.tools

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

// gson 实例，懒加载
val gson: Gson by lazy { GsonBuilder().serializeNulls().disableHtmlEscaping().create() }
inline fun <reified T : Any> T.toJson(): String = gson.toJson(this)
inline fun <reified T : Any> List<T>.toJson(): String = gson.toJson(this, object : TypeToken<List<T>>() {}.type)
inline fun <reified T : Any> String.fromJson(): T = gson.fromJson(this, T::class.java)

inline fun <reified T> String.fromJsonByType(): T {
    return Gson().fromJson(this, object : TypeToken<T>() {}.type)
}

inline fun <reified T : Any> String.fromJsonList() = gson.fromJson(this, object : TypeToken<List<T>>() {}.type) as ArrayList<T>


