package ch.srgssr.pillarbox.mediarouter.compose.ui.mediarouter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.mediarouter.R
import androidx.mediarouter.media.MediaRouter

@Composable
fun MediaRouteControllerDialog(
    router: MediaRouter,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
) {
    val selectedRoute = remember { router.selectedRoute }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    if (selectedRoute.isSelected) {
                        router.unselect(MediaRouter.UNSELECT_REASON_STOPPED)
                    }

                    onDismissRequest()
                },
                modifier = modifier,
            ) {
                Text(text = stringResource(R.string.mr_controller_stop_casting))
            }
        },
        modifier = modifier,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = selectedRoute.name)

                IconButton(onClick = onDismissRequest) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.mr_controller_close_description),
                    )
                }
            }
        },
        text = {
            Text(text = "Controller dialog...")
        },
    )
}
