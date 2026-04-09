package ru.mirea.toir.core.navigation.bottomsheet

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetValue
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class ModalBottomSheetConfiguration
@OptIn(ExperimentalMaterial3Api::class) constructor(
    val modifier: Modifier = Modifier.statusBarsPadding(),
    val properties: ModalBottomSheetProperties = ModalBottomSheetDefaults.properties,
    val contentTransition: AnimatedContentTransitionScope<*>.() -> ContentTransform = defaultContentTransition,
    val skipPartiallyExpanded: Boolean = true,
    val confirmValueChange: (SheetValue) -> Boolean = { true },
    val contentWindowInsets: @Composable () -> WindowInsets = defaultWindowInsets,
    val dragHandle: @Composable (() -> Unit)? = defaultDragHandle,
    val containerColor: @Composable () -> Color = defaultContainerColor,
    val contentColor: @Composable () -> Color = { defaultContentColor(containerColor()) },
    val scrimColor: @Composable () -> Color = defaultScrimColor,
    val shape: @Composable () -> Shape = defaultShape,
    val sheetMaxWidth: @Composable () -> Dp = defaultSheetMaxWidth,
    val tonalElevation: @Composable () -> Dp = defaultTonalElevation,
) {
    companion object {
        @OptIn(ExperimentalMaterial3Api::class)
        internal val defaultWindowInsets: @Composable (() -> WindowInsets)
            get(): @Composable () -> WindowInsets = { BottomSheetDefaults.windowInsets }

        @OptIn(ExperimentalMaterial3Api::class)
        internal val defaultDragHandle: @Composable (() -> Unit)?
            get() = { BottomSheetDefaults.DragHandle() }

        @OptIn(ExperimentalMaterial3Api::class)
        internal val defaultContainerColor: @Composable () -> Color
            get() = { Color.White }

        @OptIn(ExperimentalMaterial3Api::class)
        internal val defaultContentColor: @Composable (Color) -> Color
            get() = { containerColor -> contentColorFor(containerColor) }

        @OptIn(ExperimentalMaterial3Api::class)
        internal val defaultScrimColor: @Composable () -> Color
            get() = { BottomSheetDefaults.ScrimColor }

        @OptIn(ExperimentalMaterial3Api::class)
        internal val defaultShape: @Composable () -> Shape
            get() = { BottomSheetDefaults.ExpandedShape }

        @OptIn(ExperimentalMaterial3Api::class)
        internal val defaultSheetMaxWidth: @Composable () -> Dp
            get() = { BottomSheetDefaults.SheetMaxWidth }

        internal val defaultTonalElevation: @Composable () -> Dp
            get() = { 0.dp }

        internal val defaultContentTransition: AnimatedContentTransitionScope<*>.() -> ContentTransform
            get() = {
                fadeIn(
                    animationSpec = tween(220, delayMillis = 90)
                ) + scaleIn(
                    initialScale = 0.92f,
                    animationSpec = tween(220, delayMillis = 90)
                ) togetherWith fadeOut(animationSpec = tween(90))
            }
    }
}

@Composable
internal fun ModalBottomSheetConfiguration?.getSheetMaxWidth(): Dp =
    this?.sheetMaxWidth?.invoke() ?: ModalBottomSheetConfiguration.defaultSheetMaxWidth()

@Composable
internal fun ModalBottomSheetConfiguration?.getShape(): Shape =
    this?.shape?.invoke() ?: ModalBottomSheetConfiguration.defaultShape()

@Composable
internal fun ModalBottomSheetConfiguration?.getContainerColor(): Color =
    this?.containerColor?.invoke() ?: ModalBottomSheetConfiguration.defaultContainerColor()

@Composable
internal fun ModalBottomSheetConfiguration?.getContentColor(): Color =
    this?.contentColor?.invoke() ?: ModalBottomSheetConfiguration.defaultContentColor(
        this.getContainerColor()
    )

@Composable
internal fun ModalBottomSheetConfiguration?.getTonalElevation(): Dp =
    this?.tonalElevation?.invoke() ?: ModalBottomSheetConfiguration.defaultTonalElevation()

@Composable
internal fun ModalBottomSheetConfiguration?.getScrimColor(): Color =
    this?.scrimColor?.invoke() ?: ModalBottomSheetConfiguration.defaultScrimColor()

internal fun ModalBottomSheetConfiguration?.getDragHandle(): @Composable (() -> Unit)? =
    this?.dragHandle ?: ModalBottomSheetConfiguration.defaultDragHandle

@Composable
internal fun ModalBottomSheetConfiguration?.getContentWindowInsets(): (@Composable () -> WindowInsets) =
    this?.contentWindowInsets ?: ModalBottomSheetConfiguration.defaultWindowInsets

internal fun ModalBottomSheetConfiguration?.getModifier(): Modifier =
    this?.modifier ?: Modifier

internal fun ModalBottomSheetConfiguration?.getSkipPartiallyExpanded(): Boolean =
    this?.skipPartiallyExpanded ?: true

@OptIn(ExperimentalMaterial3Api::class)
internal fun ModalBottomSheetConfiguration?.getConfirmValueChange(): (SheetValue) -> Boolean =
    this?.confirmValueChange ?: { true }

@OptIn(ExperimentalMaterial3Api::class)
internal fun ModalBottomSheetConfiguration?.getProperties(): ModalBottomSheetProperties =
    this?.properties ?: ModalBottomSheetDefaults.properties

internal fun ModalBottomSheetConfiguration?.getContentTransition(): (
AnimatedContentTransitionScope<*>.() -> ContentTransform
) = this?.contentTransition ?: ModalBottomSheetConfiguration.defaultContentTransition
