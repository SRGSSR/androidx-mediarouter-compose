/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.androidx.mediarouter.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.mediarouter.R
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
                title = stringResource(R.string.mr_chooser_title),
                confirmButtonLabel = null,
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
                title = stringResource(R.string.mr_chooser_title),
                confirmButtonLabel = null,
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
                title = stringResource(R.string.mr_chooser_zero_routes_found_title),
                confirmButtonLabel = stringResource(android.R.string.ok),
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
                title = stringResource(R.string.mr_chooser_title),
                confirmButtonLabel = null,
                onDismissRequest = {},
            )
        }
    }
}
