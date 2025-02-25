/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.androidx.mediarouter.compose

import androidx.annotation.StringRes
import androidx.mediarouter.R

/**
 * The connection state of a cast route.
 *
 * @property contentDescriptionRes The content description associated with the connection state.
 */
internal enum class CastConnectionState(@StringRes val contentDescriptionRes: Int) {
    /**
     * The device is connected to a Cast session.
     */
    Connected(R.string.mr_cast_button_connected),

    /**
     * The device is connecting to a Cast session.
     */
    Connecting(R.string.mr_cast_button_connecting),

    /**
     * The device is not connected to a Cast session.
     */
    Disconnected(R.string.mr_cast_button_disconnected),
}
