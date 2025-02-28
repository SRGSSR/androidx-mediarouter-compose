/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.androidx.mediarouter.compose

import android.content.Context
import android.content.IntentFilter
import androidx.mediarouter.media.MediaControlIntent
import androidx.mediarouter.media.MediaRouteDescriptor
import androidx.mediarouter.media.MediaRouteProvider
import androidx.mediarouter.media.MediaRouteProviderDescriptor
import androidx.mediarouter.media.MediaRouter.RouteInfo

class TestMediaRouteProvider(context: Context) : MediaRouteProvider(context) {
    init {
        val intentFilterAudio = IntentFilter()
        intentFilterAudio.addCategory(MediaControlIntent.CATEGORY_LIVE_AUDIO)
        val intentFilterVideo = IntentFilter()
        intentFilterVideo.addCategory(MediaControlIntent.CATEGORY_LIVE_VIDEO)

        descriptor = MediaRouteProviderDescriptor.Builder()
            .addRoute(
                MediaRouteDescriptor.Builder("disconnected_route", "Disconnected route")
                    .setConnectionState(RouteInfo.CONNECTION_STATE_DISCONNECTED)
                    .addControlFilter(intentFilterVideo)
                    .build()
            )
            .addRoute(
                MediaRouteDescriptor.Builder("connecting_route", "Connecting route")
                    .setConnectionState(RouteInfo.CONNECTION_STATE_CONNECTING)
                    .addControlFilter(intentFilterAudio)
                    .build()
            )
            .addRoute(
                MediaRouteDescriptor.Builder("connected_route", "Connected route")
                    .setConnectionState(RouteInfo.CONNECTION_STATE_CONNECTED)
                    .addControlFilter(intentFilterVideo)
                    .build()
            )
            .addRoute(
                MediaRouteDescriptor.Builder("invalid_state_route", "Invalid state route")
                    .setConnectionState(Int.MAX_VALUE)
                    .addControlFilter(intentFilterAudio)
                    .build()
            )
            .addRoute(
                MediaRouteDescriptor.Builder("disabled_route", "Disabled route")
                    .setEnabled(false)
                    .addControlFilter(intentFilterVideo)
                    .build()
            )
            .addRoute(
                MediaRouteDescriptor.Builder(
                    "presentation_display_route",
                    "Presentation display route"
                )
                    .addControlFilter(intentFilterVideo)
                    .setPresentationDisplayId(42)
                    .build()
            )
            .build()
    }
}
