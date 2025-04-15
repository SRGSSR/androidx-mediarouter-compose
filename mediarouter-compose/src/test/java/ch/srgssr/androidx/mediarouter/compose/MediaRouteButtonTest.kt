/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.androidx.mediarouter.compose

import android.content.Context
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.core.content.getSystemService
import androidx.mediarouter.media.MediaRouter
import androidx.mediarouter.testing.MediaRouterTestHelper
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
    private lateinit var router: MediaRouter

    @get:Rule
    val composeTestRule = createComposeRule()

    @BeforeTest
    fun before() {
        context = ApplicationProvider.getApplicationContext()
        dialogTypes = mutableListOf()

        // Trigger static initialization inside MediaRouter
        context.getSystemService<android.media.MediaRouter>()

        router = MediaRouter.getInstance(context)
        router.addProvider(TestMediaRouteProvider(context))
    }

    @AfterTest
    fun after() {
        MediaRouterTestHelper.resetMediaRouter()
    }

    @Test
    fun `default dialog type`() {
        assertMediaRouteButtonState(
            expectedDialogTypes = listOf(DialogType.None),
        )
    }

    @Test
    fun `clicking on button should open the chooser dialog`() {
        assertMediaRouteButtonState(
            expectedDialogTypes = listOf(DialogType.None, DialogType.Chooser),
            action = {
                onNodeWithTag(TEST_TAG).performClick()
            },
        )
    }

    @Test
    fun `clicking on button should open the controller dialog when a non-default route is selected`() {
        assertMediaRouteButtonState(
            expectedDialogTypes = listOf(DialogType.None, DialogType.Controller),
            action = {
                router.selectRouteById(TestMediaRouteProvider.ROUTE_ID_CONNECTED)

                onNodeWithTag(TEST_TAG).performClick()
            },
        )
    }

    private fun assertMediaRouteButtonState(
        expectedDialogTypes: List<DialogType>,
        action: (ComposeTestRule.() -> Unit)? = null,
    ) {
        composeTestRule.setContent {
            MediaRouteButton(
                modifier = Modifier.testTag(TEST_TAG),
                mediaRouteChooserDialog = {},
                mediaRouteDynamicChooserDialog = {},
                mediaRouteControllerDialog = {},
                mediaRouteDynamicControllerDialog = {},
                onDialogTypeChange = dialogTypes::add,
            )
        }

        action?.let {
            composeTestRule.it()
            composeTestRule.waitForIdle()
        }

        assertEquals(expectedDialogTypes, dialogTypes)
    }

    private fun MediaRouter.selectRouteById(id: String) {
        val providerFQCN = TestMediaRouteProvider::class.qualifiedName
        val fullId = "${context.packageName}/$providerFQCN:$id"

        routes.single { it.id == fullId }.select()
    }

    private companion object {
        private const val TEST_TAG = "media_route_button"
    }
}
