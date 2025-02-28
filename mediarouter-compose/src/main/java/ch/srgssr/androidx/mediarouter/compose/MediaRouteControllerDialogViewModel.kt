/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.androidx.mediarouter.compose

import android.app.Application
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
import androidx.annotation.VisibleForTesting
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.mediarouter.R
import androidx.mediarouter.media.MediaRouteSelector
import androidx.mediarouter.media.MediaRouter
import androidx.mediarouter.media.MediaRouter.RouteInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

internal class MediaRouteControllerDialogViewModel(
    private val application: Application,
    private val savedStateHandle: SavedStateHandle,
    private val volumeControlEnabled: Boolean,
) : AndroidViewModel(application) {
    private var mediaController: MediaControllerCompat? = null

    private val mediaControllerCallback = MediaControllerCallback()
    private val mediaRouterCallback = MediaRouterCallback()
    private val router = MediaRouter.getInstance(application)
    private val isGroupVolumeUxEnabled = MediaRouter.isGroupVolumeUxEnabled()

    private val routerUpdates = MutableStateFlow(0)

    @VisibleForTesting
    internal val mediaDescription = MutableStateFlow<MediaDescriptionCompat?>(null)

    @VisibleForTesting
    internal val playbackState = MutableStateFlow<PlaybackStateCompat?>(null)
    private val _isDeviceGroupExpanded = MutableStateFlow(false)
    private val _selectedRoute = routerUpdates.map { router.selectedRoute }

    val showDialog = savedStateHandle.getStateFlow(KEY_SHOW_DIALOG, true)
    val selectedRoute = _selectedRoute
        .stateIn(viewModelScope, WhileSubscribed(), router.selectedRoute)
    val isDeviceGroupExpanded: StateFlow<Boolean> = _isDeviceGroupExpanded.asStateFlow()

    val showPlaybackControl =
        combine(mediaDescription, playbackState) { mediaDescription, playbackState ->
            mediaDescription != null || playbackState != null
        }.stateIn(viewModelScope, WhileSubscribed(), false)
    val showVolumeControl = _selectedRoute.map { selectedRoute ->
        selectedRoute.volumeHandling == RouteInfo.PLAYBACK_VOLUME_VARIABLE
                && (isGroupVolumeUxEnabled || !(selectedRoute.isGroup && selectedRoute.memberRoutes.size > 1))
                && volumeControlEnabled
    }.stateIn(viewModelScope, WhileSubscribed(), false)
    val imageModel = mediaDescription.map { mediaDescription ->
        mediaDescription?.iconBitmap?.takeIf { !it.isRecycled } ?: mediaDescription?.iconUri
    }.stateIn(viewModelScope, WhileSubscribed(), null)

    val title = combine(
        mediaDescription,
        playbackState,
        _selectedRoute,
    ) { mediaDescription, playbackState, selectedRoute ->
        if (selectedRoute.presentationDisplayId != RouteInfo.PRESENTATION_DISPLAY_ID_NONE) {
            application.getString(R.string.mr_controller_casting_screen)
        } else if (playbackState == null || playbackState.state == PlaybackStateCompat.STATE_NONE) {
            application.getString(R.string.mr_controller_no_media_selected)
        } else if (mediaDescription?.title.isNullOrEmpty() && mediaDescription?.subtitle.isNullOrEmpty()) {
            application.getString(R.string.mr_controller_no_info_available)
        } else {
            mediaDescription?.title?.toString()
        }
    }.stateIn(viewModelScope, WhileSubscribed(), null)
    val subtitle = mediaDescription.map { it?.subtitle?.toString() }
        .stateIn(viewModelScope, WhileSubscribed(), null)
    val iconInfo = playbackState.map { playbackState ->
        if (playbackState == null) {
            return@map null
        }

        val isPlaying = playbackState.state == PlaybackStateCompat.STATE_BUFFERING
                || playbackState.state == PlaybackStateCompat.STATE_PLAYING

        val icon: ImageVector
        val contentDescription: String
        if (isPlaying && playbackState.isPauseActionSupported) {
            icon = Icons.Pause
            contentDescription = application.getString(R.string.mr_controller_pause)
        } else if (isPlaying && playbackState.isStopActionSupported) {
            icon = Icons.Stop
            contentDescription = application.getString(R.string.mr_controller_stop)
        } else if (!isPlaying && playbackState.isPlayActionSupported) {
            icon = Icons.PlayArrow
            contentDescription = application.getString(R.string.mr_controller_play)
        } else {
            return@map null
        }

        icon to contentDescription
    }.stateIn(viewModelScope, WhileSubscribed(), null)

    init {
        router.addCallback(
            MediaRouteSelector.EMPTY,
            mediaRouterCallback,
            MediaRouter.CALLBACK_FLAG_UNFILTERED_EVENTS,
        )

        router.mediaSessionToken?.let { mediaSessionToken ->
            val mediaController = MediaControllerCompat(application, mediaSessionToken)
            mediaController.registerCallback(mediaControllerCallback)

            this.mediaController = mediaController
            mediaDescription.update { mediaController.metadata?.description }
            playbackState.update { mediaController.playbackState }
        }
    }

    fun hideDialog() {
        savedStateHandle[KEY_SHOW_DIALOG] = false
    }

    fun toggleDeviceGroup() {
        _isDeviceGroupExpanded.update { !it }
    }

    fun stopCasting() {
        if (selectedRoute.value.isSelected) {
            router.unselect(MediaRouter.UNSELECT_REASON_STOPPED)
        }

        hideDialog()
    }

    fun disconnect() {
        if (selectedRoute.value.isSelected) {
            router.unselect(MediaRouter.UNSELECT_REASON_DISCONNECTED)
        }

        hideDialog()
    }

    fun onKeyEvent(keyEvent: KeyEvent): Boolean {
        return if (keyEvent.key == Key.VolumeDown || keyEvent.key == Key.VolumeUp) {
            val isDeviceGroupExpanded = _isDeviceGroupExpanded.value
            if (keyEvent.type == KeyEventType.KeyDown && (isGroupVolumeUxEnabled || !isDeviceGroupExpanded)) {
                val delta = if (keyEvent.key == Key.VolumeDown) -1 else 1

                selectedRoute.value.requestUpdateVolume(delta)
            }

            true
        } else {
            false
        }
    }

    fun startSessionActivity() {
        mediaController?.sessionActivity?.let { pendingIntent ->
            try {
                pendingIntent.send()
                hideDialog()
            } catch (exception: PendingIntent.CanceledException) {
                Log.d(
                    "MediaRouteController",
                    "$pendingIntent was not sent, it has been canceled.",
                    exception,
                )
            }
        }
    }

    fun onPlaybackIconClick() {
        val mediaController = mediaController ?: return
        val playbackState = playbackState.value ?: return
        val isPlaying = playbackState.state == PlaybackStateCompat.STATE_PLAYING

        if (isPlaying && playbackState.isPauseActionSupported) {
            mediaController.transportControls.pause()
        } else if (isPlaying && playbackState.isStopActionSupported) {
            mediaController.transportControls.stop()
        } else if (!isPlaying && playbackState.isPlayActionSupported) {
            mediaController.transportControls.play()
        }
    }

    override fun onCleared() {
        router.removeCallback(mediaRouterCallback)
        mediaControllerCallback.onSessionDestroyed()
    }

    private val PlaybackStateCompat.isPlayActionSupported: Boolean
        get() = actions and (ACTION_PLAY or ACTION_PLAY_PAUSE) != 0L

    private val PlaybackStateCompat.isPauseActionSupported: Boolean
        get() = actions and (ACTION_PAUSE or ACTION_PLAY_PAUSE) != 0L

    private val PlaybackStateCompat.isStopActionSupported: Boolean
        get() = actions and ACTION_STOP != 0L

    private companion object {
        private const val KEY_SHOW_DIALOG = "showDialog"
    }

    class Factory(private val volumeControlEnabled: Boolean) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            val application = checkNotNull(extras[APPLICATION_KEY])
            val savedStateHandle = extras.createSavedStateHandle()

            @Suppress("UNCHECKED_CAST")
            return MediaRouteControllerDialogViewModel(
                application = application,
                savedStateHandle = savedStateHandle,
                volumeControlEnabled = volumeControlEnabled,
            ) as T
        }
    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {
        override fun onSessionDestroyed() {
            mediaController?.unregisterCallback(this)
            mediaController = null
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            playbackState.update { state }
            routerUpdates.update { it + 1 }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            mediaDescription.update { metadata?.description }
            routerUpdates.update { it + 1 }
        }
    }

    private inner class MediaRouterCallback : MediaRouter.Callback() {
        override fun onRouteUnselected(router: MediaRouter, route: RouteInfo, reason: Int) {
            routerUpdates.update { it + 1 }
        }

        override fun onRouteChanged(router: MediaRouter, route: RouteInfo) {
            routerUpdates.update { it + 1 }
        }

        override fun onRouteVolumeChanged(router: MediaRouter, route: RouteInfo) {
            routerUpdates.update { it + 1 }
        }
    }
}
