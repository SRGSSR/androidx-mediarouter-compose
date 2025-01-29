package ch.srgssr.pillarbox.mediarouter.compose.ui.mediarouter

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.mediarouter.R
import androidx.mediarouter.media.MediaRouter
import androidx.mediarouter.media.MediaRouter.RouteInfo

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MediaRouteControllerDialog(
    router: MediaRouter,
    modifier: Modifier = Modifier,
    volumeControlEnabled: Boolean = true,
    onDismissRequest: () -> Unit,
) {
    var mediaController by remember { mutableStateOf<MediaControllerCompat?>(null) }
    var playbackState by remember { mutableStateOf<PlaybackStateCompat?>(null) }
    var description by remember { mutableStateOf<MediaDescriptionCompat?>(null) }

    val context = LocalContext.current
    val mediaControllerCallback = remember {
        object : MediaControllerCompat.Callback() {
            override fun onSessionDestroyed() {
                mediaController?.unregisterCallback(this)
                mediaController = null
            }

            override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
                playbackState = state
            }

            override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
                description = metadata?.description
                // TODO updateArtIconIfNeeded()
            }
        }
    }

    DisposableEffect(router.mediaSessionToken) {
        val mediaSessionToken = router.mediaSessionToken
        if (mediaSessionToken != null) {
            mediaController = MediaControllerCompat(context, mediaSessionToken).apply {
                registerCallback(mediaControllerCallback)

                description = metadata?.description
                playbackState = this.playbackState
                // TODO updateArtIconIfNeeded()
            }
        }

        onDispose {
            mediaController?.unregisterCallback(mediaControllerCallback)
            mediaController = null
        }
    }

    ControllerDialog(
        router = router,
        volumeControlEnabled = volumeControlEnabled,
        playbackState = playbackState,
        mediaController = mediaController,
        description = description,
        modifier = modifier,
        onDismissRequest = onDismissRequest,
    )
}

@Composable
private fun ControllerDialog(
    router: MediaRouter,
    volumeControlEnabled: Boolean,
    playbackState: PlaybackStateCompat?,
    mediaController: MediaControllerCompat?,
    description: MediaDescriptionCompat?,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            AlertButton(
                label = stringResource(R.string.mr_controller_stop_casting),
                onClick = {
                    if (router.selectedRoute.isSelected) {
                        router.unselect(MediaRouter.UNSELECT_REASON_STOPPED)
                    }

                    onDismissRequest()
                },
            )
        },
        modifier = modifier,
        dismissButton = if (router.selectedRoute.canDisconnect()) {
            {
                AlertButton(
                    label = stringResource(R.string.mr_controller_disconnect),
                    onClick = {
                        if (router.selectedRoute.isSelected) {
                            router.unselect(MediaRouter.UNSELECT_REASON_DISCONNECTED)
                        }

                        onDismissRequest()
                    },
                )
            }
        } else {
            null
        },
        title = {
            Title(
                label = router.selectedRoute.name,
                modifier = Modifier.fillMaxWidth(),
                onDismissRequest = onDismissRequest,
            )
        },
        text = {
            Text(
                router = router,
                volumeControlEnabled = volumeControlEnabled,
                playbackState = playbackState,
                mediaController = mediaController,
                description = description,
                modifier = Modifier.fillMaxWidth(),
                onDismissRequest = onDismissRequest,
            )
        },
    )
}

@Composable
private fun AlertButton(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Text(text = label)
    }
}

@Composable
private fun Title(
    label: String,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
        )

        IconButton(onClick = onDismissRequest) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.mr_controller_close_description),
            )
        }
    }
}

