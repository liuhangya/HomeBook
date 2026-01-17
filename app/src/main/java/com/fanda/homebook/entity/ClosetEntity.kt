package com.fanda.homebook.entity

import androidx.annotation.DrawableRes
import androidx.room.Entity
import androidx.room.PrimaryKey

data class ClosetGridEntity(val name: String, val count: Int, val photoUrl: String, var isSelected: Boolean = false)

class UserEntity(id: Int, name: String) : BaseCategoryEntity(id, name)

enum class ClosetCategoryBottomMenuType {
    ALL_SELECTED, MOVE, COPY, DELETE,
}

data class CategoryBottomMenuEntity(val name: String, @DrawableRes val icon: Int, val type: ClosetCategoryBottomMenuType)




























