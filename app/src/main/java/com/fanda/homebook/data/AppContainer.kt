package com.fanda.homebook.data

import android.content.Context
import com.fanda.homebook.data.closet.ClosetRepository
import com.fanda.homebook.data.closet.LocalClosetRepository
import com.fanda.homebook.data.color.ColorTypeRepository
import com.fanda.homebook.data.color.LocalColorTypeRepository

/*
* 依赖注入容器类
* */
interface AppContainer {
    val colorTypeRepository: ColorTypeRepository
    val closetRepository: ClosetRepository

}

class AppContainerImpl(private val context: Context) : AppContainer {
    override val colorTypeRepository: ColorTypeRepository by lazy {
        LocalColorTypeRepository(HomeBookDatabase.getDatabase(context).colorTypeDao())
    }
    override val closetRepository: ClosetRepository by lazy {
        LocalClosetRepository(HomeBookDatabase.getDatabase(context).closetDao())
    }
}