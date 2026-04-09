package ${packageName}.ui.api

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import ru.mirea.toir.core.navigation.Screen
import ru.mirea.toir.core.navigation.navigateOnResumed
import kotlinx.serialization.Serializable
import ${packageName}.ui.${featureName}Screen

@Serializable
data object ${featureName}Route : Screen

fun NavController.navigateTo${featureName}Screen(
    popUpInclusive: Boolean = true,
    popUpToScreen: Screen? = null,
) {
    navigateOnResumed(${featureName}Route) {
        launchSingleTop = true
        popUpToScreen?.let { screen ->
            popUpTo(screen) {
                inclusive = popUpInclusive
            }
        }
    }
}

fun NavGraphBuilder.composable${featureName}Screen(
    // TODO: add navigation callbacks
) {
    composable<${featureName}Route> {
        ${featureName}Screen(
            // TODO: pass navigation callbacks
        )
    }
}
