package ch.srgssr.pillarbox.mediarouter.compose.ui.mediarouter

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current
    val router = remember { MediaRouter.getInstance(context) }

    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        if (!router.selectedRoute.isDefaultRouteOrBluetooth) {
            MediaRouteControllerDialog(
                router = router,
                onDismissRequest = { showDialog = false },
            )
        } else {
            MediaRouteChooserDialog(
                router = router,
                routeSelector = routeSelector,
                onDismissRequest = { showDialog = false },
            )
        }
    }

    IconButton(
        onClick = { showDialog = true },
        modifier = modifier,
    ) {
        Icon(
            imageVector = Icons.Default.Cast,
            contentDescription = null,
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
