package com.fanda.homebook.closet.state

import com.fanda.homebook.data.closet.AddClosetEntity
import com.fanda.homebook.data.closet.ClosetEntity
import com.fanda.homebook.data.color.ColorTypeEntity
import com.fanda.homebook.data.season.SeasonEntity

/*
* 添加衣橱状态类
* */
data class AddClosetUiState(
    val closetEntity: ClosetEntity = ClosetEntity(),
    val colorType: ColorTypeEntity? = null,
    val season: SeasonEntity? = null
)
