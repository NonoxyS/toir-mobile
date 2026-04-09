package dev.nonoxy.kmmtemplate.common.ui.compose.utils

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.text.TextLayoutResult
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch

@Composable
fun <T> Flow<T>.CollectFlow(collector: FlowCollector<T>) {
    LaunchedEffect(Unit) {
        collect(collector = collector)
    }
}

// https://twitter.github.io/compose-rules/rules/#ordering-composable-parameters-properly
@Suppress("ComposableParametersOrdering", "ComposableParamOrder")
@Composable
fun <T> Flow<T>.CollectFlowWithLifecycle(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    collector: FlowCollector<T>
) {
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(lifecycleState) {
            collect(collector = collector)
        }
    }
}

@Suppress("ComposableParametersOrdering")
@Composable
fun <T> rememberBlock(
    key: Any = Unit,
    block: () -> T
) = remember(key1 = key) { block }

@Composable
fun <P, T> rememberBlockWithParams(block: (P) -> T) = remember { block }

@OptIn(ExperimentalFoundationApi::class)
fun onFocusBringViewToScreen(
    coroutineScope: CoroutineScope,
    requester: BringIntoViewRequester,
    delay: Long = FOCUS_BRING_DELAY.toLong()
): (FocusState) -> Unit = {
    if (it.isFocused || it.hasFocus) {
        coroutineScope.launch {
            delay(delay)
            requester.bringIntoView()
        }
    }
}

fun dynamicTextWidthSize(
    onOverflowWidth: (Float) -> Unit,
    onReadyToDraw: (Boolean) -> Unit = {},
    percent: Float = 0.9F
): (TextLayoutResult) -> Unit = {
    if (it.didOverflowWidth) {
        onOverflowWidth(percent)
    } else {
        onReadyToDraw(true)
    }
}

fun dynamicTextHeightSize(
    onOverflow: (Float) -> Unit,
    onReadyToDraw: (Boolean) -> Unit = {},
    percent: Float = 0.9F
): (TextLayoutResult) -> Unit = {
    if (it.didOverflowHeight) {
        onOverflow(percent)
    } else {
        onReadyToDraw(true)
    }
}

fun Modifier.handleSwipeDownToDismiss(
    enabled: Boolean,
    onDrag: (offset: Float) -> Unit,
    onDragEnd: (offset: Float) -> Unit,
): Modifier {
    return this.pointerInput(Unit) {
        if (enabled) {
            var dragOffset = 0f
            detectVerticalDragGestures(
                onVerticalDrag = { _, offset ->
                    dragOffset = offset
                    onDrag(dragOffset)
                },
                onDragEnd = {
                    onDragEnd(dragOffset)
                    dragOffset = 0f
                }
            )
        }
    }
}

private const val FOCUS_BRING_DELAY = 250
