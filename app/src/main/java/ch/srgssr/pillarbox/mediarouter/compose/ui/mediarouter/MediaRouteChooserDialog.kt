package ch.srgssr.pillarbox.mediarouter.compose.ui.mediarouter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Drawable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.SpeakerGroup
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.mediarouter.R
import androidx.mediarouter.media.MediaRouteSelector
import androidx.mediarouter.media.MediaRouter
import androidx.mediarouter.media.MediaRouter.RouteInfo
import ch.srgssr.pillarbox.mediarouter.compose.ui.theme.PillarboxAndroidMediaRouterComposeTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
fun MediaRouteChooserDialog(
    router: MediaRouter,
    routeSelector: MediaRouteSelector,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
) {
    var refreshRoutes by remember { mutableIntStateOf(0) }
    var chooserState by remember {
        mutableStateOf(MediaRouterChooserDialogState.FINDING_DEVICES)
    }

    val context = LocalContext.current
    val callback = rememberRouterCallback { refreshRoutes++ }
    val routes = remember(refreshRoutes) {
        router.routes
            .filter { route ->
                !route.isDefaultRouteOrBluetooth &&
                        route.isEnabled &&
                        route.matchesSelector(routeSelector)
            }
            .sortedBy { it.name }
            .toMutableStateList()
    }

    DisposableEffect(routeSelector) {
        router.addCallback(routeSelector, callback, MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN)

        onDispose {
            router.removeCallback(callback)
        }
    }

    DisposableEffect(context) {
        val intentFilter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == Intent.ACTION_SCREEN_OFF) {
                    onDismissRequest()
                }
            }
        }

        context.registerReceiver(receiver, intentFilter)

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    LaunchedEffect(routes) {
        if (routes.isEmpty()) {
            chooserState = MediaRouterChooserDialogState.FINDING_DEVICES

            delay(5.seconds)

            chooserState = MediaRouterChooserDialogState.NO_DEVICES_NO_WIFI_HINT

            delay(15.seconds)

            chooserState = MediaRouterChooserDialogState.NO_ROUTES

            router.removeCallback(callback)
        } else {
            chooserState = MediaRouterChooserDialogState.SHOWING_ROUTES
        }
    }

    ChooserDialog(
        routes = routes,
        chooserState = chooserState,
        modifier = modifier,
        onDismissRequest = onDismissRequest,
    )
}

@Composable
private fun rememberRouterCallback(
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

            override fun onRouteSelected(
                router: MediaRouter,
                selectedRoute: RouteInfo,
                reason: Int,
                requestedRoute: RouteInfo
            ) {
                action()
            }
        }
    }
}

@Composable
private fun ChooserDialog(
    routes: List<RouteInfo>,
    chooserState: MediaRouterChooserDialogState,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            ConfirmButton(
                state = chooserState,
                onDismissRequest = onDismissRequest,
            )
        },
        modifier = modifier,
        title = {
            Title(chooserState)
        },
        text = {
            Text(
                state = chooserState,
                routes = routes,
                onRouteClick = { route ->
                    route.select()
                    onDismissRequest()
                },
            )
        },
    )
}

@Composable
private fun ConfirmButton(
    state: MediaRouterChooserDialogState,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
) {
    if (state == MediaRouterChooserDialogState.NO_ROUTES) {
        TextButton(
            onClick = onDismissRequest,
            modifier = modifier,
        ) {
            Text(text = stringResource(android.R.string.ok))
        }
    }
}

@Composable
private fun Title(
    state: MediaRouterChooserDialogState,
    modifier: Modifier = Modifier,
) {
    val titleRes = when (state) {
        MediaRouterChooserDialogState.FINDING_DEVICES,
        MediaRouterChooserDialogState.NO_DEVICES_NO_WIFI_HINT,
        MediaRouterChooserDialogState.SHOWING_ROUTES -> R.string.mr_chooser_title

        MediaRouterChooserDialogState.NO_ROUTES -> R.string.mr_chooser_zero_routes_found_title
    }

    Text(
        text = stringResource(titleRes),
        modifier = modifier,
    )
}

@Composable
private fun Text(
    state: MediaRouterChooserDialogState,
    routes: List<RouteInfo>,
    modifier: Modifier = Modifier,
    onRouteClick: (route: RouteInfo) -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        when (state) {
            MediaRouterChooserDialogState.FINDING_DEVICES -> FindingDevices()
            MediaRouterChooserDialogState.NO_DEVICES_NO_WIFI_HINT -> NoDevicesNoWifiHint()
            MediaRouterChooserDialogState.NO_ROUTES -> NoRoutes()
            MediaRouterChooserDialogState.SHOWING_ROUTES -> ShowingRoutes(
                routes = routes,
                onRouteClick = onRouteClick,
            )
        }
    }
}

