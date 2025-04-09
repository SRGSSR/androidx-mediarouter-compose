/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.androidx.mediarouter.compose

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.core.content.getSystemService
import androidx.mediarouter.R
import androidx.mediarouter.media.MediaRouteProvider
import androidx.mediarouter.media.MediaRouter
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.runner.RunWith
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class MediaRouteButtonTest {
    private lateinit var context: Context
    private lateinit var dialogTypes: MutableList<DialogType>
    private lateinit var provider: MediaRouteProvider
    private lateinit var router: MediaRouter

    @get:Rule
    val composeTestRule = createComposeRule()

    @BeforeTest
    fun before() {
        context = ApplicationProvider.getApplicationContext()
        dialogTypes = mutableListOf()

        // Trigger static initialization inside MediaRouter
        context.getSystemService<android.media.MediaRouter>()

        provider = TestMediaRouteProvider(context)

        router = MediaRouter.getInstance(context)
        router.addProvider(provider)
    }

    @AfterTest
    fun after() {
        router.removeProvider(provider)
        router.unselect(MediaRouter.UNSELECT_REASON_DISCONNECTED)
    }

    @Test
    fun `default dialog type`() {
        assertMediaRouteButtonState(
            expectedDialogTypes = listOf(DialogType.None),
            contentDescriptionRes = R.string.mr_cast_button_disconnected,
        )
    }

    @Test
    fun `clicking on button should open the chooser dialog`() {
        assertMediaRouteButtonState(
            expectedDialogTypes = listOf(DialogType.None, DialogType.Chooser),
            contentDescriptionRes = R.string.mr_cast_button_disconnected,
            action = {
                onNodeWithTag(TEST_TAG).performClick()
            },
        )
    }

    @Test
    fun `clicking on button should open the controller dialog when a non-default route is selected`() {
        router.routes[3].select()

        assertMediaRouteButtonState(
            expectedDialogTypes = listOf(
                DialogType.None,
                DialogType.Controller,
            ),
            contentDescriptionRes = R.string.mr_cast_button_connected,
            action = {
                onNodeWithTag(TEST_TAG).performClick()
            },
        )
    }

    private fun assertMediaRouteButtonState(
        expectedDialogTypes: List<DialogType>,
        @StringRes contentDescriptionRes: Int,
        action: (ComposeTestRule.() -> Unit)? = null,
    ) {
        composeTestRule.setContent {
            MediaRouteButton(
                modifier = Modifier.testTag(TEST_TAG),
                onDialogTypeChange = dialogTypes::add,
            )
        }

        action?.let {
            composeTestRule.it()
            composeTestRule.waitForIdle()
        }

        composeTestRule.onNodeWithTag(TEST_TAG)
            .assertContentDescriptionEquals(context.getString(contentDescriptionRes))

        assertEquals(expectedDialogTypes, dialogTypes)
    }

    private companion object {
        private const val TEST_TAG = "media_route_button"
    }
}
