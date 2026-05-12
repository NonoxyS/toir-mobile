package ru.mirea.toir.feature.routes.list.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.feature.routes.list.presentation.models.UiSyncIndicator
import ru.mirea.toir.res.MR

@Composable
internal fun SyncIndicatorIcon(
    indicator: UiSyncIndicator,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (icon, tint) = resolveIconAndTint(indicator)
    val rotation = if (indicator.isRunning) rememberSpinRotation() else 0f

    IconButton(
        onClick = onClick,
        modifier = modifier.size(44.dp),
    ) {
        BadgedBox(
            badge = {
                val showBadge =
                    !indicator.isRunning && indicator.lastError == null && indicator.pendingCount > 0
                if (showBadge) {
                    Badge(
                        containerColor = ToirTheme.colors.warning,
                        contentColor = Color.White,
                    ) {
                        Text(indicator.pendingCount.toString())
                    }
                }
            }
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = stringResource(MR.strings.sync_indicator_content_description),
                tint = tint,
                modifier = Modifier
                    .size(24.dp)
                    .let { if (indicator.isRunning) it.rotate(rotation) else it },
            )
        }
    }
}

@Composable
private fun resolveIconAndTint(indicator: UiSyncIndicator): Pair<dev.icerock.moko.resources.ImageResource, Color> {
    val colors = ToirTheme.colors
    return when {
        indicator.isRunning -> MR.images.ic_sync_alt to colors.textPrimary
        indicator.lastError != null -> MR.images.ic_error_outline to colors.error
        indicator.pendingCount > 0 -> MR.images.ic_sync_alt to colors.warning
        else -> MR.images.ic_check_circle to colors.success
    }
}

@Composable
private fun rememberSpinRotation(): Float {
    val transition = rememberInfiniteTransition(label = "sync_spin")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "sync_spin_angle",
    )
    return angle
}
