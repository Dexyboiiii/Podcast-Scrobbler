package com.morrisonhowe.podcastscrobbler

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.morrisonhowe.podcastscrobbler.types.Podcast
import com.morrisonhowe.podcastscrobbler.utilities.PodcastsManager

@Composable
fun PodcastsList(podcasts: SnapshotStateList<Podcast>, pm: PodcastsManager, navController: NavController) {
    val ( remove, setRemove ) = remember { mutableStateOf(false) }

    Column(Modifier.width(IntrinsicSize.Max)) {
        Row(modifier = Modifier.padding(20.dp, 10.dp, 0.dp, 0.dp)) {
            Button(onClick = { setRemove(!remove) }) {
                if (remove) {
                    Icon(Icons.Filled.Done, contentDescription = "Finished removing")
                } else {
                    Icon(Icons.Filled.Delete, contentDescription = "Remove podcasts")
                }
            }

            Button(onClick = { navController.navigate(route = Screen.ADD_PODCAST.name) }, modifier = Modifier.padding(5.dp, 0.dp)) {
                Icon(Icons.Filled.Add, "Add podcast")
            }
        }

        // List of podcasts
        Column(Modifier.verticalScroll(rememberScrollState(), enabled = true)) {
            for (podcast in podcasts) {
                PodcastCard(podcast, remove, pm, navController)
            }
        }

    }
}

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PodcastCardPreview() {
    PodcastCard(podcast = previewPodcast[0], false, PodcastsManager(LocalContext.current), navController = rememberNavController())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PodcastCard(podcast: Podcast, remove: Boolean, pm: PodcastsManager, navController: NavController) {
    val context = LocalContext.current

    val onClickAction: () -> Unit = if (!remove) {
        { navController.navigate(route = "${Screen.EPISODES_LIST.name}/${podcast.title}") }
    } else {
        { pm.removePodcast(podcast) }
    }

    Card(onClick = onClickAction, modifier = Modifier.padding(20.dp, 10.dp).width(400.dp)) {
        Row {
            Image(
                painter = rememberAsyncImagePainter(podcast.episodes.first().imageURL),
                contentDescription = null,
                modifier = Modifier.size(128.dp)
            )

            Column(
                Modifier
                    .padding(5.dp)
                    .width(IntrinsicSize.Max)) {
                Text(podcast.title, color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 20.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(podcast.author, color = MaterialTheme.colorScheme.onSecondaryContainer, maxLines = 1, overflow = TextOverflow.Clip)
            }

            if (remove) {
                Icon(Icons.Filled.Delete, "Remove podcast")
            }

        }
    }
}