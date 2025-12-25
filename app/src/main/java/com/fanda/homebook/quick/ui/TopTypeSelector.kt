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
import com.fanda.homebook.entity.TransactionType

@Composable
fun TopTypeSelector(
    transactionType: TransactionType,
    date: String,
    modifier: Modifier = Modifier,
    onDateClick: () -> Unit = {},
    onTypeChange: (TransactionType) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier
    ) {
        SelectableRoundedButton(
            text = "支出",
            selected = transactionType == TransactionType.EXPENSE,
            onClick = { onTypeChange(TransactionType.EXPENSE) })
        SelectableRoundedButton(
            text = "入账",
            selected = transactionType == TransactionType.INCOME,
            onClick = { onTypeChange(TransactionType.INCOME) })
        SelectableRoundedButton(
            text = "不计入收支",
            selected = transactionType == TransactionType.EXCLUDED,
            onClick = { onTypeChange(TransactionType.EXCLUDED) })
        Spacer(modifier = Modifier.weight(1f))
        SelectableRoundedButton(
            text = date, selected = false, onClick = onDateClick, imageRes = R.mipmap.icon_down
        )
    }
}