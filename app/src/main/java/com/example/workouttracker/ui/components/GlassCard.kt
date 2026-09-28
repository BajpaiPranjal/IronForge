package com.example.workouttracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workouttracker.ui.theme.LocalGlassColors

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    borderWidth: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val glassColors = LocalGlassColors.current

    val baseModifier = modifier
        .clip(shape)
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    glassColors.surfaceGlass,
                    glassColors.surfaceGlassVariant
                )
            )
        )
        .border(
            border = BorderStroke(
                width = borderWidth,
                brush = Brush.linearGradient(
                    colors = listOf(
                        glassColors.borderGlass,
                        glassColors.borderGlass.copy(alpha = 0.15f)
                    )
                )
            ),
            shape = shape
        )

    val finalModifier = if (onClick != null) {
        baseModifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(color = glassColors.primary),
            onClick = onClick
        )
    } else {
        baseModifier
    }

    Box(
        modifier = finalModifier,
        content = content
    )
}

@Composable
fun GlassFilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glassColors = LocalGlassColors.current

    val backgroundBrush = if (isSelected) {
        Brush.horizontalGradient(
            colors = listOf(
                glassColors.primary.copy(alpha = 0.75f),
                glassColors.primaryVariant.copy(alpha = 0.85f)
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                glassColors.surfaceGlassVariant,
                glassColors.surfaceGlassVariant.copy(alpha = 0.25f)
            )
        )
    }

    val borderBrush = if (isSelected) {
        Brush.horizontalGradient(
            colors = listOf(
                glassColors.primary,
                Color.White.copy(alpha = 0.8f)
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                glassColors.borderGlass.copy(alpha = 0.35f),
                glassColors.borderGlass.copy(alpha = 0.15f)
            )
        )
    }

    val textColor = if (isSelected) {
        if (glassColors.isDark) Color.White else Color.White
    } else {
        glassColors.textSecondary
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundBrush)
            .border(
                BorderStroke(1.dp, borderBrush),
                RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = glassColors.primary),
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun GlassBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null,
    textColor: Color? = null
) {
    val glassColors = LocalGlassColors.current
    val bg = backgroundColor ?: glassColors.primary.copy(alpha = 0.2f)
    val txt = textColor ?: glassColors.primary

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(
                BorderStroke(0.8.dp, txt.copy(alpha = 0.4f)),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = txt,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
