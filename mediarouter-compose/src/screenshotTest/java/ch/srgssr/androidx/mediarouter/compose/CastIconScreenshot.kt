/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.androidx.mediarouter.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp

class CastIconScreenshot {
    @Composable
    @PreviewLightDark
    private fun DisconnectedPreview() {
        ScreenshotTheme {
            CastIcon(
                state = CastConnectionState.Disconnected,
                contentDescription = null,
                size = 72.dp,
            )
        }
    }

    @Composable
    @PreviewLightDark
    private fun ConnectingPreview() {
        ScreenshotTheme {
            CastIcon(
                state = CastConnectionState.Connecting,
                contentDescription = null,
                size = 72.dp,
            )
        }
    }

    @Composable
    @PreviewLightDark
    private fun ConnectedPreview() {
        ScreenshotTheme {
            CastIcon(
                state = CastConnectionState.Connected,
                contentDescription = null,
                size = 72.dp,
            )
        }
    }
}
