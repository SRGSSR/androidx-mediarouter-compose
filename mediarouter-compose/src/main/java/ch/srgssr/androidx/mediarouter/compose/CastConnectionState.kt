package ch.srgssr.androidx.mediarouter.compose

import androidx.annotation.StringRes
import androidx.mediarouter.R

enum class CastConnectionState(@StringRes val contentDescriptionRes: Int) {
    Connected(R.string.mr_cast_button_connected),
    Connecting(R.string.mr_cast_button_connecting),
    Disconnected(R.string.mr_cast_button_disconnected),
}
