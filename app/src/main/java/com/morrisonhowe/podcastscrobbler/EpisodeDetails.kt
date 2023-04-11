package com.morrisonhowe.podcastscrobbler

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.TextView
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Pause
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
import androidx.compose.runtime.MutableState
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
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionToken
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.google.common.util.concurrent.MoreExecutors
import com.morrisonhowe.podcastscrobbler.playerservice.MusicPlayerService
import com.morrisonhowe.podcastscrobbler.types.Episode
import com.morrisonhowe.podcastscrobbler.types.Podcast
import com.morrisonhowe.podcastscrobbler.types.Track
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.io.Serializable

//@Preview
//@Composable
//fun EpisodeDetailsPreview() {
//    EpisodeDetails("Mind Over Matter", 132,
//        previewPodcast as SnapshotStateList<Podcast>, navController = rememberNavController())
//}

@Composable
fun EpisodeDetails(player: Player?, podcastTitle: String?, episodeKey: Int?, podcastsSaved: SnapshotStateList<Podcast>, navController: NavController) {
    val context = LocalContext.current

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
            EpisodeDetailsHeader(episode = episode, player = player)
            EpisodeTabs(episode = episode)
        }
    }
}

@Composable
fun EpisodeDetailsHeader(episode: Episode, player: Player?) {
    var context = LocalContext.current

    val (isPaused, setPaused) = remember { mutableStateOf(false) }

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
                    Button(onClick = { playPodcast(episode, context) }) {
                        Icon(Icons.Filled.PlayArrow, "Play Episode")
                    }
                }

                Button(onClick = { episode.parseDescription() }) {
                    Icon(Icons.Filled.FormatListNumbered, "Parse Description")
                }
            }
        }
    }
}

fun playPodcast(episode: Episode, context: Context) {
    val intent = Intent(context, MusicPlayerService::class.java)
    intent.putExtra("episode", Json.encodeToString(episode))
    intent.putExtra("action", "start")
    context.startForegroundService(intent)
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
        Text(text = "No tracks to display. Last parse results:\n${episode.tracklistLog}", Modifier.scrollable(
            rememberScrollState(), Orientation.Vertical))
    } else {
        Column(Modifier.scrollable(rememberScrollState(), Orientation.Vertical)) {
            for (track in episode.tracks) {
                TrackCard(track)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackCard(track: Track) {
    Card {
        Modifier
            .padding(5.dp)
            .width(IntrinsicSize.Max)
        Text(track.title, fontSize = 20.sp, maxLines = 1, overflow = TextOverflow.Clip)
        Text(track.artist, color = MaterialTheme.colorScheme.onSecondary, maxLines = 1, overflow = TextOverflow.Clip)
    }
}