package ch.srgssr.androidx.mediarouter.compose

import android.graphics.drawable.Drawable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.mediarouter.R
import androidx.mediarouter.media.MediaRouteSelector
import androidx.mediarouter.media.MediaRouter
import androidx.mediarouter.media.MediaRouter.RouteInfo
import ch.srgssr.androidx.mediarouter.compose.MediaRouteChooserDialogViewModel.ChooserState

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
    val viewModel = viewModel<MediaRouteChooserDialogViewModel>(
        key = routeSelector.toString(),
        factory = MediaRouteChooserDialogViewModel.Factory(routeSelector),
    )
    val showDialog by viewModel.showDialog.collectAsState()
    val routes by viewModel.routes.collectAsState()
    val chooserState by viewModel.chooserState.collectAsState()
    val title by viewModel.title.collectAsState()
    val confirmButtonLabel by viewModel.confirmButtonLabel.collectAsState()

    LaunchedEffect(showDialog) {
        if (!showDialog) {
            onDismissRequest()
        }
    }

    ChooserDialog(
        routes = routes,
        state = chooserState,
        title = title,
        confirmButtonLabel = confirmButtonLabel,
        modifier = modifier,
        onDismissRequest = viewModel::hideDialog,
    )
}

@Composable
private fun ChooserDialog(
    routes: List<RouteInfo>,
    state: ChooserState,
    title: String,
    confirmButtonLabel: String?,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            if (confirmButtonLabel != null) {
                TextButton(onClick = onDismissRequest) {
                    Text(text = confirmButtonLabel)
                }
            }
        },
        modifier = modifier,
        title = {
            Text(
                text = title,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        },
        text = {
            when (state) {
                ChooserState.FindingDevices -> FindingDevices()
                ChooserState.NoDevicesNoWifiHint -> NoDevicesNoWifiHint()
                ChooserState.NoRoutes -> NoRoutes()
                ChooserState.ShowingRoutes -> ShowingRoutes(
                    routes = routes,
                    onRouteClick = { route ->
                        route.select()
                        onDismissRequest()
                    },
                )
            }
        },
    )
}

@Composable
private fun FindingDevices(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = stringResource(R.string.mr_chooser_looking_for_devices))

        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun NoDevicesNoWifiHint(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Wifi,
                contentDescription = stringResource(R.string.ic_media_route_learn_more_accessibility),
            )

            Text(text = DeviceUtils.getDialogChooserWifiWarningDescription(LocalContext.current))
        }

        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun NoRoutes(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Wifi,
            contentDescription = stringResource(R.string.ic_media_route_learn_more_accessibility),
        )

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            val uriHandler = LocalUriHandler.current
            val url = "https://support.google.com/chromecast/?p=trouble-finding-devices"
            val link = LinkAnnotation.Url(
                url = url,
                styles = TextLinkStyles(style = SpanStyle(color = MaterialTheme.colorScheme.primary)),
                linkInteractionListener = { uriHandler.openUri(url) },
            )

            Text(text = DeviceUtils.getDialogChooserWifiWarningDescription(LocalContext.current))

            Text(
                text = buildAnnotatedString {
                    withLink(link) {
                        append(stringResource(R.string.mr_chooser_wifi_learn_more))
                    }
                },
            )
        }
    }
}

@Composable
private fun ShowingRoutes(
    routes: List<RouteInfo>,
    modifier: Modifier = Modifier,
    onRouteClick: (route: RouteInfo) -> Unit,
) {
    LazyColumn(modifier = modifier) {
        items(
            items = routes,
            key = { it.id },
        ) { route ->
            RouteItem(
                route = route,
                modifier = Modifier.animateItem(),
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

@Preview
@Composable
private fun ChooserDialogFindingDevicesPreview() {
    MaterialTheme {
        ChooserDialog(
            routes = emptyList(),
            state = ChooserState.FindingDevices,
            title = stringResource(R.string.mr_chooser_title),
            confirmButtonLabel = null,
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
            state = ChooserState.NoDevicesNoWifiHint,
            title = stringResource(R.string.mr_chooser_title),
            confirmButtonLabel = null,
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
            state = ChooserState.NoRoutes,
            title = stringResource(R.string.mr_chooser_zero_routes_found_title),
            confirmButtonLabel = stringResource(android.R.string.ok),
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
            state = ChooserState.ShowingRoutes,
            title = stringResource(R.string.mr_chooser_title),
            confirmButtonLabel = null,
            onDismissRequest = {},
        )
    }
}
