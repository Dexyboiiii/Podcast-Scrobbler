package com.morrisonhowe.podcastscrobbler.playerservice

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import com.morrisonhowe.podcastscrobbler.types.Episode
import com.morrisonhowe.podcastscrobbler.types.Track
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class MusicPlayerService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private lateinit var player: Player
    lateinit var episode: Episode
    val currentTrack: () -> Track?
        get() = {
            episode.getTrackAtTime((player.currentPosition/1000).toInt())
        }

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build()
        mediaSession = MediaSession.Builder(this, player).build()
    }
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        super.onStartCommand(intent, flags, startId);

        if (intent != null) {
            when (intent.extras?.getString("action")) {
                "start" -> {
                    initMediaPlayerAndPlay(intent)
                }
                "pause" -> {
                    if (player.isPlaying) {
                        player.pause()
                    } else {
                        player.play()
                    }
                }
            }
        }

        return START_STICKY;
    }

    private fun initMediaPlayerAndPlay(intent: Intent) {
        if (intent.extras?.getString("episode") != null) {
            episode = Json.decodeFromString<Episode>(string = intent.extras?.getString("episode") as String)
        } else {
            println("Needs valid episode and player")
        }

        player.setMediaItem(MediaItem.fromUri(episode.audioURL))

        println("Playing episode from URI ${episode.audioURL}")

        player.prepare()
        player.play()
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}