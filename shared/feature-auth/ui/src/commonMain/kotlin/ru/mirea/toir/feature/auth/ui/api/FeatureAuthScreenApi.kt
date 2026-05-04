package ru.mirea.toir.feature.auth.ui.api

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import ru.mirea.toir.core.navigation.Screen
import ru.mirea.toir.core.navigation.navigateOnResumed
import ru.mirea.toir.feature.auth.ui.LoginScreen

@Serializable
data object AuthRoute : Screen

fun NavController.navigateToAuthScreen(
    popUpInclusive: Boolean = true,
    popUpToScreen: Screen? = null,
) {
    navigateOnResumed(AuthRoute) {
        launchSingleTop = true
        popUpToScreen?.let { screen ->
            popUpTo(screen) { inclusive = popUpInclusive }
        }
    }
}

fun NavGraphBuilder.composableAuthScreen(
    onNavigateToMain: () -> Unit,
) {
    composable<AuthRoute> {
        LoginScreen(onNavigateToMain = onNavigateToMain)
    }
}
