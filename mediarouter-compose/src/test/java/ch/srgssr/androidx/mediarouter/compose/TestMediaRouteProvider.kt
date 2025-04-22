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

internal class TestMediaRouteProvider(context: Context) : MediaRouteProvider(context) {
    init {
        val intentFilterAudio = IntentFilter()
        intentFilterAudio.addCategory(MediaControlIntent.CATEGORY_LIVE_AUDIO)
        val intentFilterVideo = IntentFilter()
        intentFilterVideo.addCategory(MediaControlIntent.CATEGORY_LIVE_VIDEO)

        descriptor = MediaRouteProviderDescriptor.Builder()
            .addRoute(
                MediaRouteDescriptor.Builder(ROUTE_ID_DISCONNECTED, ROUTE_NAME_DISCONNECTED)
                    .setConnectionState(RouteInfo.CONNECTION_STATE_DISCONNECTED)
                    .addControlFilter(intentFilterVideo)
                    .build()
            )
            .addRoute(
                MediaRouteDescriptor.Builder(ROUTE_ID_CONNECTING, ROUTE_NAME_CONNECTING)
                    .setConnectionState(RouteInfo.CONNECTION_STATE_CONNECTING)
                    .addControlFilter(intentFilterAudio)
                    .build()
            )
            .addRoute(
                MediaRouteDescriptor.Builder(ROUTE_ID_CONNECTED, ROUTE_NAME_CONNECTED)
                    .setConnectionState(RouteInfo.CONNECTION_STATE_CONNECTED)
                    .addControlFilter(intentFilterVideo)
                    .build()
            )
            .addRoute(
                MediaRouteDescriptor.Builder(ROUTE_ID_INVALID, ROUTE_NAME_INVALID)
                    .setConnectionState(Int.MAX_VALUE)
                    .addControlFilter(intentFilterAudio)
                    .build()
            )
            .addRoute(
                MediaRouteDescriptor.Builder(ROUTE_ID_DISABLED, ROUTE_NAME_DISABLED)
                    .setEnabled(false)
                    .addControlFilter(intentFilterVideo)
                    .build()
            )
            .addRoute(
                MediaRouteDescriptor.Builder(ROUTE_ID_PRESENTATION, ROUTE_NAME_PRESENTATION)
                    .addControlFilter(intentFilterVideo)
                    .setPresentationDisplayId(42)
                    .build()
            )
            .build()
    }

    companion object {
        // Route ids
        const val ROUTE_ID_CONNECTED = "connected_route"
        const val ROUTE_ID_CONNECTING = "connecting_route"
        const val ROUTE_ID_DISABLED = "disabled_route"
        const val ROUTE_ID_DISCONNECTED = "disconnected_route"
        const val ROUTE_ID_INVALID = "invalid_state_route"
        const val ROUTE_ID_PRESENTATION = "presentation_display_route"

        // Route names
        const val DEFAULT_ROUTE_NAME = "Phone"
        const val ROUTE_NAME_CONNECTED = "Connected route"
        const val ROUTE_NAME_CONNECTING = "Connecting route"
        const val ROUTE_NAME_DISABLED = "Disabled route"
        const val ROUTE_NAME_DISCONNECTED = "Disconnected route"
        const val ROUTE_NAME_INVALID = "Invalid state route"
        const val ROUTE_NAME_PRESENTATION = "Presentation display route"
    }
}
