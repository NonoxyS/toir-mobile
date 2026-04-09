package dev.nonoxy.kmmtemplate.common.ui.compose.extensions

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit

@Composable
fun Int.toDp(): Dp = with(LocalDensity.current) { this@toDp.toDp() }

fun Int.toDp(density: Density): Dp = with(density) { this@toDp.toDp() }

@Composable
fun Float.toDp(): Dp = with(LocalDensity.current) { this@toDp.toDp() }

fun Float.toDp(density: Density): Dp = with(density) { this@toDp.toDp() }

@Composable
fun Dp.toPx(): Float = with(LocalDensity.current) { this@toPx.toPx() }

fun Dp.toPx(density: Density): Float = with(density) { this@toPx.toPx() }

@Composable
fun Dp.roundToPx(): Int = with(LocalDensity.current) { this@roundToPx.roundToPx() }

fun Dp.roundToPx(density: Density): Int = with(density) { this@roundToPx.roundToPx() }

@Composable
fun Dp.toSp(): TextUnit = with(LocalDensity.current) { this@toSp.toSp() }

fun Dp.toSp(density: Density): TextUnit = with(density) { this@toSp.toSp() }

@Composable
fun TextUnit.toDp(): Dp = with(LocalDensity.current) { this@toDp.toDp() }

fun TextUnit.toDp(density: Density): Dp = with(density) { this@toDp.toDp() }

@Composable
fun TextUnit.toPx(): Float = with(LocalDensity.current) { this@toPx.toPx() }

fun TextUnit.toPx(density: Density): Float = with(density) { this@toPx.toPx() }

@Composable
fun TextUnit.roundToPx(): Int = with(LocalDensity.current) { this@roundToPx.roundToPx() }

fun TextUnit.roundToPx(density: Density): Int = with(density) { this@roundToPx.roundToPx() }
