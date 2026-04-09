package dev.nonoxy.kmmtemplate.core.navigation.bottomsheet

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue.Expanded
import androidx.compose.material3.SheetValue.Hidden
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestinationBuilder
import androidx.navigation.NavDestinationDsl
import androidx.navigation.NavType
import kotlin.jvm.JvmSuppressWildcards
import kotlin.reflect.KClass
import kotlin.reflect.KType

@NavDestinationDsl
class ModalBottomSheetNavigatorDestinationBuilder : NavDestinationBuilder<ModalBottomSheetNavigator.Destination> {

    private val modalBottomSheetNavigator: ModalBottomSheetNavigator
    private val configuration: ModalBottomSheetConfiguration
    private val content: @Composable (NavBackStackEntry) -> Unit

    /**
     * DSL for constructing a new [ModalBottomSheetNavigator.Destination]
     *
     * @param navigator navigator used to create the destination
     * @param route the destination's unique route
     * @param modifier Optional [Modifier] for the bottom sheet.
     * @param modalBottomSheetProperties properties that should be passed to
     *   [ModalBottomSheet].
     * @param skipPartiallyExpanded Whether the partially expanded state, if the sheet is tall enough,
     *   should be skipped. If true, the sheet will always expand to the [Expanded] state and move to
     *   the [Hidden] state when hiding the sheet, either programmatically or by user interaction.
     * @param confirmValueChange Optional callback invoked to confirm or veto a pending state change.
     * @param contentWindowInsets window insets to be passed to the bottom sheet content via PaddingValues params.
     * @param dragHandle Optional visual marker to swipe the bottom sheet.
     * @param content composable for the destination
     */
    @ExperimentalMaterial3Api
    constructor(
        navigator: ModalBottomSheetNavigator,
        route: String,
        configuration: ModalBottomSheetConfiguration,
        content: @Composable (NavBackStackEntry) -> Unit,
    ) : super(navigator, route) {
        this.modalBottomSheetNavigator = navigator
        this.configuration = configuration
        this.content = content
    }

    /**
     * DSL for constructing a new [ModalBottomSheetNavigator.Destination]
     *
     * @param navigator navigator used to create the destination
     * @param route the destination's unique route from a [KClass]
     * @param typeMap map of destination arguments' kotlin type [KType] to its respective custom
     *   [NavType]. May be empty if [route] does not use custom NavTypes.
     * @param modifier Optional [Modifier] for the bottom sheet.
     * @param modalBottomSheetProperties properties that should be passed to
     *   [ModalBottomSheet].
     * @param skipPartiallyExpanded Whether the partially expanded state, if the sheet is tall enough,
     *   should be skipped. If true, the sheet will always expand to the [Expanded] state and move to
     *   the [Hidden] state when hiding the sheet, either programmatically or by user interaction.
     * @param confirmValueChange Optional callback invoked to confirm or veto a pending state change.
     * @param contentWindowInsets window insets to be passed to the bottom sheet content via PaddingValues params.
     * @param dragHandle Optional visual marker to swipe the bottom sheet.
     * @param content composable for the destination
     */
    @ExperimentalMaterial3Api
    constructor(
        navigator: ModalBottomSheetNavigator,
        route: KClass<*>,
        typeMap: Map<KType, @JvmSuppressWildcards NavType<*>>,
        configuration: ModalBottomSheetConfiguration,
        content: @Composable (NavBackStackEntry) -> Unit,
    ) : super(navigator, route, typeMap) {
        this.modalBottomSheetNavigator = navigator
        this.configuration = configuration
        this.content = content
    }

    @ExperimentalMaterial3Api
    override fun instantiateDestination(): ModalBottomSheetNavigator.Destination =
        ModalBottomSheetNavigator.Destination(
            navigator = modalBottomSheetNavigator,
            configuration = configuration,
            content = content,
        )
}
