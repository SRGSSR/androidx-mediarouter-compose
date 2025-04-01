/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.androidx.mediarouter.compose.demo

import android.app.Application
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.cast.CastPlayer
import androidx.media3.cast.SessionAvailabilityListener
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.google.android.gms.cast.framework.CastContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlin.time.Duration.Companion.seconds

@OptIn(UnstableApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val localPlayer = ExoPlayer.Builder(application).build()
    private val castPlayer = CastPlayer(CastContext.getSharedInstance(application))
    private val currentPlayer =
        MutableStateFlow(if (castPlayer.isCastSessionAvailable) castPlayer else localPlayer)

    val player = currentPlayer
        .onEach { player ->
            val oldPlayer = if (player == localPlayer) castPlayer else localPlayer
            player.volume = oldPlayer.volume
            player.repeatMode = oldPlayer.repeatMode
            player.playWhenReady = oldPlayer.playWhenReady

            oldPlayer.currentMediaItem?.let {
                player.setMediaItem(it, oldPlayer.currentPosition)
            }
            oldPlayer.stop()
        }
        .stateIn(
            scope = viewModelScope,
            started = WhileSubscribed(5.seconds.inWholeMilliseconds),
            initialValue = currentPlayer.value,
        )

    init {
        localPlayer.setMediaItem(mediaItem)
        localPlayer.volume = 0f
        localPlayer.prepare()
        localPlayer.play()

        castPlayer.setSessionAvailabilityListener(object : SessionAvailabilityListener {
            override fun onCastSessionAvailable() {
                currentPlayer.update { castPlayer }
            }

            override fun onCastSessionUnavailable() {
                currentPlayer.update { localPlayer }
            }
        })
    }

    override fun onCleared() {
        localPlayer.release()
        castPlayer.setSessionAvailabilityListener(null)
        castPlayer.release()
    }

    private companion object {
        @Suppress("MaxLineLength")
        private val mediaItem = MediaItem.Builder()
            .setUri("https://storage.googleapis.com/wvmedia/clear/h264/tears/tears.mpd")
            .setMimeType(MimeTypes.APPLICATION_MPD)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle("Tears of Steel (DASH, adaptive, HD, MP4, H264/aac)")
                    .setArtworkUri("https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg".toUri())
                    .build()
            )
            .build()
    }
}
