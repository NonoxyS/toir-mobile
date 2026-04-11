package ru.mirea.toir.common.ui.compose.utils

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// ── Row spacers (horizontal) ──────────────────────────────────────────────────

@Composable
fun RowScope.Spacer4(modifier: Modifier = Modifier) =
    Spacer(modifier = Modifier.width(4.dp).then(modifier))

@Composable
fun RowScope.Spacer8(modifier: Modifier = Modifier) =
    Spacer(modifier = Modifier.width(8.dp).then(modifier))

@Composable
fun RowScope.Spacer12(modifier: Modifier = Modifier) =
    Spacer(modifier = Modifier.width(12.dp).then(modifier))

@Composable
fun RowScope.Spacer16(modifier: Modifier = Modifier) =
    Spacer(modifier = Modifier.width(16.dp).then(modifier))

@Composable
fun RowScope.Spacer24(modifier: Modifier = Modifier) =
    Spacer(modifier = Modifier.width(24.dp).then(modifier))

@Composable
fun RowScope.Spacer32(modifier: Modifier = Modifier) =
    Spacer(modifier = Modifier.width(32.dp).then(modifier))

@Composable
fun RowScope.Spacer48(modifier: Modifier = Modifier) =
    Spacer(modifier = Modifier.width(48.dp).then(modifier))

// ── Column spacers (vertical) ─────────────────────────────────────────────────

@Composable
fun ColumnScope.Spacer4(modifier: Modifier = Modifier) =
    Spacer(modifier = Modifier.height(4.dp).then(modifier))

@Composable
fun ColumnScope.Spacer8(modifier: Modifier = Modifier) =
    Spacer(modifier = Modifier.height(8.dp).then(modifier))

@Composable
fun ColumnScope.Spacer12(modifier: Modifier = Modifier) =
    Spacer(modifier = Modifier.height(12.dp).then(modifier))

@Composable
fun ColumnScope.Spacer16(modifier: Modifier = Modifier) =
    Spacer(modifier = Modifier.height(16.dp).then(modifier))

@Composable
fun ColumnScope.Spacer24(modifier: Modifier = Modifier) =
    Spacer(modifier = Modifier.height(24.dp).then(modifier))

@Composable
fun ColumnScope.Spacer32(modifier: Modifier = Modifier) =
    Spacer(modifier = Modifier.height(32.dp).then(modifier))

@Composable
fun ColumnScope.Spacer48(modifier: Modifier = Modifier) =
    Spacer(modifier = Modifier.height(48.dp).then(modifier))
