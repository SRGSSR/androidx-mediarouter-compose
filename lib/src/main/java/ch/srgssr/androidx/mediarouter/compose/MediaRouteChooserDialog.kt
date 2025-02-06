package ch.srgssr.androidx.mediarouter.compose

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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.mediarouter.R
import androidx.mediarouter.media.MediaRouteSelector
import androidx.mediarouter.media.MediaRouter
import androidx.mediarouter.media.MediaRouter.RouteInfo
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

/**
 * This class implements the route chooser dialog for [MediaRouter].
 *
 * This dialog allows the user to choose a route that matches a given selector.
 *
 * @param routeSelector The media route selector for filtering the routes that the user can select
 * using the media route chooser dialog.
 * @param modifier The [Modifier] to be applied to this dialog.
 * @param onDismissRequest The action to perform when this dialog is dismissed.
 *
 * @see MediaRouteButton
 */
@Composable
fun MediaRouteChooserDialog(
    routeSelector: MediaRouteSelector,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
) {
    var mediaRouterCallbackTriggered by remember { mutableIntStateOf(0) }
    var chooserState by remember {
        mutableStateOf(MediaRouteChooserDialogState.FINDING_DEVICES)
    }

    val context = LocalContext.current
    val router = remember { MediaRouter.getInstance(context) }
    val mediaRouterCallback = rememberMediaRouterCallback { mediaRouterCallbackTriggered++ }
    val routes = remember(mediaRouterCallbackTriggered) {
        router.routes
            .filter { route ->
                !route.isDefaultOrBluetooth &&
                        route.isEnabled &&
                        route.matchesSelector(routeSelector)
            }
            .sortedBy { it.name }
            .toMutableStateList()
    }

    DisposableEffect(routeSelector) {
        router.addCallback(
            routeSelector,
            mediaRouterCallback,
            MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN,
        )

        onDispose {
            router.removeCallback(mediaRouterCallback)
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
            chooserState = MediaRouteChooserDialogState.FINDING_DEVICES

            delay(5.seconds)

            chooserState = MediaRouteChooserDialogState.NO_DEVICES_NO_WIFI_HINT

            delay(15.seconds)

            chooserState = MediaRouteChooserDialogState.NO_ROUTES

            router.removeCallback(mediaRouterCallback)
        } else {
            chooserState = MediaRouteChooserDialogState.SHOWING_ROUTES
        }
    }

    ChooserDialog(
        routes = routes,
        state = chooserState,
        modifier = modifier,
        onDismissRequest = onDismissRequest,
    )
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
    state: MediaRouteChooserDialogState,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            if (state == MediaRouteChooserDialogState.NO_ROUTES) {
                TextButton(
                    onClick = onDismissRequest,
                    modifier = modifier,
                ) {
                    Text(text = stringResource(android.R.string.ok))
                }
            }
        },
        modifier = modifier,
        title = {
            val titleRes = when (state) {
                MediaRouteChooserDialogState.FINDING_DEVICES,
                MediaRouteChooserDialogState.NO_DEVICES_NO_WIFI_HINT,
                MediaRouteChooserDialogState.SHOWING_ROUTES -> R.string.mr_chooser_title

                MediaRouteChooserDialogState.NO_ROUTES -> R.string.mr_chooser_zero_routes_found_title
            }

            Text(
                text = stringResource(titleRes),
                modifier = modifier,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        },
        text = {
            ChooserDialogContent(
                state = state,
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
private fun ChooserDialogContent(
    state: MediaRouteChooserDialogState,
    routes: List<RouteInfo>,
    modifier: Modifier = Modifier,
    onRouteClick: (route: RouteInfo) -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        when (state) {
            MediaRouteChooserDialogState.FINDING_DEVICES -> FindingState()
            MediaRouteChooserDialogState.NO_DEVICES_NO_WIFI_HINT -> NoDevicesNoWifiHint()
            MediaRouteChooserDialogState.NO_ROUTES -> NoRoutes()
            MediaRouteChooserDialogState.SHOWING_ROUTES -> ShowingRoutes(
                routes = routes,
                onRouteClick = onRouteClick,
            )
        }
    }
}

@Composable
private fun ColumnScope.FindingState() {
    Text(text = stringResource(R.string.mr_chooser_looking_for_devices))

    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
}

@Composable
private fun ColumnScope.NoDevicesNoWifiHint() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val context = LocalContext.current

        Icon(
            imageVector = Icons.Wifi,
            contentDescription = stringResource(R.string.ic_media_route_learn_more_accessibility),
        )

        Text(text = DeviceUtils.getDialogChooserWifiWarningDescription(context))
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
            imageVector = Icons.Wifi,
            contentDescription = stringResource(R.string.ic_media_route_learn_more_accessibility),
        )

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            val context = LocalContext.current
            val uriHandler = LocalUriHandler.current

            Text(text = DeviceUtils.getDialogChooserWifiWarningDescription(context))

            LearnMoreLink(
                url = "https://support.google.com/chromecast/?p=trouble-finding-devices",
                onUrlClick = uriHandler::openUri,
            )
        }
    }
}