@Composable
private fun ColumnScope.FindingDevices() {
    Text(text = stringResource(R.string.mr_chooser_looking_for_devices))

    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
}

@Composable
private fun ColumnScope.NoDevicesNoWifiHint() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Wifi,
            contentDescription = null,
        )

        Text(text = stringResource(R.string.mr_chooser_wifi_warning_description_unknown))
    }

    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
}

@Composable
private fun ColumnScope.NoRoutes() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Wifi,
            contentDescription = null,
        )

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(text = stringResource(R.string.mr_chooser_wifi_warning_description_unknown))

            Text(text = AnnotatedString.fromHtml(stringResource(R.string.mr_chooser_wifi_learn_more)))
        }
    }
}

@Composable
private fun ColumnScope.ShowingRoutes(
    routes: List<RouteInfo>,
    onRouteClick: (route: RouteInfo) -> Unit,
) {
    LazyColumn {
        items(routes) { route ->
            val isConnectedOrConnecting =
                route.connectionState == RouteInfo.CONNECTION_STATE_CONNECTED
                        || route.connectionState == RouteInfo.CONNECTION_STATE_CONNECTING

            ListItem(
                headlineContent = { Text(text = route.name) },
                modifier = Modifier.clickable(enabled = route.isEnabled) {
                    onRouteClick(route)
                },
                supportingContent =
                    if (isConnectedOrConnecting || !route.description.isNullOrBlank()) {
                        {
                            Text(text = route.description.orEmpty())
                        }
                    } else {
                        null
                    },
                leadingContent = {
                    val context = LocalContext.current
                    val drawable = remember(route.iconUri) {
                        route.iconUri
                            ?.let { context.contentResolver.openInputStream(it) }
                            ?.use { Drawable.createFromStream(it, null) }
                    }

                    if (drawable != null) {
                        Icon(
                            bitmap = drawable.toBitmap().asImageBitmap(),
                            contentDescription = null,
                        )
                    } else {
                        val defaultIcon = when (route.deviceType) {
                            RouteInfo.DEVICE_TYPE_TV -> Icons.Default.Tv
                            RouteInfo.DEVICE_TYPE_REMOTE_SPEAKER -> Icons.Default.Speaker
                            else -> if (route.isGroup) {
                                Icons.Default.SpeakerGroup
                            } else {
                                Icons.Default.Cast
                            }
                        }

                        Icon(
                            imageVector = defaultIcon,
                            contentDescription = null,
                        )
                    }
                },
                colors = ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
            )
        }
    }

    if (routes.isEmpty()) {
        Text(text = "No routes found!")
    }
}

private enum class MediaRouterChooserDialogState {
    FINDING_DEVICES,
    NO_DEVICES_NO_WIFI_HINT,
    NO_ROUTES,
    SHOWING_ROUTES,
}

@Preview
@Composable
private fun ChooserDialogFindingDevicesPreview() {
    PillarboxAndroidMediaRouterComposeTheme {
        ChooserDialog(
            routes = emptyList(),
            chooserState = MediaRouterChooserDialogState.FINDING_DEVICES,
            onDismissRequest = {},
        )
    }
}

@Preview
@Composable
private fun ChooserDialogNoDevicesNoWifiHintPreview() {
    PillarboxAndroidMediaRouterComposeTheme {
        ChooserDialog(
            routes = emptyList(),
            chooserState = MediaRouterChooserDialogState.NO_DEVICES_NO_WIFI_HINT,
            onDismissRequest = {},
        )
    }
}

@Preview
@Composable
private fun ChooserDialogNoRoutesPreview() {
    PillarboxAndroidMediaRouterComposeTheme {
        ChooserDialog(
            routes = emptyList(),
            chooserState = MediaRouterChooserDialogState.NO_ROUTES,
            onDismissRequest = {},
        )
    }
}

@Preview
@Composable
private fun ChooserDialogShowingRoutesPreview() {
    PillarboxAndroidMediaRouterComposeTheme {
        ChooserDialog(
            routes = emptyList(),
            chooserState = MediaRouterChooserDialogState.SHOWING_ROUTES,
            onDismissRequest = {},
        )
    }
}
