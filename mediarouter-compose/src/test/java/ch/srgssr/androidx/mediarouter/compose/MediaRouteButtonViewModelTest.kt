/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.androidx.mediarouter.compose

import android.app.Application
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.core.content.getSystemService
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.mediarouter.media.MediaControlIntent
import androidx.mediarouter.media.MediaRouteProvider
import androidx.mediarouter.media.MediaRouteSelector
import androidx.mediarouter.media.MediaRouter
import androidx.mediarouter.media.MediaRouterParams
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@RunWith(AndroidJUnit4::class)
class MediaRouteButtonViewModelTest {
    private lateinit var context: Application
    private lateinit var provider: MediaRouteProvider
    private lateinit var router: MediaRouter
    private lateinit var viewModel: MediaRouteButtonViewModel

    @BeforeTest
    fun before() {
        val routeSelector = MediaRouteSelector.Builder()
            .addControlCategory(MediaControlIntent.CATEGORY_LIVE_VIDEO)
            .addControlCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)
            .build()

        context = ApplicationProvider.getApplicationContext()

        // Trigger static initialization inside MediaRouter
        context.getSystemService<android.media.MediaRouter>()

        provider = TestMediaRouteProvider(context)

        router = MediaRouter.getInstance(context)
        router.addProvider(provider)

