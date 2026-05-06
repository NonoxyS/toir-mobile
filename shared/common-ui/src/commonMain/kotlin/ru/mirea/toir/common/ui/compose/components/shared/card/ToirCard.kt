package ru.mirea.toir.common.ui.compose.components.shared.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import ru.mirea.toir.common.ui.compose.theme.ToirTheme

/**
 * Surface elevation tokens per MASTER §6.
 *
 * - [Level1] — base surface (`surface`); used for the page background containers
 *   and the default list-card pattern (§9.1).
 * - [Level2] — raised surface (`surface2`) with a 1 dp `border` stroke; used to
 *   visually separate nested content such as info banners or the equipment card
 *   content area on top of [Level1].
 */
enum class ToirElevation { Level1, Level2 }

/**
 * Surface card wrapper per MASTER §6 (elevation tokens) and §9.1 (list card pattern).
 *
 * Wraps Material3 [Surface] with the design system colour, shape (10 dp `md`) and
 * — for [ToirElevation.Level2] — a 1 dp `border` stroke. Shadows are intentionally
 * disabled (`tonalElevation = 0.dp`, `shadowElevation = 0.dp`) because the dark-only
 * theme communicates depth via colour and border, not blur.
 *
 * The content slot is a [ColumnScope]; padding is the caller's responsibility.
 * When [onClick] is non-null the card uses the clickable [Surface] overload so it
 * gets the standard ripple / focus indication.
 */
@Composable
fun ToirCard(
    modifier: Modifier = Modifier,
    elevation: ToirElevation = ToirElevation.Level1,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val color = when (elevation) {
        ToirElevation.Level1 -> ToirTheme.colors.surface
        ToirElevation.Level2 -> ToirTheme.colors.surface2
    }
    val border = if (elevation == ToirElevation.Level2) {
        BorderStroke(1.dp, ToirTheme.colors.border)
    } else {
        null
    }
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier,
            shape = ToirTheme.shapes.md,
            color = color,
            contentColor = ToirTheme.colors.textPrimary,
            border = border,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Column(content = content)
        }
    } else {
        Surface(
            modifier = modifier,
            shape = ToirTheme.shapes.md,
            color = color,
            contentColor = ToirTheme.colors.textPrimary,
            border = border,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Column(content = content)
        }
    }
}

@Preview
@Composable
private fun PreviewToirCardLevel1() {
    ToirTheme {
        Box(Modifier.background(ToirTheme.colors.background).padding(16.dp)) {
            ToirCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = "Заголовок карточки",
                        style = ToirTheme.typography.label,
                    )
                    Text(
                        text = "Подпись на surface",
                        style = ToirTheme.typography.bodyMedium,
                        color = ToirTheme.colors.textSecondary,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewToirCardLevel2() {
    ToirTheme {
        Box(Modifier.background(ToirTheme.colors.background).padding(16.dp)) {
            ToirCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = ToirElevation.Level2,
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = "Заголовок карточки",
                        style = ToirTheme.typography.label,
                    )
                    Text(
                        text = "Подпись на surface2 с border",
                        style = ToirTheme.typography.bodyMedium,
                        color = ToirTheme.colors.textSecondary,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewToirCardClickable() {
    ToirTheme {
        Box(Modifier.background(ToirTheme.colors.background).padding(16.dp)) {
            ToirCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = {},
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = "Кликабельная карточка",
                        style = ToirTheme.typography.label,
                    )
                    Text(
                        text = "Нажмите для перехода",
                        style = ToirTheme.typography.bodyMedium,
                        color = ToirTheme.colors.textSecondary,
                    )
                }
            }
        }
    }
}
