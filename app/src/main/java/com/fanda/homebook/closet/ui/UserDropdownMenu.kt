package com.fanda.homebook.closet.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import com.fanda.homebook.components.CustomDropdownMenu
import com.fanda.homebook.components.MenuItem
import com.fanda.homebook.entity.BaseCategoryEntity

@Composable fun <T : BaseCategoryEntity> UserDropdownMenu(
    curUser: T, data: List<T>, modifier: Modifier = Modifier, dpOffset: DpOffset, expanded: Boolean, onDismiss: (() -> Unit), onConfirm: (T) -> Unit
) {
    CustomDropdownMenu(modifier = modifier, dpOffset = dpOffset, expanded = expanded, onDismissRequest = onDismiss) {
        Column {
            data.forEach {
                MenuItem(text = it.name, selected = it.id == curUser.id) {
                    onConfirm(it)
                }
            }
        }
    }
}
