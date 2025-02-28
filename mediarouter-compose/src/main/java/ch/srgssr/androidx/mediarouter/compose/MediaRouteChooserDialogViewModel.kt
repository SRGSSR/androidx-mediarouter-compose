package ch.srgssr.androidx.mediarouter.compose

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.mediarouter.R
import androidx.mediarouter.media.MediaRouteSelector
import androidx.mediarouter.media.MediaRouter
import androidx.mediarouter.media.MediaRouter.RouteInfo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import kotlin.time.Duration.Companion.seconds

internal class MediaRouteChooserDialogViewModel(
    private val application: Application,
    private val savedStateHandle: SavedStateHandle,
    private val routeSelector: MediaRouteSelector,
) : AndroidViewModel(application) {
    enum class ChooserState {
        FindingDevices,
        NoDevicesNoWifiHint,
        NoRoutes,
        ShowingRoutes;

        @VisibleForTesting
        internal fun title(context: Context): String {
            val titleRes = when (this) {
                FindingDevices,
                NoDevicesNoWifiHint,
                ShowingRoutes -> R.string.mr_chooser_title

                NoRoutes -> R.string.mr_chooser_zero_routes_found_title
            }

            return context.getString(titleRes)
        }

        @VisibleForTesting
        internal fun confirmLabel(context: Context): String? {
            return when (this) {
                FindingDevices,
                NoDevicesNoWifiHint,
                ShowingRoutes -> null

                NoRoutes -> context.getString(android.R.string.ok)
            }
        }
    }

    private val mediaRouterCallback = MediaRouterCallback()
    private val router = MediaRouter.getInstance(application)
    private val screenOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_SCREEN_OFF) {
                hideDialog()
            }
        }
    }

    private val routerUpdates = MutableStateFlow(0)
    private val _routes = routerUpdates.map {
        router.routes
            .filter { route ->
                !route.isDefaultOrBluetooth &&
                        route.isEnabled &&
                        route.matchesSelector(routeSelector)
            }
            .sortedBy(RouteInfo::getName)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _chooserState = _routes.transformLatest { routes ->
        if (routes.isEmpty()) {
            emit(ChooserState.FindingDevices)

            delay(5.seconds)

            emit(ChooserState.NoDevicesNoWifiHint)

            delay(15.seconds)

            emit(ChooserState.NoRoutes)

            router.removeCallback(mediaRouterCallback)
        } else {
            emit(ChooserState.ShowingRoutes)
        }
    }

    val showDialog = savedStateHandle.getStateFlow(KEY_SHOW_DIALOG, true)
    val routes = _routes.stateIn(viewModelScope, WhileSubscribed(), emptyList())
    val chooserState = _chooserState
        .stateIn(viewModelScope, WhileSubscribed(), ChooserState.FindingDevices)
    val title = _chooserState.map { it.title(application) }
        .stateIn(viewModelScope, WhileSubscribed(), "")
    val confirmButtonLabel = _chooserState.map { it.confirmLabel(application) }
        .stateIn(viewModelScope, WhileSubscribed(), null)

    init {
        router.addCallback(
            routeSelector,
            mediaRouterCallback,
            MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN,
        )

        application.registerReceiver(screenOffReceiver, IntentFilter(Intent.ACTION_SCREEN_OFF))
    }

    fun hideDialog() {
        savedStateHandle[KEY_SHOW_DIALOG] = false
    }

    override fun onCleared() {
        router.removeCallback(mediaRouterCallback)
        application.unregisterReceiver(screenOffReceiver)
    }

    private companion object {
        private const val KEY_SHOW_DIALOG = "showDialog"
    }

    class Factory(private val routeSelector: MediaRouteSelector) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            val application = checkNotNull(extras[APPLICATION_KEY])
            val savedStateHandle = extras.createSavedStateHandle()

            @Suppress("UNCHECKED_CAST")
            return MediaRouteChooserDialogViewModel(
                application = application,
                savedStateHandle = savedStateHandle,
                routeSelector = routeSelector,
            ) as T
        }
    }

    private inner class MediaRouterCallback : MediaRouter.Callback() {
        override fun onRouteAdded(router: MediaRouter, route: RouteInfo) {
            routerUpdates.update { it + 1 }
        }

        override fun onRouteRemoved(router: MediaRouter, route: RouteInfo) {
            routerUpdates.update { it + 1 }
        }

        override fun onRouteChanged(router: MediaRouter, route: RouteInfo) {
            routerUpdates.update { it + 1 }
        }

        override fun onRouteSelected(
            router: MediaRouter,
            selectedRoute: RouteInfo,
            reason: Int,
            requestedRoute: RouteInfo,
        ) {
            routerUpdates.update { it + 1 }
        }
    }
}
