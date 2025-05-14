/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.media.maestro

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.annotation.ColorInt
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.core.graphics.createBitmap
import androidx.mediarouter.media.MediaRouter
import androidx.mediarouter.media.ScreenshotMediaRouter
import ch.srgssr.media.maestro.MediaRouteControllerDialogViewModel.RouteDetail
import coil3.Image
import coil3.annotation.ExperimentalCoilApi
import coil3.asImage
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler

class MediaRouteControllerDialogScreenshot {
    @Composable
    @PreviewLightDark
    private fun NoControlsPreview() {
        val router = ScreenshotMediaRouter(LocalContext.current)

        ScreenshotTheme {
            ControllerDialog(
                routeDetail = routeDetail(router.routes[0]),
                imageModel = null,
                title = "Media title",
                subtitle = "Media subtitle",
                iconInfo = Icons.Default.PlayArrow to "Play",
                isDeviceGroupExpanded = false,
                showPlaybackControl = false,
                showVolumeControl = false,
                groupRouteDetails = emptyList(),
                customControlView = null,
                toggleDeviceGroup = {},
                onKeyEvent = { false },
                onPlaybackTitleClick = {},
                onPlaybackIconClick = {},
                onStopCasting = {},
                onDisconnect = {},
                onDismissRequest = {},
                onVolumeChange = { _, _ -> },
            )
        }
    }

    @Composable
    @PreviewLightDark
    private fun PlaybackControlPreview() {
        val router = ScreenshotMediaRouter(LocalContext.current)

        ScreenshotTheme {
            ControllerDialog(
                routeDetail = routeDetail(router.routes[0]),
                imageModel = null,
                title = "Media title",
                subtitle = "Media subtitle",
                iconInfo = Icons.Default.PlayArrow to "Play",
                isDeviceGroupExpanded = false,
                showPlaybackControl = true,
                showVolumeControl = false,
                groupRouteDetails = emptyList(),
                customControlView = null,
                toggleDeviceGroup = {},
                onKeyEvent = { false },
                onPlaybackTitleClick = {},
                onPlaybackIconClick = {},
                onStopCasting = {},
                onDisconnect = {},
                onDismissRequest = {},
                onVolumeChange = { _, _ -> },
            )
        }
    }

    @Composable
    @PreviewLightDark
    private fun VolumeControlPreview() {
        val router = ScreenshotMediaRouter(LocalContext.current)

        ScreenshotTheme {
            ControllerDialog(
                routeDetail = routeDetail(router.routes[0]),
                imageModel = null,
                title = "Media title",
                subtitle = "Media subtitle",
                iconInfo = Icons.Default.PlayArrow to "Play",
                isDeviceGroupExpanded = false,
                showPlaybackControl = false,
                showVolumeControl = true,
                groupRouteDetails = emptyList(),
                customControlView = null,
                toggleDeviceGroup = {},
                onKeyEvent = { false },
                onPlaybackTitleClick = {},
                onPlaybackIconClick = {},
                onStopCasting = {},
                onDisconnect = {},
                onDismissRequest = {},
                onVolumeChange = { _, _ -> },
            )
        }
    }

    @Composable
    @PreviewLightDark
    private fun CustomControlViewPreview() {
        val router = ScreenshotMediaRouter(LocalContext.current)

        ScreenshotTheme {
            ControllerDialog(
                routeDetail = routeDetail(router.routes[0]),
                imageModel = null,
                title = "Media title",
                subtitle = "Media subtitle",
                iconInfo = Icons.Default.PlayArrow to "Play",
                isDeviceGroupExpanded = false,
                showPlaybackControl = true,
                showVolumeControl = false,
                groupRouteDetails = emptyList(),
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
                onVolumeChange = { _, _ -> },
            )
        }
    }

    @Composable
    @PreviewLightDark
    private fun PlaybackControlAndVolumePreview() {
        val router = ScreenshotMediaRouter(LocalContext.current)

        ScreenshotTheme {
            ControllerDialog(
                routeDetail = routeDetail(router.routes[0]),
                imageModel = null,
                title = "Media title",
                subtitle = "Media subtitle",
                iconInfo = Icons.Default.PlayArrow to "Play",
                isDeviceGroupExpanded = false,
                showPlaybackControl = true,
                showVolumeControl = true,
                groupRouteDetails = emptyList(),
                customControlView = null,
                toggleDeviceGroup = {},
                onKeyEvent = { false },
                onPlaybackTitleClick = {},
                onPlaybackIconClick = {},
                onStopCasting = {},
                onDisconnect = {},
                onDismissRequest = {},
                onVolumeChange = { _, _ -> },
            )
        }
    }

    @Composable
    @PreviewLightDark
    private fun CustomControlViewAndVolumeControlPreview() {
        val router = ScreenshotMediaRouter(LocalContext.current)

        ScreenshotTheme {
            ControllerDialog(
                routeDetail = routeDetail(router.routes[0]),
                imageModel = null,
                title = "Media title",
                subtitle = "Media subtitle",
                iconInfo = Icons.Default.PlayArrow to "Play",
                isDeviceGroupExpanded = false,
                showPlaybackControl = true,
                showVolumeControl = true,
                groupRouteDetails = emptyList(),
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
                onVolumeChange = { _, _ -> },
            )
        }
    }

