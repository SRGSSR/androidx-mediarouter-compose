package ch.srgssr.androidx.mediarouter.compose

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.mediarouter.R
import androidx.mediarouter.media.MediaRouteSelector
import androidx.mediarouter.media.MediaRouter
import androidx.mediarouter.media.MediaRouter.RouteInfo
import coil3.compose.AsyncImage

/**
 * This class implements the route controller dialog for [MediaRouter].
 *
 * This dialog allows the user to control or disconnect from the currently selected route.
 *
 * @param routeSelector The media route selector for filtering the routes that the user can select
 * using the media route chooser dialog.
 * @param modifier The [Modifier] to be applied to this dialog.
 * @param volumeControlEnabled Whether to enable the volume slider and volume control using the
 * volume keys when the route supports it.
 * @param onDismissRequest The action to perform when this dialog is dismissed.
 * @param customControlView The view that will replace the default media controls for the currently
 * playing content.
 *
 * @see MediaRouteButton
 */
@Composable
fun MediaRouteControllerDialog(
    routeSelector: MediaRouteSelector,
    modifier: Modifier = Modifier,
    volumeControlEnabled: Boolean = true,
    onDismissRequest: () -> Unit,
    customControlView: @Composable (() -> Unit)? = null,
) {
    val viewModel = viewModel<MediaRouteControllerDialogViewModel>(
        key = routeSelector.toString(),
        factory = MediaRouteControllerDialogViewModel.Factory(volumeControlEnabled),
    )
    val showDialog by viewModel.showDialog.collectAsState()
    val selectedRoute by viewModel.selectedRoute.collectAsState()
    val isDeviceGroupExpanded by viewModel.isDeviceGroupExpanded.collectAsState()
    val showPlaybackControl by viewModel.showPlaybackControl.collectAsState()
    val showVolumeControl by viewModel.showVolumeControl.collectAsState()
    val imageModel by viewModel.imageModel.collectAsState()
    val title by viewModel.title.collectAsState()
    val subtitle by viewModel.subtitle.collectAsState()
    val iconInfo by viewModel.iconInfo.collectAsState()

    LaunchedEffect(showDialog) {
        if (!showDialog) {
            onDismissRequest()
        }
    }

    ControllerDialog(
        route = selectedRoute,
        volumeControlEnabled = volumeControlEnabled,
        imageModel = imageModel,
        title = title,
        subtitle = subtitle,
        iconInfo = iconInfo,
        isDeviceGroupExpanded = isDeviceGroupExpanded,
        showPlaybackControl = showPlaybackControl,
        showVolumeControl = showVolumeControl,
        modifier = modifier,
        customControlView = customControlView,
        toggleDeviceGroup = viewModel::toggleDeviceGroup,
        onKeyEvent = viewModel::onKeyEvent,
        onPlaybackTitleClick = viewModel::startSessionActivity,
        onPlaybackIconClick = viewModel::onPlaybackIconClick,
        onStopCasting = viewModel::stopCasting,
        onDisconnect = viewModel::disconnect,
        onDismissRequest = viewModel::hideDialog,
    )
}

@Composable
private fun ControllerDialog(
    route: RouteInfo,
    volumeControlEnabled: Boolean,
    imageModel: Any?,
    title: String?,
    subtitle: String?,
    iconInfo: Pair<ImageVector, String>?,
    isDeviceGroupExpanded: Boolean,
    showPlaybackControl: Boolean,
    showVolumeControl: Boolean,
    modifier: Modifier = Modifier,
    customControlView: @Composable (() -> Unit)?,
    toggleDeviceGroup: () -> Unit,
    onKeyEvent: (keyEvent: KeyEvent) -> Boolean,
    onPlaybackTitleClick: () -> Unit,
    onPlaybackIconClick: () -> Unit,
    onStopCasting: () -> Unit,
    onDisconnect: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onStopCasting) {
                Text(text = stringResource(R.string.mr_controller_stop_casting))
            }
        },
        modifier = modifier.onKeyEvent(onKeyEvent),
        dismissButton = if (route.canDisconnect()) {
            {
                TextButton(onClick = onDisconnect) {
                    Text(text = stringResource(R.string.mr_controller_disconnect))
                }
            }
        } else {
            null
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = route.name,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )

                IconButton(onClick = onDismissRequest) {
                    Icon(
                        imageVector = Icons.Close,
                        contentDescription = stringResource(R.string.mr_controller_close_description),
                    )
                }
            }
        },
        text = {
            ControllerDialogContent(
                route = route,
                volumeControlEnabled = volumeControlEnabled,
                imageModel = imageModel,
                title = title,
                subtitle = subtitle,
                iconInfo = iconInfo,
                isDeviceGroupExpanded = isDeviceGroupExpanded,
                showPlaybackControl = showPlaybackControl,
                showVolumeControl = showVolumeControl,
                modifier = Modifier.fillMaxWidth(),
                customControlView = customControlView,
                onToggleDeviceGroup = toggleDeviceGroup,
                onPlaybackTitleClick = onPlaybackTitleClick,
                onPlaybackIconClick = onPlaybackIconClick,
            )
        },
    )
}

