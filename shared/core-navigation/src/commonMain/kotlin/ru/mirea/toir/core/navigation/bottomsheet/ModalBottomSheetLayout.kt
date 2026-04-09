package ru.mirea.toir.core.navigation.bottomsheet

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable

/**
 * A composable that hosts modal bottom sheet navigation.
 *
 * Use with a [androidx.navigation.NavHost] to manage bottom sheet destinations.
 *
 * Example usage:
 * ```kotlin
 * val bottomSheetNavigator = rememberModalBottomSheetNavigator()
 * val navController = rememberNavController(bottomSheetNavigator)
 *
 * ModalBottomSheetHost(
 *     modalBottomSheetNavigator = bottomSheetNavigator
 * ) {
 *     NavHost(navController, "home") {
 *         composable("home") { /* Home content */ }
 *         bottomSheet("details") { /* Details content */ }
 *     }
 * }
 * ```
 */
@ExperimentalMaterial3Api
@Composable
fun ModalBottomSheetLayout(
    modalBottomSheetNavigator: ModalBottomSheetNavigator,
    content: @Composable () -> Unit,
) {
    content()
    ModalBottomSheetHost(modalBottomSheetNavigator = modalBottomSheetNavigator)
}
