package ch.srgssr.androidx.mediarouter.compose

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.mediarouter.app.SystemOutputSwitcherDialogController
import androidx.mediarouter.media.MediaRouteSelector
import androidx.mediarouter.media.MediaRouter
import androidx.mediarouter.media.MediaRouter.RouteInfo
import androidx.mediarouter.media.MediaRouterParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class MediaRouteButtonViewModel(
    application: Application,
    private val routeSelector: MediaRouteSelector,
) : ViewModel() {
    enum class DialogType {
        Chooser,
        DynamicChooser,
        Controller,
        DynamicController,
        None,
    }

    private val mediaRouterCallback = MediaRouterCallback()
    private val router = MediaRouter.getInstance(application)

    private val routerUpdates = MutableStateFlow(0)
    private val showDialog = MutableStateFlow(false)

    val castConnectionState = routerUpdates
        .map { router.selectedRoute }
        .map { route ->
            if (route.isDefaultOrBluetooth) {
                CastConnectionState.Disconnected
            } else {
                when (val connectionState = route.connectionState) {
                    RouteInfo.CONNECTION_STATE_CONNECTED -> CastConnectionState.Connected
                    RouteInfo.CONNECTION_STATE_CONNECTING -> CastConnectionState.Connecting
                    RouteInfo.CONNECTION_STATE_DISCONNECTED -> CastConnectionState.Disconnected
                    else -> error("Unknown connection state: $connectionState")
                }
            }
        }
        .stateIn(viewModelScope, WhileSubscribed(), CastConnectionState.Disconnected)
    val dialogType = combine(showDialog, routerUpdates) { showDialog, _ ->
        if (!showDialog) {
            return@combine DialogType.None
        }

        val routerParams = router.routerParams
        if (routerParams != null) {
            if (routerParams.isOutputSwitcherEnabled && MediaRouter.isMediaTransferEnabled()) {
                if (SystemOutputSwitcherDialogController.showDialog(application)) {
                    return@combine DialogType.None
                }
            }
        }

        val dynamicGroup = routerParams?.dialogType == MediaRouterParams.DIALOG_TYPE_DYNAMIC_GROUP
        if (router.selectedRoute.isDefaultOrBluetooth) {
            if (dynamicGroup) {
                DialogType.DynamicChooser
            } else {
                DialogType.Chooser
            }
        } else {
            if (dynamicGroup) {
                DialogType.DynamicController
            } else {
                DialogType.Controller
            }
        }
    }.distinctUntilChanged()

    init {
        if (!routeSelector.isEmpty) {
            router.addCallback(routeSelector, mediaRouterCallback)
        }
    }

    fun showDialog() {
        showDialog.update { true }
    }

    fun hideDialog() {
        showDialog.update { false }
    }

    override fun onCleared() {
        if (!routeSelector.isEmpty) {
            router.removeCallback(mediaRouterCallback)
        }
    }

    class Factory(private val routeSelector: MediaRouteSelector) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            val application = checkNotNull(extras[APPLICATION_KEY])

            @Suppress("UNCHECKED_CAST")
            return MediaRouteButtonViewModel(application, routeSelector) as T
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

        override fun onRouteSelected(router: MediaRouter, route: RouteInfo, reason: Int) {
            routerUpdates.update { it + 1 }
        }

        override fun onRouteUnselected(router: MediaRouter, route: RouteInfo, reason: Int) {
            routerUpdates.update { it + 1 }
        }
    }
}
