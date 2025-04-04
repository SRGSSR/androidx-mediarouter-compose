/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.androidx.mediarouter.compose

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.mediarouter.media.ScreenshotMediaRouter
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler

class MediaRouteControllerDialogScreenshot {
    @Composable
    @PreviewLightDark
    private fun NoControlsPreview() {
        val router = ScreenshotMediaRouter(LocalContext.current)

        ScreenshotTheme {
            ControllerDialog(
                route = router.routes[0],
                volumeControlEnabled = false,
                imageModel = null,
                title = "Media title",
                subtitle = "Media subtitle",
                iconInfo = Icons.Default.PlayArrow to "Play",
                isDeviceGroupExpanded = false,
                showPlaybackControl = false,
                showVolumeControl = false,
                customControlView = null,
                toggleDeviceGroup = {},
                onKeyEvent = { false },
                onPlaybackTitleClick = {},
                onPlaybackIconClick = {},
                onStopCasting = {},
                onDisconnect = {},
                onDismissRequest = {},
            )
        }
    }

    @Composable
    @PreviewLightDark
    private fun PlaybackControlPreview() {
        val router = ScreenshotMediaRouter(LocalContext.current)

        ScreenshotTheme {
            ControllerDialog(
                route = router.routes[0],
                volumeControlEnabled = false,
                imageModel = null,
                title = "Media title",
                subtitle = "Media subtitle",
                iconInfo = Icons.Default.PlayArrow to "Play",
                isDeviceGroupExpanded = false,
                showPlaybackControl = true,
                showVolumeControl = false,
                customControlView = null,
                toggleDeviceGroup = {},
                onKeyEvent = { false },
                onPlaybackTitleClick = {},
                onPlaybackIconClick = {},
                onStopCasting = {},
                onDisconnect = {},
                onDismissRequest = {},
            )
        }
    }

    @Composable
    @PreviewLightDark
    private fun VolumeControlPreview() {
        val router = ScreenshotMediaRouter(LocalContext.current)

        ScreenshotTheme {
            ControllerDialog(
                route = router.routes[0],
                volumeControlEnabled = false,
                imageModel = null,
                title = "Media title",
                subtitle = "Media subtitle",
                iconInfo = Icons.Default.PlayArrow to "Play",
                isDeviceGroupExpanded = false,
                showPlaybackControl = false,
                showVolumeControl = true,
                customControlView = null,
                toggleDeviceGroup = {},
                onKeyEvent = { false },
                onPlaybackTitleClick = {},
                onPlaybackIconClick = {},
                onStopCasting = {},
                onDisconnect = {},
                onDismissRequest = {},
            )
        }
    }

    @Composable
    @PreviewLightDark
    private fun CustomControlViewPreview() {
        val router = ScreenshotMediaRouter(LocalContext.current)

        ScreenshotTheme {
            ControllerDialog(
                route = router.routes[0],
                volumeControlEnabled = false,
                imageModel = null,
                title = "Media title",
                subtitle = "Media subtitle",
                iconInfo = Icons.Default.PlayArrow to "Play",
                isDeviceGroupExpanded = false,
                showPlaybackControl = true,
                showVolumeControl = false,
                customControlView = {
                    Text(text = "Custom control view")
                },
                toggleDeviceGroup = {},
                onKeyEvent = { false },
                onPlaybackTitleClick = {},
                onPlaybackIconClick = {},
                onStopCasting = {},
                onDisconnect = {},
                onDismissRequest = {},
            )
        }
    }

    @Composable
    @PreviewLightDark
    private fun PlaybackControlAndVolumePreview() {
        val router = ScreenshotMediaRouter(LocalContext.current)

        ScreenshotTheme {
            ControllerDialog(
                route = router.routes[0],
                volumeControlEnabled = false,
                imageModel = null,
                title = "Media title",
                subtitle = "Media subtitle",
                iconInfo = Icons.Default.PlayArrow to "Play",
                isDeviceGroupExpanded = false,
                showPlaybackControl = true,
                showVolumeControl = true,
                customControlView = null,
                toggleDeviceGroup = {},
                onKeyEvent = { false },
                onPlaybackTitleClick = {},
                onPlaybackIconClick = {},
                onStopCasting = {},
                onDisconnect = {},
                onDismissRequest = {},
            )
        }
    }

    @Composable
    @PreviewLightDark
    private fun CustomControlViewAndVolumeControlPreview() {
        val router = ScreenshotMediaRouter(LocalContext.current)

        ScreenshotTheme {
            ControllerDialog(
                route = router.routes[0],
                volumeControlEnabled = false,
                imageModel = null,
                title = "Media title",
                subtitle = "Media subtitle",
                iconInfo = Icons.Default.PlayArrow to "Play",
                isDeviceGroupExpanded = false,
                showPlaybackControl = true,
                showVolumeControl = true,
                customControlView = {
                    Text(text = "Custom control view")
                },
                toggleDeviceGroup = {},
                onKeyEvent = { false },
                onPlaybackTitleClick = {},
                onPlaybackIconClick = {},
                onStopCasting = {},
                onDisconnect = {},
                onDismissRequest = {},
            )
        }
    }

    @Composable
    @PreviewLightDark
    @OptIn(ExperimentalCoilApi::class)
    private fun ImageUriPreview() {
        val context = LocalContext.current
        val router = ScreenshotMediaRouter(context)

        ScreenshotTheme {
            val imageColor = MaterialTheme.colorScheme.onSurface
            val imagePreviewHandler = AsyncImagePreviewHandler {
                ColorImage(color = imageColor.toArgb(), width = 600, height = 1200)
            }

            CompositionLocalProvider(LocalAsyncImagePreviewHandler provides imagePreviewHandler) {
                ControllerDialog(
                    route = router.routes[0],
                    volumeControlEnabled = false,
                    imageModel = "https://image.url/",
                    title = "Media title",
                    subtitle = "Media subtitle",
                    iconInfo = Icons.Default.PlayArrow to "Play",
                    isDeviceGroupExpanded = false,
                    showPlaybackControl = false,
                    showVolumeControl = false,
                    customControlView = null,
                    toggleDeviceGroup = {},
                    onKeyEvent = { false },
                    onPlaybackTitleClick = {},
                    onPlaybackIconClick = {},
                    onStopCasting = {},
                    onDisconnect = {},
                    onDismissRequest = {},
                )
            }
        }
    }
}
