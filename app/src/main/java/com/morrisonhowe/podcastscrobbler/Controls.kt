package com.morrisonhowe.podcastscrobbler

import android.content.ComponentName
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import coil.compose.rememberAsyncImagePainter
import com.google.common.util.concurrent.MoreExecutors
import com.morrisonhowe.podcastscrobbler.playerservice.MusicPlayerService
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun ControlsPreview() {
    Controls(null, rememberBottomSheetScaffoldState())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Controls(player: Player?, bottomSheetScaffoldState: BottomSheetScaffoldState) {
//    val coroutineScope = rememberCoroutineScope()
//    coroutineScope.run { launch { toggleBottomSheetState(player, bottomSheetScaffoldState) } }

    metadata(player = player)
}

//@OptIn(ExperimentalMaterial3Api::class)
//suspend fun toggleBottomSheetState(player: Player?, bottomSheetScaffoldState: BottomSheetScaffoldState) {
//    if (player != null) {
//        bottomSheetScaffoldState.bottomSheetState.show()
//    } else {
//        bottomSheetScaffoldState.bottomSheetState.hide()
//    }
//}

@Composable
fun metadata(player: Player?) {
    val (isPaused, setPaused) = remember { mutableStateOf(false) }

    Row {
        episodeImage(player = player)

        Column(
            Modifier
                .padding(5.dp)
                .width(200.dp)) {
            Text(if (player?.mediaMetadata?.title != null) player.mediaMetadata.title.toString() else "Nothing playing", fontSize = 20.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row {
                if (player?.isPlaying == true) {
                    if (isPaused) {
                        Button(onClick = { player.play(); setPaused(true) }) {
                            Icon(Icons.Filled.PlayArrow, "Resume")
                        }
                    } else {
                        Button(onClick = { player.pause(); setPaused(false) }) {
                            Icon(Icons.Filled.Pause, "Pause")
                        }
                    }

                } else {
                    Button(onClick = { }, enabled = false) {
                        Icon(Icons.Filled.PlayArrow, "Play Episode")
                    }
                }
            }
        }
    }
}

@Composable
fun episodeImage(player: Player?) {
    if (player?.mediaMetadata?.artworkUri != null) {
        Image(
            painter = rememberAsyncImagePainter(player.mediaMetadata.artworkUri),
            contentDescription = null,
            modifier = Modifier.size(128.dp)
        )
    } else {
        Icon(Icons.Filled.LibraryMusic, "No Art", Modifier.size(128.dp))
    }
}

