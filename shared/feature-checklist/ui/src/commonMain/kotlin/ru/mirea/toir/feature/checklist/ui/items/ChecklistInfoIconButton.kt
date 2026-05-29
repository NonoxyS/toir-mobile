package ru.mirea.toir.feature.checklist.ui.items

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.res.MR

@Composable
internal fun ChecklistInfoIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // IconButton по умолчанию даёт touch-target 48dp — целимся в перчатки/яркое солнце.
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            painter = painterResource(MR.images.ic_info),
            contentDescription = stringResource(MR.strings.checklist_description_open_content_description),
            tint = ToirTheme.colors.textSecondary,
            modifier = Modifier.size(20.dp),
        )
    }
}
