package com.morrisonhowe.podcastscrobbler

import android.widget.TextView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.morrisonhowe.podcastscrobbler.types.Episode
import com.morrisonhowe.podcastscrobbler.types.Podcast
import com.morrisonhowe.podcastscrobbler.types.Track

@Preview
@Composable
fun EpisodeDetailsPreview() {
    EpisodeDetails("Mind Over Matter", 132,
        previewPodcast as SnapshotStateList<Podcast>, navController = rememberNavController())
}

@Composable
fun EpisodeDetails(podcastTitle: String?, episodeKey: Int?, podcastsSaved: SnapshotStateList<Podcast>, navController: NavController) {
    val podcast = podcastsSaved.firstOrNull { it.title == podcastTitle }
    var episode: Episode;

    if (podcast != null) {
        episode = podcast.episodes.firstOrNull { it.key == episodeKey }!!
    } else {
        Text(text = "Could not find podcast")
        return
    }

    if (episode != null) {
        Column {
            EpisodeDetailsHeader(episode = episode)

            EpisodeTabs(episode = episode)
        }
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

            Row {
                Button(onClick = { /*TODO*/ }) {
                    Icon(Icons.Filled.PlayArrow, "Play Episode")
                }
                Button(onClick = { episode.parseDescription() }) {
                    Icon(Icons.Filled.FormatListNumbered, "Parse Description")
                }
            }
        }
    }
}

@Composable
fun EpisodeTabs(episode: Episode) {
    val titles = listOf("Description", "Tracks")
    val (tabIndex, setTabIndex) = remember { mutableStateOf(0) }

    TabRow(selectedTabIndex = tabIndex) {
        titles.forEachIndexed { index, title ->
            Tab (
                text = { Text(title) },
                selected = tabIndex == index,
                onClick = { setTabIndex(index) }
            )
        }
    }

    when (tabIndex) {
        0 -> episodeDescHTML(descString = episode.contentEncoded, Modifier.padding(10.dp))
        1 -> Tracks(episode)
    }
}

@Composable
fun episodeDescHTML(descString: String, modifier: Modifier) {
    val context = LocalContext.current

    AndroidView (
        modifier = modifier,
        factory = { context -> TextView(context) },
        update = { it.text = HtmlCompat.fromHtml(descString, HtmlCompat.FROM_HTML_MODE_COMPACT) }
    )
}

@Composable
fun Tracks(episode: Episode) {
    if (episode.tracks.isEmpty()) {
        Text(text = "No tracks to display. Last parse results:\n${episode.tracklistLog}")
    } else {
        for (track in episode.tracks) {
            TrackCard(track)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackCard(track: Track) {
    Card {
        Modifier.padding(5.dp).width(IntrinsicSize.Max)
        Text(track.title, fontSize = 20.sp, maxLines = 1, overflow = TextOverflow.Clip)
        Text(track.artist, color = MaterialTheme.colorScheme.onSecondary, maxLines = 1, overflow = TextOverflow.Clip)
    }
}