package com.fanda.homebook.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fanda.homebook.R

@Composable fun ColoredCircleWithBorder(
    modifier: Modifier = Modifier, color: Color = Color.Transparent, borderColor: Color = colorResource(id = R.color.color_CCFFFFFF), borderWidth: Dp = 1.dp, size: Dp = 16.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
            .border(
                width = borderWidth, color = borderColor, shape = CircleShape
            )
    )
}