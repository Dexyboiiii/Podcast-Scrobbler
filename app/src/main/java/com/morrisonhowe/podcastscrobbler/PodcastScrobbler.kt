package com.morrisonhowe.podcastscrobbler

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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PodcastScrobbler() {
    val navController = rememberNavController()
    val context = LocalContext.current
    var pm = PodcastsManager(context)
    var podcastsSaved = remember { pm.savedPodcasts }

    Scaffold(
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
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(route = Screen.ADD_PODCAST.name) }) {
                Icon(Icons.Filled.Add, contentDescription = "Add RSS feed")
            }
        }
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
        }
    }
}

