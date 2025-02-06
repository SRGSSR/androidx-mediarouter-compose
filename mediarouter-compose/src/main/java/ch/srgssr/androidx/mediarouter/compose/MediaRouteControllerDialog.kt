package ch.srgssr.androidx.mediarouter.compose

import android.app.PendingIntent
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.ACTION_PAUSE
import android.support.v4.media.session.PlaybackStateCompat.ACTION_PLAY
import android.support.v4.media.session.PlaybackStateCompat.ACTION_PLAY_PAUSE
import android.support.v4.media.session.PlaybackStateCompat.ACTION_STOP
import android.util.Log
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.mediarouter.R
import androidx.mediarouter.media.MediaRouteSelector
import androidx.mediarouter.media.MediaRouter
import androidx.mediarouter.media.MediaRouter.RouteInfo

/**
 * This class implements the route controller dialog for [MediaRouter].
 *
 * This dialog allows the user to control or disconnect from the currently selected route.
 *
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
    modifier: Modifier = Modifier,
    volumeControlEnabled: Boolean = true,
    onDismissRequest: () -> Unit,
    customControlView: @Composable (() -> Unit)? = null,
) {
    var mediaRouterCallbackTriggered by remember { mutableIntStateOf(0) }
    var mediaController by remember { mutableStateOf<MediaControllerCompat?>(null) }
    var playbackState by remember(mediaController) { mutableStateOf(mediaController?.playbackState) }
    var description by remember(mediaController) { mutableStateOf(mediaController?.metadata?.description) }

    val context = LocalContext.current
    val router = remember { MediaRouter.getInstance(context) }
    val selectedRoute by remember(mediaRouterCallbackTriggered) {
        mutableStateOf(router.selectedRoute)
    }
    val mediaControllerCallback = remember {
        object : MediaControllerCompat.Callback() {
            override fun onSessionDestroyed() {
                mediaController?.unregisterCallback(this)
                mediaController = null
            }

            override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
                playbackState = state
                mediaRouterCallbackTriggered++
            }

            override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
                description = metadata?.description
                // updateArtIconIfNeeded() // TODO
                mediaRouterCallbackTriggered++
            }
        }
    }
    val mediaRouterCallback = remember {
        object : MediaRouter.Callback() {
            override fun onRouteUnselected(router: MediaRouter, route: RouteInfo, reason: Int) {
                mediaRouterCallbackTriggered++
            }

            override fun onRouteChanged(router: MediaRouter, route: RouteInfo) {
                mediaRouterCallbackTriggered++
            }

            override fun onRouteVolumeChanged(router: MediaRouter, route: RouteInfo) {
                // TODO
            }
        }
    }

    DisposableEffect(Unit) {
        router.addCallback(
            MediaRouteSelector.EMPTY,
            mediaRouterCallback,
            MediaRouter.CALLBACK_FLAG_UNFILTERED_EVENTS,
        )

        onDispose {
            router.removeCallback(mediaRouterCallback)
        }
    }

    DisposableEffect(router.mediaSessionToken) {
        router.mediaSessionToken?.let { mediaSessionToken ->
            mediaController = MediaControllerCompat(context, mediaSessionToken)
            mediaController?.registerCallback(mediaControllerCallback)

            // updateArtIconIfNeeded() // TODO
            mediaRouterCallbackTriggered++
        }

        onDispose {
            mediaController?.unregisterCallback(mediaControllerCallback)
            mediaController = null
        }
    }

    ControllerDialog(
        route = selectedRoute,
        volumeControlEnabled = volumeControlEnabled,
        playbackState = playbackState,
        mediaController = mediaController,
        description = description,
        modifier = modifier,
        customControlView = customControlView,
        onUnselectRoute = { reason ->
            if (selectedRoute.isSelected) {
                router.unselect(reason)
            }

            onDismissRequest()
        },
        onDismissRequest = onDismissRequest,
    )
}

@Composable
@Suppress("CyclomaticComplexMethod", "LongMethod")
private fun ControllerDialog(
    route: RouteInfo,
    volumeControlEnabled: Boolean,
    playbackState: PlaybackStateCompat?,
    mediaController: MediaControllerCompat?,
    description: MediaDescriptionCompat?,
    modifier: Modifier = Modifier,
    customControlView: @Composable (() -> Unit)?,
    onUnselectRoute: (reason: Int) -> Unit,
    onDismissRequest: () -> Unit,
) {
    var isDeviceGroupExpanded by remember { mutableStateOf(false) }

    val isGroupVolumeUxEnabled = remember { MediaRouter.isGroupVolumeUxEnabled() }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = { onUnselectRoute(MediaRouter.UNSELECT_REASON_STOPPED) }) {
                Text(text = stringResource(R.string.mr_controller_stop_casting))
            }
        },
        modifier = modifier.onKeyEvent { keyEvent ->
            if (keyEvent.key == Key.VolumeDown || keyEvent.key == Key.VolumeUp) {
                if (keyEvent.type == KeyEventType.KeyDown && (isGroupVolumeUxEnabled || !isDeviceGroupExpanded)) {
                    val delta = if (keyEvent.key == Key.VolumeDown) -1 else 1

                    route.requestUpdateVolume(delta)
                }

                true
            } else {
                false
            }
        },
        dismissButton = if (route.canDisconnect()) {
            {
                TextButton(onClick = { onUnselectRoute(MediaRouter.UNSELECT_REASON_DISCONNECTED) }) {
                    Text(text = stringResource(R.string.mr_controller_disconnect))
                }
            }
        } else {
            null
        },
        title = {
            Title(
                title = route.name,
                modifier = Modifier.fillMaxWidth(),
                onClose = onDismissRequest,
            )
        },
        text = {
            ControllerDialogContent(
                route = route,
                volumeControlEnabled = volumeControlEnabled,
                isGroupVolumeUxEnabled = isGroupVolumeUxEnabled,
                playbackState = playbackState,
                description = description,
                isDeviceGroupExpanded = isDeviceGroupExpanded,
                modifier = Modifier.fillMaxWidth(),
                customControlView = customControlView,
                onToggleDeviceGroup = { isDeviceGroupExpanded = !isDeviceGroupExpanded },
                onPlaybackTitleClick = {
                    mediaController?.let {
                        it.sessionActivity?.let { pendingIntent ->
                            try {
                                pendingIntent.send()
                                onDismissRequest()
                            } catch (exception: PendingIntent.CanceledException) {
                                Log.d(
                                    "MediaRouteController",
                                    "$pendingIntent was not sent, it has been canceled.",
                                    exception,
                                )
                            }
                        }
                    }
                },
                onPlaybackIconClick = {
                    if (mediaController != null && playbackState != null) {
                        val isPlaying = playbackState.state == PlaybackStateCompat.STATE_PLAYING

                        if (isPlaying && playbackState.isPauseActionSupported) {
                            mediaController.transportControls.pause()
                        } else if (isPlaying && playbackState.isStopActionSupported) {
                            mediaController.transportControls.stop()
                        } else if (!isPlaying && playbackState.isPlayActionSupported) {
                            mediaController.transportControls.play()
                        }
                    }
                },
            )
        },
    )
}

@Composable
private fun Title(
    title: String,
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
        )

        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.Close,
                contentDescription = stringResource(R.string.mr_controller_close_description),
            )
        }
    }
}

@Composable
private fun ControllerDialogContent(
    route: RouteInfo,
    volumeControlEnabled: Boolean,
    isGroupVolumeUxEnabled: Boolean,
    playbackState: PlaybackStateCompat?,
    description: MediaDescriptionCompat?,
    isDeviceGroupExpanded: Boolean,
    modifier: Modifier = Modifier,
    customControlView: @Composable (() -> Unit)?,
    onToggleDeviceGroup: () -> Unit,
    onPlaybackTitleClick: () -> Unit,
    onPlaybackIconClick: () -> Unit,
) {
    // TODO Display mr_custom_control
    // TODO Display mr_art

    Column(modifier = modifier) {
        val showPlaybackControl =
            customControlView == null && (description != null || playbackState != null)
        val showVolumeControl =
            showVolumeControl(route, volumeControlEnabled, isGroupVolumeUxEnabled)

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
                    route = route,
                    playbackState = playbackState,
                    description = description,
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
    route: RouteInfo,
    playbackState: PlaybackStateCompat?,
    description: MediaDescriptionCompat?,
    modifier: Modifier = Modifier,
    onTitleClick: () -> Unit,
    onIconClick: () -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlaybackControlTitle(
            route = route,
            playbackState = playbackState,
            description = description,
            modifier = Modifier.clickable { onTitleClick() },
        )

        if (playbackState != null) {
            PlaybackControlIcon(
                playbackState = playbackState,
                onClick = onIconClick,
            )
        }
    }
}

@Composable
private fun PlaybackControlTitle(
    route: RouteInfo,
    playbackState: PlaybackStateCompat?,
    description: MediaDescriptionCompat?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        var title = description?.title
        var showTitle = !title.isNullOrEmpty()

        val subtitle = description?.subtitle
        val showSubtitle = !subtitle.isNullOrEmpty()

        if (route.presentationDisplayId != RouteInfo.PRESENTATION_DISPLAY_ID_NONE) {
            title = stringResource(R.string.mr_controller_casting_screen)
            showTitle = true
        } else if (playbackState == null || playbackState.state == PlaybackStateCompat.STATE_NONE) {
            title = stringResource(R.string.mr_controller_no_media_selected)
            showTitle = true
        } else if (!showTitle && !showSubtitle) {
            title = stringResource(R.string.mr_controller_no_info_available)
            showTitle = true
        }

        if (showTitle) {
            Text(
                text = title.toString(),
                maxLines = 1,
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        if (showSubtitle) {
            Text(
                text = subtitle.toString(),
                maxLines = 1,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun PlaybackControlIcon(
    playbackState: PlaybackStateCompat,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val isPlaying = playbackState.state == PlaybackStateCompat.STATE_BUFFERING
            || playbackState.state == PlaybackStateCompat.STATE_PLAYING

    val icon: ImageVector
    val contentDescription: String
    if (isPlaying && playbackState.isPauseActionSupported) {
        icon = Icons.Pause
        contentDescription = stringResource(R.string.mr_controller_pause)
    } else if (isPlaying && playbackState.isStopActionSupported) {
        icon = Icons.Stop
        contentDescription = stringResource(R.string.mr_controller_stop)
    } else if (!isPlaying && playbackState.isPlayActionSupported) {
        icon = Icons.PlayArrow
        contentDescription = stringResource(R.string.mr_controller_play)
    } else {
        return
    }

    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = modifier,
        )
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

private fun showVolumeControl(
    route: RouteInfo,
    volumeControlEnabled: Boolean,
    isGroupVolumeUxEnabled: Boolean,
): Boolean {
    return route.volumeHandling == RouteInfo.PLAYBACK_VOLUME_VARIABLE
            && (isGroupVolumeUxEnabled || !(route.isGroup && route.memberRoutes.size > 1))
            && volumeControlEnabled
}

private val PlaybackStateCompat.isPlayActionSupported: Boolean
    get() = actions and (ACTION_PLAY or ACTION_PLAY_PAUSE) != 0L

private val PlaybackStateCompat.isPauseActionSupported: Boolean
    get() = actions and (ACTION_PAUSE or ACTION_PLAY_PAUSE) != 0L

private val PlaybackStateCompat.isStopActionSupported: Boolean
    get() = actions and ACTION_STOP != 0L
