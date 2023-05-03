package com.morrisonhowe.podcastscrobbler

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.Player
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.morrisonhowe.podcastscrobbler.playerservice.MusicPlayerService
import com.morrisonhowe.podcastscrobbler.types.TracklistParseState
import com.morrisonhowe.podcastscrobbler.utilities.PodcastsManager
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PodcastScrobbler( service: MusicPlayerService? ) {
    val navController = rememberNavController()
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(SheetState(skipPartiallyExpanded = true, skipHiddenState = true, initialValue = SheetValue.Expanded));
    val context = LocalContext.current
    var pm = remember { PodcastsManager(context) }
    var podcastsSaved = remember { pm.savedPodcasts }

    var (service: MusicPlayerService?, setService) = remember { mutableStateOf<MusicPlayerService?>(null) }
    val connection = remember { object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            println("Service connected!")
            // We've bound to LocalService, cast the IBinder and get LocalService instance.
            val binder = service as MusicPlayerService.LocalBinder
            setService(binder.getService())
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            println("Service disconnected!")
        }
    }}

    val (paused, setPaused) = remember { mutableStateOf<Boolean>(true) }
    val (isPlaying, setIsPlaying) = remember { mutableStateOf<Boolean>(false) }
    val (playWhenReady, setPlayWhenReady) = remember { mutableStateOf(false) }
    val (title, setTitle) = remember { mutableStateOf<String>("") }
    val (playerResolved, setPlayerResolved) = remember { mutableStateOf(false) }


    Intent(context, MusicPlayerService::class.java).also { intent ->
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    if (service?.player != null && !playerResolved) {
        service.player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                var playing = playbackState == Player.STATE_READY && isPlaying
                println("Playback state changed to $playbackState")
                setIsPlaying( playing )
                setPaused( playing )
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                println("Play when ready changed to $playWhenReady!")
                super.onPlayWhenReadyChanged(playWhenReady, reason)
                setPlayWhenReady(playWhenReady)
            }
        })
        setPlayerResolved(true)
    }


    // If the tracks have not yet been parsed, parse them
    // Should run every composition, i.e. whenever a track starts playing, as at this point the track length is known
    if (service?.episode?.tracklistParseState == TracklistParseState.UNPARSED || service?.episode?.tracklistParseState == TracklistParseState.PARSED_WITHOUT_TIMES) {
        service.interpolateTrackPositions()
    } else if (service?.episode?.tracklistParseState == TracklistParseState.PARSED_WITH_TIMES) {
        // service.startTrackingLoop()
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            println("LaunchedEffect called!")
            delay(200)
        }
    }

    BottomSheetScaffold(
        topBar = {
            when (navController.currentDestination?.route?.substringBefore("/")) {
                Screen.PODCASTS_LIST.name -> psTopBar(title = "PodcastScrobbler", pm = pm)
                Screen.ADD_PODCAST.name -> psTopBar(title = "Add Podcast Feed", pm = pm)
                Screen.EPISODES_LIST.name -> psTopBar(title = "Episodes", pm = pm)
                Screen.EPISODE.name -> psTopBar(title = "Episode", pm = pm)
                null -> psTopBar(title = "PodcastScrobbler", pm = pm)
            }
        },
        sheetContent = { Controls(bottomSheetScaffoldState, service, title, playWhenReady, paused, setPaused) },
        sheetContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
        sheetContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        scaffoldState = bottomSheetScaffoldState,
        sheetSwipeEnabled = false,
        sheetDragHandle = { },
        containerColor = Color.Transparent

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
                EpisodeDetails(service, backStackEntry.arguments?.getString("podcastTitle"), backStackEntry.arguments?.getInt("episodeTitle"), podcastsSaved, pm, navController)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun psTopBar(title: String, pm: PodcastsManager) {
    SmallTopAppBar(title = { Text(title) }, actions = {
        Button(onClick = {
            pm.retrievePodcasts()
        }) {
            Icon(Icons.Filled.Refresh, contentDescription = "Refresh")

        }
    })
}