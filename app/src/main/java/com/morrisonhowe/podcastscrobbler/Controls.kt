package com.morrisonhowe.podcastscrobbler

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.morrisonhowe.podcastscrobbler.playerservice.MusicPlayerService

@Composable
fun Controls(service: MusicPlayerService?, playWhenReady: Boolean, paused: Boolean, setPaused: (Boolean) -> Unit) {

    Row {
        EpisodeImage(service)

        Column(
            Modifier
                .padding(5.dp)
                .width(200.dp)) {
            Text(if (service?.episode?.title != null) service.episode!!.title else "Nothing playing", color = MaterialTheme.colorScheme.onTertiaryContainer, fontSize = 20.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row {
                if (playWhenReady || paused) {
                    if (paused) {
                        Button(onClick = { setPaused(false); service?.play(); }) {
                            Icon(Icons.Filled.PlayArrow, "Resume")
                        }
                    } else {
                        Button(onClick = { setPaused(true); service?.pause(); }) {
                            Icon(Icons.Filled.Pause, "Pause")
                        }
                    }

                } else {
                    Button(onClick = { }, enabled = false) {
                        Icon(Icons.Filled.PlayArrow, "Nothing playing")
                    }
                }
            }
        }
    }
}

@Composable
fun EpisodeImage(service: MusicPlayerService?) {
    if (service?.episode?.imageURL != null) {
        Image(
            painter = rememberAsyncImagePainter(service.episode!!.imageURL),
            contentDescription = null,
            modifier = Modifier.size(128.dp)
        )
    } else {
        Icon(Icons.Filled.LibraryMusic, "No Art", Modifier.size(128.dp))
    }
}

