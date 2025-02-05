package ch.srgssr.androidx.mediarouter.compose.demo

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.mediarouter.app.MediaRouteButton
import androidx.mediarouter.media.MediaControlIntent
import androidx.mediarouter.media.MediaRouteSelector
import ch.srgssr.androidx.mediarouter.compose.MediaRouteButton

class MainActivity : FragmentActivity() {
    private val routeSelector = MediaRouteSelector.Builder()
        .addControlCategory(MediaControlIntent.CATEGORY_LIVE_VIDEO)
        .addControlCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            DemoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Row(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        MediaRouteButtonType(
                            label = "Compose",
                            content = {
                                MediaRouteButton(routeSelector = routeSelector)
                            },
                        )

                        MediaRouteButtonType(
                            label = "AppCompat",
                            content = {
                                AndroidView(
                                    factory = { context ->
                                        MediaRouteButton(context).apply {
                                            routeSelector = this@MainActivity.routeSelector
                                        }
                                    },
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MediaRouteButtonType(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge,
        )

        content()
    }
}

@Preview
@Composable
private fun MediaRouteButtonTypePreview() {
    DemoTheme {
        MediaRouteButtonType(
            label = "Type",
            content = {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.Cast,
                        contentDescription = null,
                    )
                }
            },
        )
    }
}