@Composable
@Suppress("LongMethod")
private fun Text(
    router: MediaRouter,
    volumeControlEnabled: Boolean,
    playbackState: PlaybackStateCompat?,
    mediaController: MediaControllerCompat?,
    description: MediaDescriptionCompat?,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
) {
    // TODO Display mr_custom_control
    // TODO Display mr_art

    Column(
        modifier = modifier,
    ) {
        var isGroupExpanded by remember { mutableStateOf(false) }

        val hasPlaybackControls = description != null || playbackState != null
        val hasVolumeControls =
            volumeControlEnabled && router.selectedRoute.volumeHandling == RouteInfo.PLAYBACK_VOLUME_VARIABLE
        val selectedRoute = remember { router.selectedRoute }

        if (hasVolumeControls || hasPlaybackControls) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
            ) {
                if (hasPlaybackControls) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PlaybackControlsTitle(
                            route = selectedRoute,
                            playbackState = playbackState,
                            description = description,
                            modifier = Modifier.clickable {
                                mediaController?.let {
                                    val pendingIntent = it.sessionActivity
                                    if (pendingIntent != null) {
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
                        )

                        PlaybackControlsIcon(
                            playbackState = playbackState,
                        )
                    }
                }

                if (hasPlaybackControls && hasVolumeControls) {
                    HorizontalDivider()
                }

                if (hasVolumeControls) {
                    VolumeControls(
                        route = selectedRoute,
                        modifier = Modifier.fillMaxWidth(),
                        isExpanded = isGroupExpanded,
                        onExpandCollapseClick = { isGroupExpanded = !isGroupExpanded }
                    )
                }
            }
        }

        if (isGroupExpanded) {
            DeviceGroup(
                routes = selectedRoute.memberRoutes,
                volumeControlEnabled = volumeControlEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            )
        }
    }
}

@Composable
private fun PlaybackControlsTitle(
    route: RouteInfo,
    playbackState: PlaybackStateCompat?,
    description: MediaDescriptionCompat?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        var subtitle: CharSequence? = null
        val title = when {
            route.presentationDisplayId != RouteInfo.PRESENTATION_DISPLAY_ID_NONE -> stringResource(
                R.string.mr_controller_casting_screen
            )

            playbackState == null || playbackState.state == PlaybackStateCompat.STATE_NONE -> stringResource(
                R.string.mr_controller_no_media_selected
            )

            description?.title.isNullOrEmpty() && description?.subtitle.isNullOrEmpty() -> stringResource(
                R.string.mr_controller_no_info_available
            )

            else -> {
                subtitle = description.subtitle
                description.title
            }
        }

        if (!title.isNullOrEmpty()) {
            Text(
                text = title.toString(),
                maxLines = 1,
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        if (!subtitle.isNullOrEmpty()) {
            Text(
                text = subtitle.toString(),
                maxLines = 1,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun PlaybackControlsIcon(
    playbackState: PlaybackStateCompat?,
    modifier: Modifier = Modifier,
) {
    val icon: ImageVector?
    val contentDescriptionRes: Int
    val isPlaying = playbackState?.state == PlaybackStateCompat.STATE_BUFFERING
            || playbackState?.state == PlaybackStateCompat.STATE_PLAYING
    val isPauseActionSupported = (playbackState?.actions
        ?: 0L) and (ACTION_PAUSE or ACTION_PLAY_PAUSE) != 0L
    val isStopActionSupported =
        (playbackState?.actions ?: 0L) and ACTION_STOP != 0L
    val isPlayActionSupported = (playbackState?.actions
        ?: 0L) and (ACTION_PLAY or ACTION_PLAY_PAUSE) != 0L
    when {
        isPlaying && isPauseActionSupported -> {
            icon = Icons.Default.Pause
            contentDescriptionRes = R.string.mr_controller_pause
        }

        isPlaying && isStopActionSupported -> {
            icon = Icons.Default.Stop
            contentDescriptionRes = R.string.mr_controller_stop
        }

        !isPlaying && isPlayActionSupported -> {
            icon = Icons.Default.PlayArrow
            contentDescriptionRes = R.string.mr_controller_play
        }

        else -> {
            icon = null
            contentDescriptionRes = ResourcesCompat.ID_NULL
        }
    }

    if (icon != null) {
        Icon(
            imageVector = icon,
            contentDescription = stringResource(contentDescriptionRes),
            modifier = modifier,
        )
    }
}

@Composable
private fun VolumeControls(
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
            imageVector = Icons.Default.Audiotrack,
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
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = stringResource(
                        contentDescriptionRes
                    ),
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
            val isVolumeControlEnabled =
                volumeControlEnabled && route.volumeHandling == RouteInfo.PLAYBACK_VOLUME_VARIABLE
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
                        imageVector = Icons.Default.Audiotrack,
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
