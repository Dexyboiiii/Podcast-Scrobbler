package com.morrisonhowe.podcastscrobbler

import android.content.ComponentName
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.common.util.concurrent.MoreExecutors
import com.morrisonhowe.podcastscrobbler.playerservice.MusicPlayerService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PodcastScrobbler( player: Player? ) {
    val navController = rememberNavController()
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(SheetState(skipPartiallyExpanded = true, skipHiddenState = true, initialValue = SheetValue.Expanded));
    val context = LocalContext.current
    var pm = remember { PodcastsManager(context) }
    var podcastsSaved = remember { pm.savedPodcasts }

    BottomSheetScaffold(
        topBar = {
            SmallTopAppBar(title = { Text(text = "Podcasts") }, actions = {
                IconButton(
                    onClick = { /*TODO*/ }) {
                    Icon(Icons.Filled.Remove, contentDescription = "Remove")
                }
                Button(onClick = {
                    pm.retrievePodcasts()
                }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Refresh")

                }
            })
        },
        sheetContent = { Controls(player, bottomSheetScaffoldState) },
        scaffoldState = bottomSheetScaffoldState,
        sheetSwipeEnabled = false,
        sheetDragHandle = { }

    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.PODCASTS_LIST.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = Screen.PODCASTS_LIST.name) {
                PodcastsList(podcastsSaved, pm, navController)
            }
            composable(route = Screen.ADD_PODCAST.name) {
                AddPodcast(pm, navController)
            }
            composable(route = "${Screen.EPISODES_LIST.name}/{podcastTitle}", arguments = listOf(
                navArgument("podcastTitle") { type = NavType.StringType }
            )) {backStackEntry ->
                EpisodesList(backStackEntry.arguments?.getString("podcastTitle"), podcastsSaved, navController)
            }
            composable(route = "${Screen.EPISODE.name}/{podcastTitle}/{episodeTitle}", arguments = listOf(
                navArgument("podcastTitle") { type = NavType.StringType },
                navArgument("episodeTitle") { type = NavType.IntType }
            )) {backStackEntry ->
                EpisodeDetails(player, backStackEntry.arguments?.getString("podcastTitle"), backStackEntry.arguments?.getInt("episodeTitle"), podcastsSaved, navController)
            }
        }
    }
}

