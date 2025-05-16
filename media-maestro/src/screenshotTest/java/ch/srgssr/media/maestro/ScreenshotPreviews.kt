/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.media.maestro

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.content.res.Configuration.UI_MODE_TYPE_NORMAL
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview

@Preview(name = "Light - Portrait", device = Devices.PHONE)
@Preview(name = "Light - Landscape", device = "${Devices.PHONE},orientation=landscape")
@Preview(
    name = "Dark - Portrait",
    device = Devices.PHONE,
    uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL
)
@Preview(
    name = "Dark - Landscape",
    device = "${Devices.PHONE},orientation=landscape",
    uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL,
)
internal annotation class ScreenshotPreviews
