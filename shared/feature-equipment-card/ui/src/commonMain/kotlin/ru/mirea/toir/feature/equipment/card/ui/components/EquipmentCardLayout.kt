package ru.mirea.toir.feature.equipment.card.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.IntrinsicSize
import ru.mirea.toir.common.ui.compose.theme.ToirColorScheme
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.feature.equipment.card.presentation.models.UiEquipmentResultStatus

@Composable
internal fun EquipmentCardLayout(
    status: UiEquipmentResultStatus,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = ToirTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(ToirTheme.shapes.md)
            .background(colors.surface),
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(accentColorFor(status, colors)),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            content()
        }
    }
}

private fun accentColorFor(
    status: UiEquipmentResultStatus,
    colors: ToirColorScheme,
): Color = when (status) {
    UiEquipmentResultStatus.NOT_STARTED -> colors.textDisabled
    UiEquipmentResultStatus.IN_PROGRESS -> colors.warning
    UiEquipmentResultStatus.COMPLETED -> colors.success
    UiEquipmentResultStatus.SKIPPED -> colors.error
}
