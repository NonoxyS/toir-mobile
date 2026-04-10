package ru.mirea.toir.common.ui.compose.extensions

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.Spring.StiffnessMediumLow
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.round
import kotlinx.coroutines.launch

fun Modifier.modifierIf(predicate: Boolean, block: Modifier.() -> Modifier): Modifier {
    return if (predicate) block(this) else this
}

inline fun Modifier.conditional(
    predicate: Boolean,
    falseBlock: Modifier.() -> Modifier = { this },
    trueBlock: Modifier.() -> Modifier
): Modifier = if (predicate) trueBlock() else falseBlock()

@Suppress("ModifierFactoryUnreferencedReceiver")
fun Modifier.drawContentIfReady(readyToDraw: Boolean): Modifier {
    return drawWithContent {
        if (readyToDraw) drawContent()
    }
}

fun Modifier.animatePlacement(): Modifier = composed {
    val scope = rememberCoroutineScope()
    var targetOffset by remember { mutableStateOf(IntOffset.Zero) }
    var animatable by remember { mutableStateOf<Animatable<IntOffset, AnimationVector2D>?>(null) }

    this
        .onPlaced { layoutCoordinates ->
            // Calculate the position in the parent layout
            targetOffset = layoutCoordinates.positionInParent().round()
        }
        .offset {
            // Animate to the new target offset when alignment changes.
            val anim = animatable ?: Animatable(
                initialValue = targetOffset,
                typeConverter = IntOffset.VectorConverter
            ).also { animatable = it }

            if (anim.targetValue != targetOffset) {
                scope.launch {
                    anim.animateTo(
                        targetValue = targetOffset,
                        animationSpec = spring(stiffness = StiffnessMediumLow)
                    )
                }
            }
            // Offset the child in the opposite direction to the targetOffset, and slowly catch
            // up to zero offset via an animation to achieve an overall animated movement.
            animatable?.let { it.value - targetOffset } ?: IntOffset.Zero
        }
}
