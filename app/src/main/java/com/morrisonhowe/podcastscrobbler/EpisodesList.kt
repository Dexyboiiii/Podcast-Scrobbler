package com.morrisonhowe.podcastscrobbler

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.morrisonhowe.podcastscrobbler.types.Episode
import com.morrisonhowe.podcastscrobbler.types.Podcast

@Composable
fun EpisodesList(
    podcastTitle: String?,
    podcastsSaved: SnapshotStateList<Podcast>,
    navController: NavController
) {
    val podcast = podcastsSaved.firstOrNull { it.title == podcastTitle }
    if (podcast != null) {
        Column(Modifier.verticalScroll(rememberScrollState(), enabled = true)) {
            for (episode in podcast.episodes) {
                EpisodeCard(podcast = podcast, episode = episode, navController = navController)
            }
        }
    } else {
        Text(text = "Could not find episodes for the selected podcast.")
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpisodeCard(podcast: Podcast?, episode: Episode, navController: NavController) {
    // If this is called from AddPodcast, then podcast will be null
    // This prevents the item from being opened, which would crash the app as the podcast has not yet been saved
    Card(onClick = {
        if (podcast != null) {
            val route: String = "${Screen.EPISODE.name}/${podcast.title}/${episode.key}"
            navController.navigate(route = route)
        }
    }, modifier = Modifier.padding(20.dp)) {
        Row {
            Image(
                painter = rememberAsyncImagePainter(episode.imageURL),
                contentDescription = null,
                modifier = Modifier.size(128.dp)
            )

            Column(Modifier.padding(5.dp)) {
                Text(episode.title, fontSize = 20.sp, maxLines = 1, overflow = TextOverflow.Clip)
                Text(
                    episode.desc,
                    color = MaterialTheme.colorScheme.onSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
            }

            Icon(Icons.Filled.ChevronRight, "Right Chevron")
        }
    }
}