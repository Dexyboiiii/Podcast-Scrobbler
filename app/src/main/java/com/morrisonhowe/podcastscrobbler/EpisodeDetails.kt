package com.morrisonhowe.podcastscrobbler

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.morrisonhowe.podcastscrobbler.types.Episode

@Preview
@Composable
fun EpisodeDetailsPreview() {
    EpisodeDetails(episode = previewPodcast[0].getEpisodeByIndex(100))
}

@Composable
fun EpisodeDetails(episode: Episode) {
    Column {
        EpisodeDetailsHeader(episode = episode)
    }
}

@Composable
fun EpisodeDetailsHeader(episode: Episode) {
    Row {
        Image(
            painter = rememberAsyncImagePainter(episode.imageURL),
            contentDescription = null,
            modifier = Modifier.size(128.dp)
        )

        Column {
            Column(
                Modifier
                    .padding(5.dp)
                    .width(200.dp)) {
                Text(episode.title, fontSize = 20.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(episode.date, color = MaterialTheme.colorScheme.onSecondary, maxLines = 1, overflow = TextOverflow.Clip)
            }

            Button(onClick = { /*TODO*/ }) {
                Icon(Icons.Filled.PlayArrow, "Play Episode")
            }
        }
    }
}