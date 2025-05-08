/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.androidx.mediarouter.compose.demo

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwitchLeft
import androidx.compose.material.icons.filled.SwitchRight
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.mediarouter.app.MediaRouteButton
import androidx.mediarouter.media.MediaRouteSelector
import ch.srgssr.androidx.mediarouter.compose.MediaRouteButton

class MainActivity : FragmentActivity() {
    private val mainViewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            val player by mainViewModel.player.collectAsState()

            var useCompose by remember { mutableStateOf(true) }

            DemoTheme {
                MainView(
                    player = player,
                    useCompose = useCompose,
                    routeSelector = mainViewModel.routeSelector,
                    modifier = Modifier.fillMaxSize(),
                    onFabClick = { useCompose = !useCompose },
                )
            }
        }
    }
}

@Composable
internal fun MainView(
    player: Player,
    useCompose: Boolean,
    routeSelector: MediaRouteSelector,
    modifier: Modifier = Modifier,
    onFabClick: () -> Unit,
) {
    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            SwitchImplementationButton(
                useCompose = useCompose,
                onClick = onFabClick,
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Player(
                player = player,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(fraction = 0.5f)
                    .align(Alignment.CenterStart),
            )

            CastIcon(
                useCompose = useCompose,
                routeSelector = routeSelector,
                modifier = Modifier
                    .padding(all = 16.dp)
                    .align(Alignment.TopEnd),
            )
        }
    }
}

@Composable
private fun SwitchImplementationButton(
    useCompose: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    ExtendedFloatingActionButton(
        text = {
            val textResId = if (useCompose) R.string.use_androidx else R.string.use_compose

            Text(text = stringResource(textResId))
        },
        icon = {
            val icon = if (useCompose) Icons.Default.SwitchLeft else Icons.Default.SwitchRight

            Icon(
                imageVector = icon,
                contentDescription = null,
            )
        },
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
private fun CastIcon(
    useCompose: Boolean,
    routeSelector: MediaRouteSelector,
    modifier: Modifier = Modifier,
) {
    if (useCompose) {
        MediaRouteButton(
            modifier = modifier,
            routeSelector = routeSelector,
        )
    } else {
        AndroidView(
            factory = { context ->
                MediaRouteButton(context).apply {
                    this.routeSelector = routeSelector
                }
            },
            modifier = modifier,
        )
    }
}

@Composable
private fun Player(
    player: Player,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        factory = { context ->
            PlayerView(context).apply {
                this.player = player
            }
        },
        update = { playerView ->
            playerView.player = player
        },
        modifier = modifier,
    )
}

@Composable
@PreviewLightDark
private fun MainViewAndroidXPreview() {
    DemoTheme {
        Surface {
            val context = LocalContext.current

            MainView(
                player = ExoPlayer.Builder(context).build(),
                useCompose = false,
                routeSelector = MediaRouteSelector.EMPTY,
                onFabClick = {},
            )
        }
    }
}

@Composable
@PreviewLightDark
private fun MainViewComposePreview() {
    DemoTheme {
        Surface {
            val context = LocalContext.current

            MainView(
                player = ExoPlayer.Builder(context).build(),
                useCompose = true,
                routeSelector = MediaRouteSelector.EMPTY,
                onFabClick = {},
            )
        }
    }
}
