/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.androidx.mediarouter.compose

import android.app.Application
import android.graphics.BitmapFactory
import android.media.MediaDescription
import android.media.session.PlaybackState
import android.view.KeyEvent.ACTION_DOWN
import android.view.KeyEvent.KEYCODE_A
import android.view.KeyEvent.KEYCODE_VOLUME_DOWN
import android.view.KeyEvent.KEYCODE_VOLUME_UP
import androidx.activity.ComponentActivity
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.NativeKeyEvent
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.testing.ViewModelScenario
import androidx.lifecycle.viewmodel.testing.viewModelScenario
import androidx.mediarouter.R
import androidx.mediarouter.media.MediaRouter
import androidx.mediarouter.testing.MediaRouterTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class MediaRouteControllerDialogViewModelTest {
    private lateinit var context: Application
    private lateinit var router: MediaRouter
    private lateinit var viewModelScenario: ViewModelScenario<MediaRouteControllerDialogViewModel>

    private val viewModel: MediaRouteControllerDialogViewModel
        get() = viewModelScenario.viewModel

    @BeforeTest
    fun before() {
        context = ApplicationProvider.getApplicationContext()

        // Trigger static initialization inside MediaRouter
        context.getSystemService<android.media.MediaRouter>()

        router = MediaRouter.getInstance(context)
        router.addProvider(TestMediaRouteProvider(context))
        router.selectRouteById(TestMediaRouteProvider.ROUTE_ID_CONNECTED)

        viewModelScenario = viewModelScenario {
            MediaRouteControllerDialogViewModel(context, SavedStateHandle(), true)
        }
    }

    @AfterTest
    fun after() {
        viewModelScenario.close()

        MediaRouterTestHelper.resetMediaRouter()
    }

    @Test
    fun `check the default values`() = runTest {
        viewModel.showDialog.test {
            assertTrue(awaitItem())
        }

        viewModel.selectedRoute.test {
            assertEquals(TestMediaRouteProvider.ROUTE_NAME_CONNECTED, awaitItem().name)
        }

        viewModel.isDeviceGroupExpanded.test {
            assertFalse(awaitItem())
        }

        viewModel.showPlaybackControl.test {
            assertFalse(awaitItem())
        }

        viewModel.showVolumeControl.test {
            assertFalse(awaitItem())
        }

        viewModel.imageModel.test {
            assertNull(awaitItem())
        }

        viewModel.title.test {
            assertEquals(context.getString(R.string.mr_controller_no_media_selected), awaitItem())
        }

        viewModel.subtitle.test {
            assertNull(awaitItem())
        }

        viewModel.iconInfo.test {
            assertNull(awaitItem())
        }
    }

    @Test
    fun `check show playback control with media description`() = runTest {
        viewModel.mediaDescription.update { MediaDescription.Builder().build() }

        viewModel.showPlaybackControl.test {
            assertTrue(awaitItem())
        }
    }

    @Test
    fun `check show playback control with playback state`() = runTest {
        viewModel.playbackState.update { PlaybackState.Builder().build() }

        viewModel.showPlaybackControl.test {
            assertTrue(awaitItem())
        }
    }

    @Test
    fun `check image model with null icon bitmap`() = runTest {
        viewModel.mediaDescription.update {
            MediaDescription.Builder()
                .setIconBitmap(null)
                .build()
        }

        viewModel.imageModel.test {
            assertNull(awaitItem())
        }
    }

    @Test
    fun `check image model with recycled icon bitmap`() = runTest {
        val bitmap = BitmapFactory.decodeByteArray(byteArrayOf(), 0, 0)
        bitmap.recycle()

        viewModel.mediaDescription.update {
            MediaDescription.Builder()
                .setIconBitmap(bitmap)
                .build()
        }

        viewModel.imageModel.test {
            assertNull(awaitItem())
        }
    }

    @Test
    fun `check image model with icon bitmap`() = runTest {
        val bitmap = BitmapFactory.decodeByteArray(byteArrayOf(), 0, 0)

        viewModel.mediaDescription.update {
            MediaDescription.Builder()
                .setIconBitmap(bitmap)
                .build()
        }

        viewModel.imageModel.test {
            assertEquals(bitmap, awaitItem())
        }
    }

    @Test
    fun `check image model with icon URI`() = runTest {
        val iconUri = "https://example.com/icon.png".toUri()

        viewModel.mediaDescription.update {
            MediaDescription.Builder()
                .setIconUri(iconUri)
                .build()
        }

        viewModel.imageModel.test {
            assertEquals(iconUri, awaitItem())
        }
    }

    @Test
    fun `check image model with icon bitmap and icon URI`() = runTest {
        val bitmap = BitmapFactory.decodeByteArray(byteArrayOf(), 0, 0)
        val iconUri = "https://example.com/icon.png".toUri()

        viewModel.mediaDescription.update {
            MediaDescription.Builder()
                .setIconBitmap(bitmap)
                .setIconUri(iconUri)
                .build()
        }

        viewModel.imageModel.test {
            assertEquals(bitmap, awaitItem())
        }
    }

    @Test
    fun `check title with selected route has a presentation display id`() = runTest {
        router.selectRouteById(TestMediaRouteProvider.ROUTE_ID_PRESENTATION)

        viewModel.title.test {
            assertEquals(context.getString(R.string.mr_controller_casting_screen), awaitItem())
        }
    }

    @Test
    fun `check title with playback state being none`() = runTest {
        viewModel.playbackState.update {
            PlaybackState.Builder()
                .setState(PlaybackState.STATE_NONE, 0L, 0f)
                .build()
        }

        viewModel.title.test {
            assertEquals(context.getString(R.string.mr_controller_no_media_selected), awaitItem())
        }
    }

    @Test
    fun `check title with playback state different than none`() = runTest {
        viewModel.playbackState.update {
            PlaybackState.Builder()
                .setState(PlaybackState.STATE_PLAYING, 0L, 0f)
                .build()
        }

        viewModel.title.test {
            assertEquals(context.getString(R.string.mr_controller_no_info_available), awaitItem())
        }
    }

    @Test
    fun `check title with empty media description`() = runTest {
        viewModel.playbackState.update {
            PlaybackState.Builder()
                .setState(PlaybackState.STATE_PLAYING, 0L, 0f)
                .build()
        }

        viewModel.mediaDescription.update { MediaDescription.Builder().build() }

        viewModel.title.test {
            assertEquals(context.getString(R.string.mr_controller_no_info_available), awaitItem())
        }
    }

    @Test
    fun `check title with media description containing title only`() = runTest {
        viewModel.playbackState.update {
            PlaybackState.Builder()
                .setState(PlaybackState.STATE_PLAYING, 0L, 0f)
                .build()
        }

        val title = "Title"

        viewModel.mediaDescription.update {
            MediaDescription.Builder()
                .setTitle(title)
                .build()
        }

        viewModel.title.test {
            assertEquals(title, awaitItem())
        }
    }

    @Test
    fun `check title with media description containing subtitle only`() = runTest {
        viewModel.playbackState.update {
            PlaybackState.Builder()
                .setState(PlaybackState.STATE_PLAYING, 0L, 0f)
                .build()
        }

        val subtitle = "Subtitle"

        viewModel.mediaDescription.update {
            MediaDescription.Builder()
                .setSubtitle(subtitle)
                .build()
        }

        viewModel.title.test {
            assertNull(awaitItem())
        }
    }

    @Test
    fun `check subtitle with empty media description`() = runTest {
        viewModel.mediaDescription.update { MediaDescription.Builder().build() }

        viewModel.subtitle.test {
            assertNull(awaitItem())
        }
    }

    @Test
    fun `check subtitle with null subtitle`() = runTest {
        viewModel.mediaDescription.update {
            MediaDescription.Builder()
                .setSubtitle(null)
                .build()
        }

        viewModel.subtitle.test {
            assertNull(awaitItem())
        }
    }

    @Test
    fun `check subtitle with empty subtitle`() = runTest {
        viewModel.mediaDescription.update {
            MediaDescription.Builder()
                .setSubtitle("")
                .build()
        }

        viewModel.subtitle.test {
            assertEquals("", awaitItem())
        }
    }

    @Test
    fun `check subtitle with non-empty subtitle`() = runTest {
        val subtitle = "Subtitle"

        viewModel.mediaDescription.update {
            MediaDescription.Builder()
                .setSubtitle(subtitle)
                .build()
        }

        viewModel.subtitle.test {
            assertEquals(subtitle, awaitItem())
        }
    }

    @Test
    fun `check icon info while buffering and no capabilities`() = runTest {
        viewModel.playbackState.update {
            PlaybackState.Builder()
                .setState(PlaybackState.STATE_BUFFERING, 0L, 0f)
                .build()
        }

        viewModel.iconInfo.test {
            assertNull(awaitItem())
        }
    }

    @Test
    fun `check icon info while buffering and pause supported`() = runTest {
        viewModel.playbackState.update {
            PlaybackState.Builder()
                .setState(PlaybackState.STATE_BUFFERING, 0L, 0f)
                .setActions(PlaybackState.ACTION_PAUSE)
                .build()
        }

        viewModel.iconInfo.test {
            assertEquals(
                expected = Pair(Icons.Pause, context.getString(R.string.mr_controller_pause)),
                actual = awaitItem(),
            )
        }
    }

    @Test
    fun `check icon info while buffering and play pause supported`() = runTest {
        viewModel.playbackState.update {
            PlaybackState.Builder()
                .setState(PlaybackState.STATE_BUFFERING, 0L, 0f)
                .setActions(PlaybackState.ACTION_PLAY_PAUSE)
                .build()
        }

        viewModel.iconInfo.test {
            assertEquals(
                expected = Pair(Icons.Pause, context.getString(R.string.mr_controller_pause)),
                actual = awaitItem(),
            )
        }
    }

    @Test
    fun `check icon info while buffering and stop supported`() = runTest {
        viewModel.playbackState.update {
            PlaybackState.Builder()
                .setState(PlaybackState.STATE_BUFFERING, 0L, 0f)
                .setActions(PlaybackState.ACTION_STOP)
                .build()
        }

        viewModel.iconInfo.test {
            assertEquals(
                expected = Pair(Icons.Stop, context.getString(R.string.mr_controller_stop)),
                actual = awaitItem(),
            )
        }
    }

    @Test
    fun `check icon info while playing and no capabilities`() = runTest {
        viewModel.playbackState.update {
            PlaybackState.Builder()
                .setState(PlaybackState.STATE_PLAYING, 0L, 0f)
                .build()
        }

        viewModel.iconInfo.test {
            assertNull(awaitItem())
        }
    }

    @Test
    fun `check icon info while playing and pause supported`() = runTest {
        viewModel.playbackState.update {
            PlaybackState.Builder()
                .setState(PlaybackState.STATE_PLAYING, 0L, 0f)
                .setActions(PlaybackState.ACTION_PAUSE)
                .build()
        }

        viewModel.iconInfo.test {
            assertEquals(
                expected = Pair(Icons.Pause, context.getString(R.string.mr_controller_pause)),
                actual = awaitItem(),
            )
        }
    }

    @Test
    fun `check icon info while playing and play pause supported`() = runTest {
        viewModel.playbackState.update {
            PlaybackState.Builder()
                .setState(PlaybackState.STATE_PLAYING, 0L, 0f)
                .setActions(PlaybackState.ACTION_PLAY_PAUSE)
                .build()
        }

        viewModel.iconInfo.test {
            assertEquals(
                expected = Pair(Icons.Pause, context.getString(R.string.mr_controller_pause)),
                actual = awaitItem(),
            )
        }
    }

    @Test
    fun `check icon info while playing and stop supported`() = runTest {
        viewModel.playbackState.update {
            PlaybackState.Builder()
                .setState(PlaybackState.STATE_PLAYING, 0L, 0f)
                .setActions(PlaybackState.ACTION_STOP)
                .build()
        }

        viewModel.iconInfo.test {
            assertEquals(
                expected = Pair(Icons.Stop, context.getString(R.string.mr_controller_stop)),
                actual = awaitItem(),
            )
        }
    }

    @Test
    fun `check icon info while paused and no capabilities`() = runTest {
        viewModel.playbackState.update {
            PlaybackState.Builder()
                .setState(PlaybackState.STATE_PAUSED, 0L, 0f)
                .build()
        }

        viewModel.iconInfo.test {
            assertNull(awaitItem())
        }
    }

    @Test
    fun `check icon info while paused and play supported`() = runTest {
        viewModel.playbackState.update {
            PlaybackState.Builder()
                .setState(PlaybackState.STATE_PAUSED, 0L, 0f)
                .setActions(PlaybackState.ACTION_PLAY)
                .build()
        }

        viewModel.iconInfo.test {
            assertEquals(
                expected = Pair(Icons.PlayArrow, context.getString(R.string.mr_controller_play)),
                actual = awaitItem(),
            )
        }
    }

    @Test
    fun `check icon info while paused and play pause supported`() = runTest {
        viewModel.playbackState.update {
            PlaybackState.Builder()
                .setState(PlaybackState.STATE_PAUSED, 0L, 0f)
                .setActions(PlaybackState.ACTION_PLAY_PAUSE)
                .build()
        }

        viewModel.iconInfo.test {
            assertEquals(
                expected = Pair(Icons.PlayArrow, context.getString(R.string.mr_controller_play)),
                actual = awaitItem(),
            )
        }
    }

    @Test
    fun `hide dialog`() = runTest {
        viewModel.showDialog.test {
            assertTrue(awaitItem())

            viewModel.hideDialog()

            assertFalse(awaitItem())
        }
    }

    @Test
    fun `toggle device group`() = runTest {
        viewModel.isDeviceGroupExpanded.test {
            assertFalse(awaitItem())

            viewModel.toggleDeviceGroup()

            assertTrue(awaitItem())

            viewModel.toggleDeviceGroup()

            assertFalse(awaitItem())
        }
    }

    @Test
    fun `stop casting`() = runTest {
        viewModel.showDialog.test {
            assertEquals(TestMediaRouteProvider.ROUTE_NAME_CONNECTED, router.selectedRoute.name)
            assertTrue(awaitItem())

            viewModel.stopCasting()

            assertEquals("Phone", router.selectedRoute.name)
            assertFalse(awaitItem())
        }
    }

    @Test
    fun disconnect() = runTest {
        viewModel.showDialog.test {
            assertEquals(TestMediaRouteProvider.ROUTE_NAME_CONNECTED, router.selectedRoute.name)
            assertTrue(awaitItem())

            viewModel.disconnect()

            assertEquals("Phone", router.selectedRoute.name)
            assertFalse(awaitItem())
        }
    }

    @Test
    fun `on key event, volume down`() = runTest {
        val keyEvent = KeyEvent(NativeKeyEvent(ACTION_DOWN, KEYCODE_VOLUME_DOWN))

        assertTrue(viewModel.onKeyEvent(keyEvent))
    }

    @Test
    fun `on key event, volume up`() {
        val keyEvent = KeyEvent(NativeKeyEvent(ACTION_DOWN, KEYCODE_VOLUME_UP))

        assertTrue(viewModel.onKeyEvent(keyEvent))
    }

    @Test
    fun `on key event, non-volume key`() {
        val keyEvent = KeyEvent(NativeKeyEvent(ACTION_DOWN, KEYCODE_A))

        assertFalse(viewModel.onKeyEvent(keyEvent))
    }

    @Test(expected = IllegalStateException::class)
    fun `ViewModel factory fails to create a ViewModel without a Context`() {
        MediaRouteControllerDialogViewModel.Factory(volumeControlEnabled = true)
            .create(ViewModel::class.java, CreationExtras.Empty)
    }

    @Test
    fun `ViewModel factory creates an instance of MediaRouteControllerDialogViewModel`() {
        Robolectric.buildActivity(ComponentActivity::class.java)
            .use { activityController ->
                val viewModel = ViewModelProvider(
                    owner = activityController.setup().get(),
                    factory = MediaRouteControllerDialogViewModel.Factory(volumeControlEnabled = true),
                ).get<ViewModel>()

                assertIs<MediaRouteControllerDialogViewModel>(viewModel)
            }
    }

    private fun MediaRouter.selectRouteById(id: String) {
        val providerFQCN = TestMediaRouteProvider::class.qualifiedName
        val fullId = "${context.packageName}/$providerFQCN:$id"

        routes.single { it.id == fullId }.select()
    }
}
