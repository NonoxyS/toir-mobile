package ru.mirea.toir.feature.routes.list.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.common.ui.compose.utils.Spacer8
import ru.mirea.toir.res.MR

@Composable
internal fun RoutesListEmpty(
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
    ) {
        Icon(
            painter = painterResource(MR.images.ic_clipboard),
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = ToirTheme.colors.textDisabled,
        )
        Text(
            text = stringResource(MR.strings.routes_list_empty_title),
            style = ToirTheme.typography.bodyMedium,
            color = ToirTheme.colors.textPrimary,
        )
        Text(
            text = stringResource(MR.strings.routes_list_empty_subtitle),
            style = ToirTheme.typography.caption,
            color = ToirTheme.colors.textSecondary,
        )
        Spacer8()

        OutlinedButton(onClick = onRefresh) {
            Text(text = stringResource(MR.strings.routes_list_button_refresh))
        }
    }
}
