package com.fanda.homebook.closet.state

import android.net.Uri
import com.fanda.homebook.data.closet.ClosetEntity
import com.fanda.homebook.data.color.ColorTypeEntity
import com.fanda.homebook.data.product.ProductEntity
import com.fanda.homebook.data.season.SeasonEntity
import com.fanda.homebook.data.size.SizeEntity
import com.fanda.homebook.entity.ShowBottomSheetType

/*
* 添加衣橱状态类
* */
data class AddClosetUiState(
    val closetEntity: ClosetEntity = ClosetEntity(),
    val imageUri: Uri? = null,
    val seasonIds : List<Int> = listOf(),
    val sheetType: ShowBottomSheetType = ShowBottomSheetType.NONE,
)
