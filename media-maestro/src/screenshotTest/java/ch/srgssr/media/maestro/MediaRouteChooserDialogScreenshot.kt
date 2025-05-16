/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.media.maestro

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.mediarouter.media.ScreenshotMediaRouter
import ch.srgssr.media.maestro.MediaRouteChooserDialogViewModel.ChooserState

class MediaRouteChooserDialogScreenshot {
    @Composable
    @ScreenshotPreviews
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
    @ScreenshotPreviews
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
    @ScreenshotPreviews
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
    @ScreenshotPreviews
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
    @ScreenshotPreviews
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
    @ScreenshotPreviews
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
    @ScreenshotPreviews
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
    @ScreenshotPreviews
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
