package ru.mirea.toir.core.navigation.bottomsheet

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.FloatingWindow
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import kotlinx.coroutines.flow.StateFlow

@Composable
fun rememberModalBottomSheetNavigator(): ModalBottomSheetNavigator =
    remember { ModalBottomSheetNavigator() }

/**
 * Navigator that navigates through [Composable]s that will be hosted within a [ModalBottomSheet]. Every
 * destination using this Navigator must set a valid [Composable] by setting it directly on an
 * instantiated [Destination] or calling [bottomSheet].
 */
expect class ModalBottomSheetNavigator() : Navigator<ModalBottomSheetNavigator.Destination> {

    /** Get the back stack from the [state]. */
    internal val backStack: StateFlow<List<NavBackStackEntry>>

    /** Get the transitioning modal bottom sheets from the [state]. */
    internal val transitionInProgress: StateFlow<Set<NavBackStackEntry>>

    /** Dismiss the modal bottom sheets destination associated with the given [backStackEntry]. */
    internal fun dismiss(backStackEntry: NavBackStackEntry)

    internal fun onTransitionComplete(entry: NavBackStackEntry)

    override fun navigate(
        entries: List<NavBackStackEntry>,
        navOptions: NavOptions?,
        navigatorExtras: Extras?,
    )

    override fun createDestination(): Destination

    override fun popBackStack(popUpTo: NavBackStackEntry, savedState: Boolean)

    /** NavDestination specific to [ModalBottomSheetNavigator] */
    @Suppress("UnusedPrivateProperty")
    class Destination @ExperimentalMaterial3Api constructor(
        navigator: ModalBottomSheetNavigator,
        configuration: ModalBottomSheetConfiguration = ModalBottomSheetConfiguration(),
        content: @Composable (NavBackStackEntry) -> Unit,
    ) : NavDestination, FloatingWindow {
        internal val content: @Composable (NavBackStackEntry) -> Unit
        internal val configuration: ModalBottomSheetConfiguration
    }

    companion object {
        val NAME: String
    }
}
