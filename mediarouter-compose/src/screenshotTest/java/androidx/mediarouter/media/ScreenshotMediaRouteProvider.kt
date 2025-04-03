/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package androidx.mediarouter.media

import android.content.Context

internal class ScreenshotMediaRouteProvider(context: Context) : MediaRouteProvider(context) {
    init {
        descriptor = MediaRouteProviderDescriptor.Builder()
            .addRoute(
                MediaRouteDescriptor.Builder("route1", "Living room TV")
                    .setDescription("Connected TV")
                    .setConnectionState(MediaRouter.RouteInfo.CONNECTION_STATE_CONNECTED)
                    .setCanDisconnect(true)
                    .setDeviceType(MediaRouter.RouteInfo.DEVICE_TYPE_TV)
                    .setVolume(75)
                    .setVolumeMax(100)
                    .build()
            )
            .addRoute(
                MediaRouteDescriptor.Builder("route2", "Bedroom speaker")
                    .setDescription("Connecting speaker")
                    .setConnectionState(MediaRouter.RouteInfo.CONNECTION_STATE_CONNECTING)
                    .setCanDisconnect(true)
                    .setDeviceType(MediaRouter.RouteInfo.DEVICE_TYPE_REMOTE_SPEAKER)
                    .setVolume(50)
                    .setVolumeMax(100)
                    .build()
            )
            .addRoute(
                MediaRouteDescriptor.Builder("route3", "Bluetooth headset")
                    .setDescription("Disconnected headset")
                    .setConnectionState(MediaRouter.RouteInfo.CONNECTION_STATE_DISCONNECTED)
                    .setCanDisconnect(false)
                    .setDeviceType(MediaRouter.RouteInfo.DEVICE_TYPE_BLE_HEADSET)
                    .setVolume(25)
                    .setVolumeMax(100)
                    .build()
            )
            .build()
    }
}
