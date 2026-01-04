package com.fanda.homebook.entity

import androidx.annotation.DrawableRes

data class ClosetGridEntity(val name: String, val count: Int, val photoUrl: String ,var isSelected: Boolean = false)

data class UserEntity(val id: Int, val name: String)

enum class ClosetCategoryBottomMenuType {
        ALL_SELECTED,
        MOVE ,
        COPY,
        DELETE,
}

data class CategoryBottomMenuEntity(val name: String, @DrawableRes val icon: Int,val type: ClosetCategoryBottomMenuType)