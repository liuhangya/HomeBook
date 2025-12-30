package com.fanda.homebook.closet.ui

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import com.fanda.homebook.components.CustomDropdownMenu
import com.fanda.homebook.components.MenuItem
import com.fanda.homebook.entity.UserEntity

@Composable fun UserDropdownMenu(
    curUser: UserEntity, data: List<UserEntity>, modifier: Modifier = Modifier, dpOffset: DpOffset, expanded: Boolean, onDismiss: (() -> Unit), onConfirm: (UserEntity) -> Unit
) {
    CustomDropdownMenu(modifier = modifier, dpOffset = dpOffset, expanded = expanded, onDismissRequest = onDismiss) {
        Column {
            data.forEach {
                Log.d("UserDropdownMenu", "UserDropdownMenu: ${it.id} , ${curUser.id} , ${it.id == curUser.id}")
                MenuItem(text = it.name, selected = it.id == curUser.id) {
                    onConfirm(it)
                }
            }
        }
    }
}
