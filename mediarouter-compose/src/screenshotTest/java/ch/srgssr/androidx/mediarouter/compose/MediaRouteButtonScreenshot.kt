/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.androidx.mediarouter.compose

import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark

class MediaRouteButtonScreenshot {
    @Composable
    @PreviewLightDark
    private fun DisconnectedPreview() {
        ScreenshotTheme {
            MediaRouteButton(
                state = CastConnectionState.Disconnected,
                colors = IconButtonDefaults.iconButtonColors(),
                onClick = {},
            )
        }
    }

    @Composable
    @PreviewLightDark
    private fun ConnectingPreview() {
        ScreenshotTheme {
            MediaRouteButton(
                state = CastConnectionState.Connecting,
                colors = IconButtonDefaults.iconButtonColors(),
                onClick = {},
            )
        }
    }

    @Composable
    @PreviewLightDark
    private fun ConnectedPreview() {
        ScreenshotTheme {
            MediaRouteButton(
                state = CastConnectionState.Connected,
                colors = IconButtonDefaults.iconButtonColors(),
                onClick = {},
            )
        }
    }
}
