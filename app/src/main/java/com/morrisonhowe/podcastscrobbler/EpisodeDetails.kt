package com.morrisonhowe.podcastscrobbler

import android.content.Context
import android.content.Intent
import android.widget.TextView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.morrisonhowe.podcastscrobbler.playerservice.MusicPlayerService
import com.morrisonhowe.podcastscrobbler.types.Episode
import com.morrisonhowe.podcastscrobbler.types.Podcast
import com.morrisonhowe.podcastscrobbler.types.Track
import com.morrisonhowe.podcastscrobbler.utilities.PodcastsManager
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

//@Preview
//@Composable
//fun EpisodeDetailsPreview() {
//    EpisodeDetails("Mind Over Matter", 132,
//        previewPodcast as SnapshotStateList<Podcast>, navController = rememberNavController())
//}

@Composable
fun EpisodeDetails(
    service: MusicPlayerService?,
    podcastTitle: String?,
    episodeKey: Int?,
    podcastsSaved: SnapshotStateList<Podcast>,
    podcastsManager: PodcastsManager
) {

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
            EpisodeDetailsHeader(episode = episode, service = service, podcastsManager)
            EpisodeTabs(episode = episode)
        }
    }
}

@Composable
fun EpisodeDetailsHeader(episode: Episode, service: MusicPlayerService?, podcastsManager: PodcastsManager) {
    var context = LocalContext.current

    val (isPaused: Boolean?, setPaused) = remember { mutableStateOf(service?.isPlaying) }

    Row(
        Modifier
            .background(MaterialTheme.colorScheme.background)
            .width(411.dp)) {
        Image(
            painter = rememberAsyncImagePainter(episode.imageURL),
            contentDescription = null,
            modifier = Modifier.size(128.dp)
        )

        Column {
            Column(
                Modifier
                    .padding(5.dp)
                    .width(250.dp)
            ) {
                Text(
                    episode.title,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 20.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    episode.date,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
            }

            Row (modifier = Modifier.padding(10.dp, 0.dp, 0.dp, 0.dp)) {
                if (service?.player?.isPlaying == true) {
                    if (isPaused == true) {
                        Button(onClick = { service?.play(); setPaused(true) }, modifier = Modifier.padding(5.dp, 0.dp)) {
                            Icon(Icons.Filled.PlayArrow, "Resume")
                        }
                    } else {
                        Button(onClick = { service?.pause(); setPaused(false) }, modifier = Modifier.padding(5.dp, 0.dp)) {
                            Icon(Icons.Filled.Pause, "Pause")
                        }
                    }

                } else {
                    Button(onClick = { playEpisode(episode, context); setPaused(false) }, modifier = Modifier.padding(5.dp, 0.dp)) {
                        Icon(Icons.Filled.PlayArrow, "Nothing playing")
                    }
                }

                Button(onClick = { episode.parseDescription(); podcastsManager.updatePodcasts() }) {
                    Icon(Icons.Filled.FormatListNumbered, "Parse Description")
                }
            }
        }
    }
}

fun playEpisode(episode: Episode, context: Context) {
    val intent = Intent(context, MusicPlayerService::class.java)
    intent.putExtra("episode", Json.encodeToString(episode))
    intent.putExtra("action", "start")
    context.startService(intent)
}

@Composable
fun EpisodeTabs(episode: Episode) {
    val titles = listOf("Description", "Tracks")
    val (tabIndex, setTabIndex) = remember { mutableStateOf(0) }

    TabRow(selectedTabIndex = tabIndex) {
        titles.forEachIndexed { index, title ->
            Tab(
                text = { Text(title) },
                selected = tabIndex == index,
                onClick = { setTabIndex(index) }
            )
        }
    }

    when (tabIndex) {
        // 0 -> episodeDescHTML(descString = episode.contentEncoded, Modifier.padding(10.dp))
        0 -> Text(episode.desc, Modifier.padding(10.dp).scrollable(rememberScrollState(), Orientation.Vertical, enabled = true), fontSize = TextUnit(3f, TextUnitType.Em))
        1 -> Tracks(episode)
    }
}

@Composable
fun episodeDescHTML(descString: String, modifier: Modifier) {
    val context = LocalContext.current

    AndroidView(
        modifier = modifier,
        factory = { context -> TextView(context) },
        update = { it.text = HtmlCompat.fromHtml(descString, HtmlCompat.FROM_HTML_MODE_COMPACT) }
    )
}

@Composable
fun Tracks(episode: Episode) {
    if (episode.tracks.isEmpty()) {
        Text(
            text = "No tracks to display. Last parse results:\n${episode.tracklistLog}",
            Modifier.scrollable(
                rememberScrollState(), Orientation.Vertical, enabled = true
            )
        )
    } else {
        Column(Modifier.scrollable(rememberScrollState(), Orientation.Vertical, enabled = true)) {
            for (track in episode.tracks) {
                TrackCard(track)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackCard(track: Track) {
    Text(track.title, fontSize = 20.sp, maxLines = 1, overflow = TextOverflow.Clip)
    Text(
        track.artist,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        maxLines = 1,
        overflow = TextOverflow.Clip
    )
}