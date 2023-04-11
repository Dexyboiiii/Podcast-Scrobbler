package com.morrisonhowe.podcastscrobbler

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import com.morrisonhowe.podcastscrobbler.playerservice.MusicPlayerService

enum class Screen() {
    PODCASTS_LIST,
    EPISODES_LIST,
    EPISODE,
    ADD_PODCAST
}

class MainActivity : ComponentActivity() {

    var player: Player? = null;

    override fun onStart() {
        super.onStart()

        var sessionToken = SessionToken(this, ComponentName(this, MusicPlayerService::class.java));
        val controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()

        controllerFuture.addListener(
            { player = controllerFuture.get() },
            MoreExecutors.directExecutor()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                PodcastScrobbler(player)
            }
        }
    }
}