        viewModel = MediaRouteButtonViewModel(context, SavedStateHandle(), routeSelector)
    }

    @AfterTest
    fun after() {
        router.routerParams = null
        router.removeProvider(provider)
        router.unselect(MediaRouter.UNSELECT_REASON_DISCONNECTED)
    }

    @Test
    fun `check the default values`() = runTest {
        viewModel.castConnectionState.test {
            assertEquals(CastConnectionState.Disconnected, awaitItem())
        }

        viewModel.dialogType.test {
            assertEquals(DialogType.None, awaitItem())
        }
    }

    @Test
    fun `check the cast connection state with a connected route`() = runTest {
        viewModel.castConnectionState.test {
            router.routes[INDEX_ROUTE_CONNECTED].select()

            shadowOf(Looper.getMainLooper()).idle()

            assertEquals(CastConnectionState.Disconnected, awaitItem())
            assertEquals(CastConnectionState.Connected, awaitItem())
        }
    }

    @Test
    fun `check the cast connection state with a connecting route`() = runTest {
        viewModel.castConnectionState.test {
            router.routes[INDEX_ROUTE_CONNECTING].select()

            shadowOf(Looper.getMainLooper()).idle()

            assertEquals(CastConnectionState.Disconnected, awaitItem())
            assertEquals(CastConnectionState.Connecting, awaitItem())
        }
    }

    @Test
    fun `check the cast connection state with a disconnected route`() = runTest {
        viewModel.castConnectionState.test {
            router.routes[INDEX_ROUTE_DISCONNECTED].select()

            shadowOf(Looper.getMainLooper()).idle()

            assertEquals(CastConnectionState.Disconnected, awaitItem())
        }
    }

    @Test(expected = IllegalStateException::class)
    fun `check the cast connection state with an invalid state route`() = runTest {
        viewModel.castConnectionState.test {
            router.routes[INDEX_ROUTE_INVALID_STATE].select()

            shadowOf(Looper.getMainLooper()).idle()

            assertEquals(CastConnectionState.Disconnected, awaitItem())
        }
    }

    @Test
    fun `check the dialog type when the dialog is hidden with the default route`() = runTest {
        viewModel.dialogType.test {
            viewModel.hideDialog()

            shadowOf(Looper.getMainLooper()).idle()

            assertEquals(DialogType.None, awaitItem())
        }
    }

    @Test
    fun `check the dialog type when the dialog is shown with the default route`() = runTest {
        viewModel.dialogType.test {
            viewModel.showDialog()

            shadowOf(Looper.getMainLooper()).idle()

            assertEquals(DialogType.None, awaitItem())
            assertEquals(DialogType.Chooser, awaitItem())
        }
    }

    @Test
    fun `check the dialog type when the dialog is shown with the default route and router params`() =
        runTest {
            router.routerParams = MediaRouterParams.Builder().build()

            viewModel.dialogType.test {
                viewModel.showDialog()

                shadowOf(Looper.getMainLooper()).idle()

                assertEquals(DialogType.None, awaitItem())
                assertEquals(DialogType.Chooser, awaitItem())
            }
        }

    @Test
    fun `check the dialog type when the dialog is shown with the default route and dynamic group`() =
        runTest {
            router.routerParams = MediaRouterParams.Builder()
                .setDialogType(MediaRouterParams.DIALOG_TYPE_DYNAMIC_GROUP)
                .build()

            viewModel.dialogType.test {
                viewModel.showDialog()

                shadowOf(Looper.getMainLooper()).idle()

                assertEquals(DialogType.None, awaitItem())
                assertEquals(DialogType.DynamicChooser, awaitItem())
            }
        }

    @Test
    fun `check the dialog type when the dialog is hidden with a non-default route`() = runTest {
        viewModel.dialogType.test {
            router.routes[INDEX_ROUTE_CONNECTED].select()
            viewModel.hideDialog()

            shadowOf(Looper.getMainLooper()).idle()

            assertEquals(DialogType.None, awaitItem())
        }
    }

    @Test
    fun `check the dialog type when the dialog is shown with a non-default route`() = runTest {
        viewModel.dialogType.test {
            router.routes[INDEX_ROUTE_CONNECTED].select()
            viewModel.showDialog()

            shadowOf(Looper.getMainLooper()).idle()

            assertEquals(DialogType.None, awaitItem())
            assertEquals(DialogType.Controller, awaitItem())
        }
    }

    @Test
    fun `check the dialog type when the dialog is shown with a non-default route and router params`() =
        runTest {
            router.routerParams = MediaRouterParams.Builder().build()

            viewModel.dialogType.test {
                router.routes[INDEX_ROUTE_CONNECTED].select()
                viewModel.showDialog()

                shadowOf(Looper.getMainLooper()).idle()

                assertEquals(DialogType.None, awaitItem())
                assertEquals(DialogType.Controller, awaitItem())
            }
        }

    @Test
    fun `check the dialog type when the dialog is shown with a non-default route and dynamic group`() =
        runTest {
            router.routerParams = MediaRouterParams.Builder()
                .setDialogType(MediaRouterParams.DIALOG_TYPE_DYNAMIC_GROUP)
                .build()

            viewModel.dialogType.test {
                router.routes[INDEX_ROUTE_CONNECTED].select()
                viewModel.showDialog()

                shadowOf(Looper.getMainLooper()).idle()

                assertEquals(DialogType.None, awaitItem())
                assertEquals(DialogType.DynamicController, awaitItem())
            }
        }

    @Test(expected = IllegalStateException::class)
    fun `ViewModel factory fails to create a ViewModel without a Context`() {
        MediaRouteButtonViewModel.Factory(MediaRouteSelector.EMPTY)
            .create(ViewModel::class.java, CreationExtras.Empty)
    }

    @Test
    fun `ViewModel factory creates an instance of MediaRouteButtonViewModel`() {
        Robolectric.buildActivity(ComponentActivity::class.java)
            .use { activityController ->
                val viewModel = ViewModelProvider(
                    owner = activityController.setup().get(),
                    factory = MediaRouteButtonViewModel.Factory(MediaRouteSelector.EMPTY),
                ).get<ViewModel>()

                assertIs<MediaRouteButtonViewModel>(viewModel)
            }
    }

    private companion object {
        // The route at index 0 is the default route
        private const val INDEX_ROUTE_DISCONNECTED = 1
        private const val INDEX_ROUTE_CONNECTING = 2
        private const val INDEX_ROUTE_CONNECTED = 3
        private const val INDEX_ROUTE_INVALID_STATE = 4
    }
}
