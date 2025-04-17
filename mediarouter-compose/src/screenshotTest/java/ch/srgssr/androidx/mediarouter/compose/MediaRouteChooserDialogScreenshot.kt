/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.androidx.mediarouter.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.mediarouter.media.ScreenshotMediaRouter
import ch.srgssr.androidx.mediarouter.compose.MediaRouteChooserDialogViewModel.ChooserState

class MediaRouteChooserDialogScreenshot {
    @Composable
    @PreviewLightDark
    private fun FindingDevicesPreview() {
        ScreenshotTheme {
            ChooserDialog(
                routes = emptyList(),
                state = ChooserState.FindingDevices,
                title = null,
                onDismissRequest = {},
            )
        }
    }

    @Composable
    @PreviewLightDark
    private fun NoDevicesNoWifiHintPreview() {
        ScreenshotTheme {
            ChooserDialog(
                routes = emptyList(),
                state = ChooserState.NoDevicesNoWifiHint,
                title = null,
                onDismissRequest = {},
            )
        }
    }

    @Composable
    @PreviewLightDark
    private fun NoRoutesPreview() {
        ScreenshotTheme {
            ChooserDialog(
                routes = emptyList(),
                state = ChooserState.NoRoutes,
                title = null,
                onDismissRequest = {},
            )
        }
    }

    @Composable
    @PreviewLightDark
    private fun ShowingRoutesPreview() {
        val router = ScreenshotMediaRouter(LocalContext.current)

        ScreenshotTheme {
            ChooserDialog(
                routes = router.routes,
                state = ChooserState.ShowingRoutes,
                title = null,
                onDismissRequest = {},
            )
        }
    }

    @Composable
    @PreviewLightDark
    private fun FindingDevicesCustomTitlePreview() {
        ScreenshotTheme {
            ChooserDialog(
                routes = emptyList(),
                state = ChooserState.FindingDevices,
                title = "Custom title",
                onDismissRequest = {},
            )
        }
    }

    @Composable
    @PreviewLightDark
    private fun NoDevicesNoWifiHintCustomTitlePreview() {
        ScreenshotTheme {
            ChooserDialog(
                routes = emptyList(),
                state = ChooserState.NoDevicesNoWifiHint,
                title = "Custom title",
                onDismissRequest = {},
            )
        }
    }

    @Composable
    @PreviewLightDark
    private fun NoRoutesCustomTitlePreview() {
        ScreenshotTheme {
            ChooserDialog(
                routes = emptyList(),
                state = ChooserState.NoRoutes,
                title = "Custom title",
                onDismissRequest = {},
            )
        }
    }

    @Composable
    @PreviewLightDark
    private fun ShowingRoutesCustomTitlePreview() {
        val router = ScreenshotMediaRouter(LocalContext.current)

        ScreenshotTheme {
            ChooserDialog(
                routes = router.routes,
                state = ChooserState.ShowingRoutes,
                title = "Custom title",
                onDismissRequest = {},
            )
        }
    }
}