@Composable
private fun ControllerDialogContent(
    route: RouteInfo,
    volumeControlEnabled: Boolean,
    imageModel: Any?,
    title: String?,
    subtitle: String?,
    iconInfo: Pair<ImageVector, String>?,
    isDeviceGroupExpanded: Boolean,
    showPlaybackControl: Boolean,
    showVolumeControl: Boolean,
    modifier: Modifier = Modifier,
    customControlView: @Composable (() -> Unit)?,
    onToggleDeviceGroup: () -> Unit,
    onPlaybackTitleClick: () -> Unit,
    onPlaybackIconClick: () -> Unit,
) {
    @Suppress("NoNameShadowing")
    val showPlaybackControl = showPlaybackControl && customControlView == null

    Column(modifier = modifier) {
        customControlView?.invoke()

        if (imageModel != null) {
            AsyncImage(
                model = imageModel,
                contentDescription = stringResource(R.string.mr_controller_album_art),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPlaybackTitleClick() },
                contentScale = ContentScale.FillBounds,
            )
        }

        if (!showVolumeControl && !showPlaybackControl) {
            return
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
        ) {
            if (showPlaybackControl) {
                PlaybackControlRow(
                    title = title,
                    subtitle = subtitle,
                    icon = iconInfo?.first,
                    contentDescription = iconInfo?.second,
                    modifier = Modifier.fillMaxWidth(),
                    onTitleClick = onPlaybackTitleClick,
                    onIconClick = onPlaybackIconClick,
                )
            }

            if (showPlaybackControl && showVolumeControl) {
                HorizontalDivider()
            }

            if (showVolumeControl) {
                VolumeControl(
                    route = route,
                    modifier = Modifier.fillMaxWidth(),
                    isExpanded = isDeviceGroupExpanded,
                    onExpandCollapseClick = onToggleDeviceGroup,
                )
            }
        }

        if (isDeviceGroupExpanded) {
            DeviceGroup(
                routes = route.memberRoutes,
                volumeControlEnabled = volumeControlEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            )
        }
    }
}

@Composable
private fun PlaybackControlRow(
    title: CharSequence?,
    subtitle: CharSequence?,
    icon: ImageVector?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    onTitleClick: () -> Unit,
    onIconClick: () -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.clickable { onTitleClick() },
        ) {
            if (title != null) {
                Text(
                    text = title.toString(),
                    maxLines = 1,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            if (subtitle != null) {
                Text(
                    text = subtitle.toString(),
                    maxLines = 1,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        if (icon != null) {
            IconButton(onClick = onIconClick) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    modifier = modifier,
                )
            }
        }
    }
}

@Composable
private fun VolumeControl(
    route: RouteInfo,
    modifier: Modifier = Modifier,
    isExpanded: Boolean,
    onExpandCollapseClick: () -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        var volume by remember { mutableFloatStateOf(route.volume.toFloat()) }

        Icon(
            imageVector = Icons.Audiotrack,
            contentDescription = null,
        )

        Slider(
            value = volume,
            onValueChange = {
                volume = it

                route.requestSetVolume(it.toInt())
            },
            modifier = Modifier.weight(1f),
            valueRange = 0f..route.volumeMax.toFloat(),
        )

        if (route.isGroup && route.memberRoutes.size > 1) {
            IconButton(onClick = onExpandCollapseClick) {
                val scale by animateFloatAsState(targetValue = if (isExpanded) -1f else 1f)
                val contentDescriptionRes = if (isExpanded) {
                    R.string.mr_controller_collapse_group
                } else {
                    R.string.mr_controller_expand_group
                }

                Icon(
                    imageVector = Icons.ExpandMore,
                    contentDescription = stringResource(contentDescriptionRes),
                    modifier = Modifier.scale(scaleX = 1f, scaleY = scale),
                )
            }
        }
    }
}

@Composable
private fun DeviceGroup(
    routes: List<RouteInfo>,
    volumeControlEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(routes) { route ->
            val isVolumeControlEnabled = volumeControlEnabled
                    && route.volumeHandling == RouteInfo.PLAYBACK_VOLUME_VARIABLE
            val volumeRange = 0f..route.volumeMax.toFloat()
            val enabled = route.isEnabled

            var volume by remember {
                mutableFloatStateOf(if (isVolumeControlEnabled) route.volume.toFloat() else 100f)
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = route.name,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodySmall,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Audiotrack,
                        contentDescription = null,
                    )

                    Slider(
                        value = volume,
                        onValueChange = {
                            if (isVolumeControlEnabled) {
                                volume = it

                                route.requestSetVolume(it.toInt())
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = enabled,
                        valueRange = volumeRange,
                    )
                }
            }
        }
    }
}
