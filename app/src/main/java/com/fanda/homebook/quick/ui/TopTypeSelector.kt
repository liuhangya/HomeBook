package com.fanda.homebook.quick.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fanda.homebook.R
import com.fanda.homebook.components.SelectableRoundedButton
import com.fanda.homebook.data.transaction.TransactionEntity
import com.fanda.homebook.data.transaction.TransactionWithSubCategories
import com.fanda.homebook.entity.TransactionType

@Composable fun TopTypeSelector(
    transactionType: TransactionEntity?, data: List<TransactionEntity>, date: String, modifier: Modifier = Modifier, onDateClick: () -> Unit = {}, onTypeChange: (TransactionEntity?) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier
    ) {
        data.forEach {
            SelectableRoundedButton(
                text = it.name, selected = transactionType?.name == it.name, onClick = { onTypeChange(it) })
        }
        Spacer(modifier = Modifier.weight(1f))
        SelectableRoundedButton(
            text = date, selected = false, onClick = onDateClick, imageRes = R.mipmap.icon_down , interaction = true
        )
    }
}