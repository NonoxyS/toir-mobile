package ru.mirea.toir.feature.routes.list.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import ru.mirea.toir.common.ui.compose.theme.ToirTheme
import ru.mirea.toir.res.MR

@Composable
internal fun RoutesListError(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        Icon(
            painter = painterResource(MR.images.ic_cloud_off),
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = ToirTheme.colors.error,
        )
        Text(
            text = stringResource(MR.strings.routes_list_error_title),
            style = ToirTheme.typography.bodyMedium,
            color = ToirTheme.colors.textPrimary,
        )

        Button(onClick = onRetry) {
            Text(text = stringResource(MR.strings.routes_list_button_retry))
        }
    }
}
