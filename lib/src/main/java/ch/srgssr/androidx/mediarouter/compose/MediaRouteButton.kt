package ch.srgssr.androidx.mediarouter.compose

import androidx.annotation.StringRes
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.mediarouter.R
import androidx.mediarouter.app.SystemOutputSwitcherDialogController
import androidx.mediarouter.media.MediaRouteSelector
import androidx.mediarouter.media.MediaRouter
import androidx.mediarouter.media.MediaRouter.RouteInfo
import androidx.mediarouter.media.MediaRouterParams

/**
 * The media route button allows the user to select routes and to control the currently selected
 * route.
 *
 * The application must specify the kinds of routes that the user should be allowed to select by
 * specifying a [selector][MediaRouteSelector].
 *
 * When the default route is selected, the button will appear in an inactive state indicating that
 * the application is not connected to a route. Clicking on the button opens a
 * [MediaRouteChooserDialog] to allow the user to select a route. If no non-default routes
 * match the selector and it is not possible for an active scan to discover any matching routes,
 * then the button is disabled.
 *
 * When a non-default route is selected, the button will appear in an active state indicating that
 * the application is connected to a route of the kind that it wants to use. The button may also
 * appear in an intermediary connecting state if the route is in the process of connecting to the
 * destination but has not yet completed doing so. In either case, clicking on the button opens a
 * [MediaRouteControllerDialog] to allow the user to control or disconnect from the current route.
 *
 * @param modifier The [Modifier] to be applied to this button.
 * @param routeSelector The media route selector for filtering the routes that the user can select
 * using the media route chooser dialog.
 * @param colors [IconButtonColors] that will be used to resolve the colors used for this icon
 * button in different states. See [IconButtonDefaults.iconButtonColors].
 */
@Composable
fun MediaRouteButton(
    modifier: Modifier = Modifier,
    routeSelector: MediaRouteSelector = MediaRouteSelector.EMPTY,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
) {
    var mediaRouterCallbackTriggered by remember { mutableIntStateOf(0) }
    var showDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val router = remember { MediaRouter.getInstance(context) }
    val mediaRouterCallback = rememberMediaRouterCallback { mediaRouterCallbackTriggered++ }
    val connectionState by remember(mediaRouterCallbackTriggered) {
        mutableIntStateOf(computeConnectionState(router))
    }
    val icon = remember(connectionState) {
        computeIcon(connectionState)
    }
    val contentDescriptionRes = remember(connectionState) {
        computeContentDescriptionRes(connectionState)
    }

    DisposableEffect(routeSelector) {
        if (!routeSelector.isEmpty) {
            router.addCallback(routeSelector, mediaRouterCallback)
        }

        onDispose {
            if (!routeSelector.isEmpty) {
                router.removeCallback(mediaRouterCallback)
            }
        }
    }

    if (showDialog) {
        MediaRouteDialog(
            router = router,
            routeSelector = routeSelector,
            onDismissRequest = { showDialog = false },
        )
    }

    IconButton(
        onClick = { showDialog = true },
        modifier = modifier,
        colors = colors,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = stringResource(contentDescriptionRes),
        )
    }
}

@Composable
private fun MediaRouteDialog(
    router: MediaRouter,
    routeSelector: MediaRouteSelector,
    onDismissRequest: () -> Unit,
) {
    val context = LocalContext.current
    val params = router.routerParams
    var useDynamicGroup = false

    if (params != null) {
        if (params.isOutputSwitcherEnabled && MediaRouter.isMediaTransferEnabled()) {
            if (SystemOutputSwitcherDialogController.showDialog(context)) {
                return
            }
        }

        useDynamicGroup = params.dialogType == MediaRouterParams.DIALOG_TYPE_DYNAMIC_GROUP
    }

    if (router.selectedRoute.isDefaultOrBluetooth) {
        if (useDynamicGroup) {
            // TODO Display MediaRouteDynamicChooserDialog
        } else {
            MediaRouteChooserDialog(
                routeSelector = routeSelector,
                onDismissRequest = onDismissRequest,
            )
        }
    } else {
        if (useDynamicGroup) {
            // TODO Use MediaRouteDynamicControllerDialog
        } else {
            MediaRouteControllerDialog(
                routeSelector = routeSelector,
            )
        }
    }
}

@Composable
private fun rememberMediaRouterCallback(
    action: () -> Unit,
): MediaRouter.Callback {
    return remember {
        object : MediaRouter.Callback() {
            override fun onRouteAdded(router: MediaRouter, route: RouteInfo) {
                action()
            }

            override fun onRouteRemoved(router: MediaRouter, route: RouteInfo) {
                action()
            }

            override fun onRouteChanged(router: MediaRouter, route: RouteInfo) {
                action()
            }

            override fun onRouteSelected(router: MediaRouter, route: RouteInfo, reason: Int) {
                action()
            }

            override fun onRouteUnselected(router: MediaRouter, route: RouteInfo, reason: Int) {
                action()
            }

            override fun onProviderAdded(router: MediaRouter, provider: MediaRouter.ProviderInfo) {
                action()
            }

            override fun onProviderRemoved(
                router: MediaRouter,
                provider: MediaRouter.ProviderInfo
            ) {
                action()
            }

            override fun onProviderChanged(
                router: MediaRouter,
                provider: MediaRouter.ProviderInfo
            ) {
                action()
            }
        }
    }
}

private fun computeConnectionState(router: MediaRouter): Int {
    val selectedRoute = router.selectedRoute
    val isRemote = !selectedRoute.isDefaultOrBluetooth

    return if (isRemote) selectedRoute.connectionState else RouteInfo.CONNECTION_STATE_DISCONNECTED
}

private fun computeIcon(connectionState: Int): ImageVector {
    return when (connectionState) {
        RouteInfo.CONNECTION_STATE_CONNECTING -> Icons.Downloading // TODO Use the proper Cast animation
        RouteInfo.CONNECTION_STATE_CONNECTED -> Icons.CastConnected
        else -> Icons.Cast
    }
}

@StringRes
private fun computeContentDescriptionRes(connectionState: Int): Int {
    return when (connectionState) {
        RouteInfo.CONNECTION_STATE_CONNECTING -> R.string.mr_cast_button_connecting
        RouteInfo.CONNECTION_STATE_CONNECTED -> R.string.mr_cast_button_connected
        else -> R.string.mr_cast_button_disconnected
    }
}