@Composable
private fun LearnMoreLink(
    url: String,
    modifier: Modifier = Modifier,
    onUrlClick: (url: String) -> Unit,
) {
    val link = LinkAnnotation.Url(
        url = url,
        styles = TextLinkStyles(style = SpanStyle(color = MaterialTheme.colorScheme.primary)),
        linkInteractionListener = { onUrlClick(url) },
    )

    Text(
        text = buildAnnotatedString {
            withLink(link) {
                append(stringResource(R.string.mr_chooser_wifi_learn_more))
            }
        },
        modifier = modifier,
    )
}

@Composable
private fun ColumnScope.ShowingRoutes(
    routes: List<RouteInfo>,
    onRouteClick: (route: RouteInfo) -> Unit,
) {
    LazyColumn {
        items(
            items = routes,
            key = { it.id },
        ) { route ->
            RouteItem(
                route = route,
                onRouteClick = onRouteClick,
            )
        }
    }
}

@Composable
private fun RouteItem(
    route: RouteInfo,
    modifier: Modifier = Modifier,
    onRouteClick: (route: RouteInfo) -> Unit,
) {
    val isConnectedOrConnecting =
        route.connectionState == RouteInfo.CONNECTION_STATE_CONNECTED
                || route.connectionState == RouteInfo.CONNECTION_STATE_CONNECTING

    ListItem(
        headlineContent = {
            Text(
                text = route.name,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        },
        modifier = modifier.clickable(route.isEnabled) {
            onRouteClick(route)
        },
        supportingContent = if (isConnectedOrConnecting && !route.description.isNullOrBlank()) {
            {
                Text(
                    text = route.description.orEmpty(),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
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
                val defaultIcon = when {
                    route.deviceType == RouteInfo.DEVICE_TYPE_TV -> Icons.Tv
                    route.deviceType == RouteInfo.DEVICE_TYPE_REMOTE_SPEAKER -> Icons.Speaker
                    route.isGroup -> Icons.SpeakerGroup
                    else -> Icons.Cast
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

enum class MediaRouteChooserDialogState {
    FINDING_DEVICES,
    SHOWING_ROUTES,
    NO_DEVICES_NO_WIFI_HINT,
    NO_ROUTES,
}

@Preview
@Composable
private fun ChooserDialogFindingDevicesPreview() {
    MaterialTheme {
        ChooserDialog(
            routes = emptyList(),
            state = MediaRouteChooserDialogState.FINDING_DEVICES,
            onDismissRequest = {},
        )
    }
}

@Preview
@Composable
private fun ChooserDialogNoDevicesNoWifiHintPreview() {
    MaterialTheme {
        ChooserDialog(
            routes = emptyList(),
            state = MediaRouteChooserDialogState.NO_DEVICES_NO_WIFI_HINT,
            onDismissRequest = {},
        )
    }
}

@Preview
@Composable
private fun ChooserDialogNoRoutesPreview() {
    MaterialTheme {
        ChooserDialog(
            routes = emptyList(),
            state = MediaRouteChooserDialogState.NO_ROUTES,
            onDismissRequest = {},
        )
    }
}

@Preview
@Composable
private fun ChooserDialogShowingRoutesPreview() {
    MaterialTheme {
        ChooserDialog(
            routes = emptyList(),
            state = MediaRouteChooserDialogState.SHOWING_ROUTES,
            onDismissRequest = {},
        )
    }
}