    @Composable
    @PreviewLightDark
    @OptIn(ExperimentalCoilApi::class)
    private fun ImageUriLandscapePreview() {
        val router = ScreenshotMediaRouter(LocalContext.current)

        ScreenshotTheme {
            val imageColor = MaterialTheme.colorScheme.onSurface
            val imagePreviewHandler = AsyncImagePreviewHandler {
                createImage(imageColor.toArgb(), width = IMAGE_SIZE, height = IMAGE_SIZE * 9 / 16)
            }

            CompositionLocalProvider(LocalAsyncImagePreviewHandler provides imagePreviewHandler) {
                ControllerDialog(
                    routeDetail = routeDetail(router.routes[0]),
                    imageModel = "https://image.url/",
                    title = "Media title",
                    subtitle = "Media subtitle",
                    iconInfo = Icons.Default.PlayArrow to "Play",
                    isDeviceGroupExpanded = false,
                    showPlaybackControl = true,
                    showVolumeControl = true,
                    groupRouteDetails = emptyList(),
                    customControlView = null,
                    toggleDeviceGroup = {},
                    onKeyEvent = { false },
                    onPlaybackTitleClick = {},
                    onPlaybackIconClick = {},
                    onStopCasting = {},
                    onDisconnect = {},
                    onDismissRequest = {},
                    onVolumeChange = { _, _ -> },
                )
            }
        }
    }

    @Composable
    @PreviewLightDark
    @OptIn(ExperimentalCoilApi::class)
    private fun ImageUriSquarePreview() {
        val router = ScreenshotMediaRouter(LocalContext.current)

        ScreenshotTheme {
            val imageColor = MaterialTheme.colorScheme.onSurface
            val imagePreviewHandler = AsyncImagePreviewHandler {
                createImage(imageColor.toArgb(), width = IMAGE_SIZE, height = IMAGE_SIZE)
            }

            CompositionLocalProvider(LocalAsyncImagePreviewHandler provides imagePreviewHandler) {
                ControllerDialog(
                    routeDetail = routeDetail(router.routes[0]),
                    imageModel = "https://image.url/",
                    title = "Media title",
                    subtitle = "Media subtitle",
                    iconInfo = Icons.Default.PlayArrow to "Play",
                    isDeviceGroupExpanded = false,
                    showPlaybackControl = true,
                    showVolumeControl = true,
                    groupRouteDetails = emptyList(),
                    customControlView = null,
                    toggleDeviceGroup = {},
                    onKeyEvent = { false },
                    onPlaybackTitleClick = {},
                    onPlaybackIconClick = {},
                    onStopCasting = {},
                    onDisconnect = {},
                    onDismissRequest = {},
                    onVolumeChange = { _, _ -> },
                )
            }
        }
    }

    @Composable
    @PreviewLightDark
    @OptIn(ExperimentalCoilApi::class)
    private fun ImageUriPortraitPreview() {
        val router = ScreenshotMediaRouter(LocalContext.current)

        ScreenshotTheme {
            val imageColor = MaterialTheme.colorScheme.onSurface
            val imagePreviewHandler = AsyncImagePreviewHandler {
                createImage(
                    imageColor.toArgb(),
                    width = IMAGE_SIZE,
                    height = IMAGE_SIZE * 4 / 3
                )
            }

            CompositionLocalProvider(LocalAsyncImagePreviewHandler provides imagePreviewHandler) {
                ControllerDialog(
                    routeDetail = routeDetail(router.routes[0]),
                    imageModel = "https://image.url/",
                    title = "Media title",
                    subtitle = "Media subtitle",
                    iconInfo = Icons.Default.PlayArrow to "Play",
                    isDeviceGroupExpanded = false,
                    showPlaybackControl = true,
                    showVolumeControl = true,
                    groupRouteDetails = emptyList(),
                    customControlView = null,
                    toggleDeviceGroup = {},
                    onKeyEvent = { false },
                    onPlaybackTitleClick = {},
                    onPlaybackIconClick = {},
                    onStopCasting = {},
                    onDisconnect = {},
                    onDismissRequest = {},
                    onVolumeChange = { _, _ -> },
                )
            }
        }
    }

    private fun createImage(@ColorInt color: Int, width: Int, height: Int): Image {
        val paint = Paint()
        paint.color = Color.RED
        paint.strokeWidth = 2f
        paint.style = Paint.Style.STROKE

        val widthFloat = width.toFloat()
        val heightFloat = height.toFloat()

        val bitmap = createBitmap(width, height)
        Canvas(bitmap).apply {
            drawColor(color)
            drawRect(1f, 1f, widthFloat - 2f, heightFloat - 2f, paint)
            drawLine(0f, 0f, widthFloat, heightFloat, paint)
            drawLine(widthFloat, 0f, 0f, heightFloat, paint)
        }

        return bitmap.asImage()
    }

    private fun routeDetail(route: MediaRouter.RouteInfo): RouteDetail {
        return RouteDetail(
            route = route,
            volume = route.volume.toFloat(),
            volumeRange = 0f..route.volumeMax.toFloat(),
        )
    }

    private companion object {
        private const val IMAGE_SIZE = 1000
    }
}
