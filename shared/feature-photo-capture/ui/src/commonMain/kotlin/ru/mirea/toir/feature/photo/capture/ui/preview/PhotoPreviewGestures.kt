package ru.mirea.toir.feature.photo.capture.ui.preview

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.platform.LocalViewConfiguration
import kotlin.math.abs
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal fun Modifier.photoDismissDrag(
    state: PhotoPreviewDragState,
    isZoomed: () -> Boolean,
    onClose: () -> Unit,
    onDoubleTap: (Offset) -> Unit,
    dismissThresholdFraction: Float = PhotoPreviewDefaults.DismissThresholdFraction,
    dismissVelocityThreshold: Float = PhotoPreviewDefaults.DismissVelocityThreshold,
): Modifier = composed {
    val scope = rememberCoroutineScope()
    val viewConfig = LocalViewConfiguration.current
    val touchSlop = viewConfig.touchSlop
    val doubleTapTimeout = viewConfig.doubleTapTimeoutMillis
    pointerInput(state, onClose) {
        var snapJob: Job? = null
        var lastTapTime = 0L
        var lastTapPos = Offset.Zero
        awaitPointerEventScope {
            while (true) {
                val initialEvent = awaitPointerEvent(PointerEventPass.Initial)
                val downChange = initialEvent.changes.firstOrNull { it.changedToDown() } ?: continue
                if (initialEvent.changes.count { it.pressed } > 1) continue

                snapJob?.cancel()
                snapJob = null
                val downPos = downChange.position
                val downTime = downChange.uptimeMillis
                downChange.consume()

                val tracker = VelocityTracker().apply { addPointerInputChange(downChange) }
                var dragClaimed = false
                var aborted = false
                var totalDx = 0f
                var totalDy = 0f
                var upTime = downTime

                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    val pressedChanges = event.changes.filter { it.pressed }
                    if (pressedChanges.isEmpty()) {
                        upTime = event.changes.firstOrNull()?.uptimeMillis ?: upTime
                        break
                    }
                    if (pressedChanges.size >= 2) {
                        aborted = true
                        break
                    }
                    val change = pressedChanges.first()
                    tracker.addPointerInputChange(change)

                    if (dragClaimed) {
                        state.applyDragDelta(change.positionChange().y)
                        change.consume()
                        continue
                    }
                    if (isZoomed()) {
                        aborted = true
                        break
                    }
                    totalDx += change.positionChange().x
                    totalDy += change.positionChange().y
                    if (abs(totalDy) > touchSlop && abs(totalDy) > abs(totalDx)) {
                        dragClaimed = true
                        state.applyDragDelta(totalDy)
                    }
                    change.consume()
                }

                if (aborted) continue

                if (dragClaimed) {
                    val velocity = tracker.calculateVelocity()
                    val fraction = state.dragFraction
                    val sameDirection = velocity.y != 0f &&
                        (velocity.y > 0f) == (state.dragOffsetY > 0f)
                    val flickDismiss = sameDirection &&
                        abs(velocity.y) >= dismissVelocityThreshold
                    val distanceDismiss = abs(fraction) >= dismissThresholdFraction
                    if (flickDismiss || distanceDismiss) {
                        onClose()
                    } else {
                        snapJob = scope.launch { state.snapBackDrag() }
                    }
                } else {
                    val gap = upTime - lastTapTime
                    val close = (downPos - lastTapPos).getDistance() < touchSlop * 3f
                    if (gap in 1..doubleTapTimeout && close) {
                        onDoubleTap(downPos)
                        lastTapTime = 0L
                        lastTapPos = Offset.Zero
                    } else {
                        lastTapTime = upTime
                        lastTapPos = downPos
                    }
                }
            }
        }
    }
}

private fun PointerInputChange.changedToDown(): Boolean = pressed && !previousPressed
