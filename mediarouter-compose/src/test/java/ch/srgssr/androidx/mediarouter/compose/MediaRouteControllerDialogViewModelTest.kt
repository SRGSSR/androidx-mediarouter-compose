package ch.srgssr.androidx.mediarouter.compose

import android.app.Application
import android.graphics.BitmapFactory
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.PlaybackStateCompat
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
import androidx.mediarouter.R
import androidx.mediarouter.media.MediaRouteProvider
import androidx.mediarouter.media.MediaRouter
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
    private lateinit var provider: MediaRouteProvider
    private lateinit var router: MediaRouter
    private lateinit var viewModel: MediaRouteControllerDialogViewModel

    @BeforeTest
    fun before() {
        context = ApplicationProvider.getApplicationContext()

        // Trigger static initialization inside MediaRouter
        context.getSystemService<android.media.MediaRouter>()

        provider = TestMediaRouteProvider(context)

        router = MediaRouter.getInstance(context)
        router.addProvider(provider)
        router.selectRoute(router.routes[2])

        viewModel = MediaRouteControllerDialogViewModel(
            context,
            SavedStateHandle(),
            volumeControlEnabled = true
        )
    }

    @AfterTest
    fun after() {
        router.removeProvider(provider)
    }

    @Test
    fun `check the default values`() = runTest {
        viewModel.showDialog.test {
            assertTrue(awaitItem())
        }

        viewModel.selectedRoute.test {
            assertEquals(router.routes[2], awaitItem())
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
        viewModel.mediaDescription.update { MediaDescriptionCompat.Builder().build() }

        viewModel.showPlaybackControl.test {
            assertTrue(awaitItem())
        }
    }

    @Test
    fun `check show playback control with playback state`() = runTest {
        viewModel.playbackState.update { PlaybackStateCompat.Builder().build() }

        viewModel.showPlaybackControl.test {
            assertTrue(awaitItem())
        }
    }

    @Test
    fun `check image model with null icon bitmap`() = runTest {
        viewModel.mediaDescription.update {
            MediaDescriptionCompat.Builder()
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
            MediaDescriptionCompat.Builder()
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
            MediaDescriptionCompat.Builder()
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
            MediaDescriptionCompat.Builder()
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
            MediaDescriptionCompat.Builder()
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
        router.selectRoute(router.routes[6])

        viewModel.title.test {
            assertEquals(context.getString(R.string.mr_controller_casting_screen), awaitItem())
        }
    }

    @Test
    fun `check title with playback state being none`() = runTest {
        viewModel.playbackState.update {
            PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_NONE, 0L, 0f)
                .build()
        }

        viewModel.title.test {
            assertEquals(context.getString(R.string.mr_controller_no_media_selected), awaitItem())
        }
    }

    @Test
    fun `check title with playback state different than none`() = runTest {
        viewModel.playbackState.update {
            PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PLAYING, 0L, 0f)
                .build()
        }

        viewModel.title.test {
            assertEquals(context.getString(R.string.mr_controller_no_info_available), awaitItem())
        }
    }

    @Test
    fun `check title with empty media description`() = runTest {
        viewModel.playbackState.update {
            PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PLAYING, 0L, 0f)
                .build()
        }

        viewModel.mediaDescription.update { MediaDescriptionCompat.Builder().build() }

        viewModel.title.test {
            assertEquals(context.getString(R.string.mr_controller_no_info_available), awaitItem())
        }
    }

    @Test
    fun `check title with media description containing title only`() = runTest {
        viewModel.playbackState.update {
            PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PLAYING, 0L, 0f)
                .build()
        }

        val title = "Title"

        viewModel.mediaDescription.update {
            MediaDescriptionCompat.Builder()
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
            PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PLAYING, 0L, 0f)
                .build()
        }

        val subtitle = "Subtitle"

        viewModel.mediaDescription.update {
            MediaDescriptionCompat.Builder()
                .setSubtitle(subtitle)
                .build()
        }

        viewModel.title.test {
            assertNull(awaitItem())
        }
    }

    @Test
    fun `check subtitle with empty media description`() = runTest {
        viewModel.mediaDescription.update { MediaDescriptionCompat.Builder().build() }

        viewModel.subtitle.test {
            assertNull(awaitItem())
        }
    }

    @Test
    fun `check subtitle with null subtitle`() = runTest {
        viewModel.mediaDescription.update {
            MediaDescriptionCompat.Builder()
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
            MediaDescriptionCompat.Builder()
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
            MediaDescriptionCompat.Builder()
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
            PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_BUFFERING, 0L, 0f)
                .build()
        }

        viewModel.iconInfo.test {
            assertNull(awaitItem())
        }
    }

    @Test
    fun `check icon info while buffering and pause supported`() = runTest {
        viewModel.playbackState.update {
            PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_BUFFERING, 0L, 0f)
                .setActions(PlaybackStateCompat.ACTION_PAUSE)
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
            PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_BUFFERING, 0L, 0f)
                .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE)
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
            PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_BUFFERING, 0L, 0f)
                .setActions(PlaybackStateCompat.ACTION_STOP)
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
            PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PLAYING, 0L, 0f)
                .build()
        }

        viewModel.iconInfo.test {
            assertNull(awaitItem())
        }
    }

    @Test
    fun `check icon info while playing and pause supported`() = runTest {
        viewModel.playbackState.update {
            PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PLAYING, 0L, 0f)
                .setActions(PlaybackStateCompat.ACTION_PAUSE)
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
            PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PLAYING, 0L, 0f)
                .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE)
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
            PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PLAYING, 0L, 0f)
                .setActions(PlaybackStateCompat.ACTION_STOP)
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
            PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PAUSED, 0L, 0f)
                .build()
        }

        viewModel.iconInfo.test {
            assertNull(awaitItem())
        }
    }

    @Test
    fun `check icon info while paused and play supported`() = runTest {
        viewModel.playbackState.update {
            PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PAUSED, 0L, 0f)
                .setActions(PlaybackStateCompat.ACTION_PLAY)
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
            PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PAUSED, 0L, 0f)
                .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE)
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
            assertFalse(router.routes[0].isSelected)
            assertTrue(router.routes[2].isSelected)
            assertTrue(awaitItem())

            viewModel.stopCasting()

            assertTrue(router.routes[0].isSelected)
            assertFalse(router.routes[2].isSelected)
            assertFalse(awaitItem())
        }
    }

    @Test
    fun disconnect() = runTest {
        viewModel.showDialog.test {
            assertFalse(router.routes[0].isSelected)
            assertTrue(router.routes[2].isSelected)
            assertTrue(awaitItem())

            viewModel.disconnect()

            assertTrue(router.routes[0].isSelected)
            assertFalse(router.routes[2].isSelected)
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
}
