/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.media.maestro

import androidx.mediarouter.R
import kotlin.test.Test
import kotlin.test.assertEquals

class CastConnectionStateTest {
    @Test
    fun `content description res for the Connected state`() {
        assertEquals(
            R.string.mr_cast_button_connected,
            CastConnectionState.Connected.contentDescriptionRes,
        )
    }

    @Test
    fun `content description res for the Connecting state`() {
        assertEquals(
            R.string.mr_cast_button_connecting,
            CastConnectionState.Connecting.contentDescriptionRes,
        )
    }

    @Test
    fun `content description res for the Disconnected state`() {
        assertEquals(
            R.string.mr_cast_button_disconnected,
            CastConnectionState.Disconnected.contentDescriptionRes,
        )
    }
}
