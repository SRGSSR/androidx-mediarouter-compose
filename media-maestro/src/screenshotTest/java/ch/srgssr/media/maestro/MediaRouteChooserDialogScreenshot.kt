/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.media.maestro

import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
                shape = AlertDialogDefaults.shape,
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
                shape = AlertDialogDefaults.shape,
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
                shape = AlertDialogDefaults.shape,
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
                shape = AlertDialogDefaults.shape,
                onDismissRequest = {},
            )
        }
    }

    @Composable
    @ScreenshotPreviews
    private fun FindingDevicesCustomStylePreview() {
        ScreenshotTheme {
            ChooserDialog(
                routes = emptyList(),
                state = ChooserState.FindingDevices,
                title = "Custom title",
                shape = CutCornerShape(32.dp),
                onDismissRequest = {},
            )
        }
    }

    @Composable
    @ScreenshotPreviews
    private fun NoDevicesNoWifiHintCustomStylePreview() {
        ScreenshotTheme {
            ChooserDialog(
                routes = emptyList(),
                state = ChooserState.NoDevicesNoWifiHint,
                title = "Custom title",
                shape = CutCornerShape(32.dp),
                onDismissRequest = {},
            )
        }
    }

    @Composable
    @ScreenshotPreviews
    private fun NoRoutesCustomStylePreview() {
        ScreenshotTheme {
            ChooserDialog(
                routes = emptyList(),
                state = ChooserState.NoRoutes,
                title = "Custom title",
                shape = CutCornerShape(32.dp),
                onDismissRequest = {},
            )
        }
    }

    @Composable
    @ScreenshotPreviews
    private fun ShowingRoutesCustomStylePreview() {
        val router = ScreenshotMediaRouter(LocalContext.current)

        ScreenshotTheme {
            ChooserDialog(
                routes = router.routes,
                state = ChooserState.ShowingRoutes,
                title = "Custom title",
                shape = CutCornerShape(32.dp),
                onDismissRequest = {},
            )
        }
    }
}
