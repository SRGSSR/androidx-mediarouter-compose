package ch.srgssr.androidx.mediarouter.compose

import android.app.Application
import android.os.Looper
import androidx.core.content.getSystemService
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.mediarouter.R
import androidx.mediarouter.media.MediaControlIntent
import androidx.mediarouter.media.MediaRouteProvider
import androidx.mediarouter.media.MediaRouteSelector
import androidx.mediarouter.media.MediaRouter
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.cash.turbine.test
import ch.srgssr.androidx.mediarouter.compose.MediaRouteChooserDialogViewModel.ChooserState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class MediaRouteChooserDialogViewModelTest {
    private lateinit var context: Application
    private lateinit var provider: MediaRouteProvider
    private lateinit var router: MediaRouter
    private lateinit var viewModel: MediaRouteChooserDialogViewModel

    @BeforeTest
    @OptIn(ExperimentalCoroutinesApi::class)
    fun before() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        val routeSelector = MediaRouteSelector.Builder()
            .addControlCategory(MediaControlIntent.CATEGORY_LIVE_VIDEO)
            .addControlCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)
            .build()

        context = ApplicationProvider.getApplicationContext<Application>()

        // Trigger static initialization inside MediaRouter
        context.getSystemService<android.media.MediaRouter>()

        provider = TestMediaRouteProvider(context)

        router = MediaRouter.getInstance(context)

        viewModel = MediaRouteChooserDialogViewModel(context, routeSelector)
    }

    @AfterTest
    @OptIn(ExperimentalCoroutinesApi::class)
    fun after() {
        router.removeProvider(provider)

        shadowOf(Looper.getMainLooper()).idle()

        Dispatchers.resetMain()
    }

    @Test
    fun `check the default values`() = runTest {
        viewModel.showDialog.test {
            assertTrue(awaitItem())
        }

        viewModel.routes.test {
            assertEquals(emptyList(), awaitItem())
        }

        viewModel.chooserState.test {
            assertEquals(ChooserState.FindingDevices, awaitItem())
        }

        viewModel.title.test {
            assertEquals(context.getString(R.string.mr_chooser_title), awaitItem())
        }

        viewModel.confirmButtonLabel.test {
            assertNull(awaitItem())
        }
    }

    @Test
    fun `hide dialog`() = runTest {
        router.addProvider(provider)

        viewModel.showDialog.test {
            assertTrue(awaitItem())

            viewModel.hideDialog()

            assertFalse(awaitItem())
        }
    }

    @Test
    fun `check that the route are filtered and sorted alphabetically`() = runTest {
        router.addProvider(provider)

        viewModel.routes.test {
            assertEquals(
                listOf("Connected route", "Disconnected route"),
                awaitItem().map { it.name },
            )
        }
    }

    @Test
    fun `check that the chooser state moves to ShowingRoutes when there are routes available`() {
        runTest {
            viewModel.chooserState.test {
                assertEquals(ChooserState.FindingDevices, awaitItem())

                router.addProvider(provider)
                InstrumentationRegistry.getInstrumentation().waitForIdleSync()

                assertEquals(ChooserState.ShowingRoutes, awaitItem())
            }
        }
    }

    @Test
    fun `check that the chooser state is updated periodically when no routes are available`() {
        runTest {
            viewModel.chooserState.test {
                assertEquals(ChooserState.FindingDevices, awaitItem())
                assertEquals(ChooserState.NoDevicesNoWifiHint, awaitItem())
                assertEquals(ChooserState.NoRoutes, awaitItem())
            }
        }
    }

    @Test
    fun `check the title based on the chooser state`() {
        assertEquals(
            expected = context.getString(R.string.mr_chooser_title),
            actual = ChooserState.FindingDevices.title(context),
        )
        assertEquals(
            expected = context.getString(R.string.mr_chooser_title),
            actual = ChooserState.NoDevicesNoWifiHint.title(context),
        )
        assertEquals(
            expected = context.getString(R.string.mr_chooser_zero_routes_found_title),
            actual = ChooserState.NoRoutes.title(context),
        )
        assertEquals(
            expected = context.getString(R.string.mr_chooser_title),
            actual = ChooserState.ShowingRoutes.title(context),
        )
    }

    @Test
    fun `check the confirm label based on the chooser state`() {
        assertNull(ChooserState.FindingDevices.confirmLabel(context))
        assertNull(ChooserState.NoDevicesNoWifiHint.confirmLabel(context))
        assertEquals(
            expected = context.getString(android.R.string.ok),
            actual = ChooserState.NoRoutes.confirmLabel(context),
        )
        assertNull(ChooserState.ShowingRoutes.confirmLabel(context))
    }

    @Test(expected = IllegalStateException::class)
    fun `ViewModel factory fails to create a ViewModel without a Context`() {
        MediaRouteChooserDialogViewModel.Factory(MediaRouteSelector.EMPTY)
            .create(ViewModel::class.java, CreationExtras.Empty)
    }

    @Test
    fun `ViewModel factory creates an instance of MediaRouteChooserDialogViewModel`() {
        val creationExtras = MutableCreationExtras()
        creationExtras[APPLICATION_KEY] = context

        val viewModel = MediaRouteChooserDialogViewModel.Factory(MediaRouteSelector.EMPTY)
            .create(ViewModel::class.java, creationExtras)

        assertIs<MediaRouteChooserDialogViewModel>(viewModel)
    }
}
