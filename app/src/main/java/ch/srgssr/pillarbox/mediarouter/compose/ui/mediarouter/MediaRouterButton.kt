package ch.srgssr.pillarbox.mediarouter.compose.ui.mediarouter

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.mediarouter.R
import androidx.mediarouter.media.MediaControlIntent
import androidx.mediarouter.media.MediaRouteSelector
import androidx.mediarouter.media.MediaRouter
import androidx.mediarouter.media.MediaRouter.RouteInfo

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MediaRouterButton(
    modifier: Modifier = Modifier,
    routeSelector: MediaRouteSelector = MediaRouteSelector.EMPTY,
) {
    var refreshRoutes by remember { mutableIntStateOf(0) }
    var showDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val router = remember { MediaRouter.getInstance(context) }
    val callback = remember {
        object : MediaRouter.Callback() {
            override fun onRouteAdded(router: MediaRouter, route: RouteInfo) {
                refreshRoutes++
            }

            override fun onRouteRemoved(router: MediaRouter, route: RouteInfo) {
                refreshRoutes++
            }

            override fun onRouteChanged(router: MediaRouter, route: RouteInfo) {
                refreshRoutes++
            }

            override fun onRouteSelected(router: MediaRouter, route: RouteInfo, reason: Int) {
                refreshRoutes++
            }

            override fun onRouteUnselected(router: MediaRouter, route: RouteInfo, reason: Int) {
                refreshRoutes++
            }

            override fun onProviderAdded(router: MediaRouter, provider: MediaRouter.ProviderInfo) {
                refreshRoutes++
            }

            override fun onProviderRemoved(
                router: MediaRouter,
                provider: MediaRouter.ProviderInfo,
            ) {
                refreshRoutes++
            }

            override fun onProviderChanged(
                router: MediaRouter,
                provider: MediaRouter.ProviderInfo,
            ) {
                refreshRoutes++
            }
        }
    }
    val connectionState by remember(refreshRoutes) {
        val selectedRoute = router.selectedRoute
        val isRemote = !selectedRoute.isDefaultRouteOrBluetooth

        mutableIntStateOf(if (isRemote) selectedRoute.connectionState else RouteInfo.CONNECTION_STATE_DISCONNECTED)
    }
    val icon = remember(connectionState) {
        when (connectionState) {
            RouteInfo.CONNECTION_STATE_CONNECTING -> Icons.Default.Downloading // TODO Use the proper Cast animation
            RouteInfo.CONNECTION_STATE_CONNECTED -> Icons.Default.CastConnected
            else -> Icons.Default.Cast
        }
    }
    val contentDescriptionRes = remember(connectionState) {
        when (connectionState) {
            RouteInfo.CONNECTION_STATE_CONNECTING -> R.string.mr_cast_button_connecting
            RouteInfo.CONNECTION_STATE_CONNECTED -> R.string.mr_cast_button_connected
            else -> R.string.mr_cast_button_disconnected
        }
    }

    DisposableEffect(routeSelector) {
        if (!routeSelector.isEmpty) {
            router.addCallback(routeSelector, callback)
        }

        onDispose {
            if (!routeSelector.isEmpty) {
                router.removeCallback(callback)
            }
        }
    }

    if (showDialog) {
        if (router.selectedRoute.isDefaultRouteOrBluetooth) {
            MediaRouteChooserDialog(
                router = router,
                routeSelector = routeSelector,
                onDismissRequest = { showDialog = false },
            )
        } else {
            MediaRouteControllerDialog(
                router = router,
                onDismissRequest = { showDialog = false },
            )
        }
    }

    IconButton(
        onClick = { showDialog = true },
        modifier = modifier,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = stringResource(contentDescriptionRes),
        )
    }
}

// This is a copy of MediaRouter.RouteInfo.isDefaultOrBluetooth().
internal val RouteInfo.isDefaultRouteOrBluetooth: Boolean
    get() = if (isDefault || deviceType == RouteInfo.DEVICE_TYPE_BLUETOOTH_A2DP) {
        true
    } else {
        provider.providerInstance.metadata.packageName == "android" &&
                supportsControlCategory(MediaControlIntent.CATEGORY_LIVE_AUDIO) &&
                !supportsControlCategory(MediaControlIntent.CATEGORY_LIVE_VIDEO)
    }
