package ru.mirea.toir.feature.auth.ui.api

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import ru.mirea.toir.core.navigation.AuthRoute
import ru.mirea.toir.feature.auth.ui.LoginScreen

fun NavGraphBuilder.composableAuthScreen(
    onNavigateToMain: () -> Unit,
) {
    composable<AuthRoute> {
        LoginScreen(onNavigateToMain = onNavigateToMain)
    }
}
