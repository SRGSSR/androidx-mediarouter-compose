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
                fixedIcon = false,
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
                fixedIcon = false,
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
                fixedIcon = false,
                colors = IconButtonDefaults.iconButtonColors(),
                onClick = {},
            )
        }
    }

    @Composable
    @PreviewLightDark
    private fun DisconnectedFixedIconPreview() {
        ScreenshotTheme {
            MediaRouteButton(
                state = CastConnectionState.Disconnected,
                fixedIcon = true,
                colors = IconButtonDefaults.iconButtonColors(),
                onClick = {},
            )
        }
    }

    @Composable
    @PreviewLightDark
    private fun ConnectingFixedIconPreview() {
        ScreenshotTheme {
            MediaRouteButton(
                state = CastConnectionState.Connecting,
                fixedIcon = true,
                colors = IconButtonDefaults.iconButtonColors(),
                onClick = {},
            )
        }
    }

    @Composable
    @PreviewLightDark
    private fun ConnectedFixedIconPreview() {
        ScreenshotTheme {
            MediaRouteButton(
                state = CastConnectionState.Connected,
                fixedIcon = true,
                colors = IconButtonDefaults.iconButtonColors(),
                onClick = {},
            )
        }
    }
}
