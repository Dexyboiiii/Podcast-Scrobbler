package com.morrisonhowe.podcastscrobbler.playerservice

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.morrisonhowe.podcastscrobbler.types.Episode
import com.morrisonhowe.podcastscrobbler.types.Track
import com.morrisonhowe.podcastscrobbler.types.TracklistParseState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class MusicPlayerService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    lateinit var player: Player
    var episode: Episode? = null

    // Int of how long the track has been played for. Set to -1 when track is scrobbled
    var tracksThisSession: MutableMap<Track, Int>? = null
    private val binder = LocalBinder()
    val isPlaying: Boolean
        get() = player.isPlaying

    // Keeps episode title so that loop() is only called once per episode
    var loopEpisodeName: String? = null

    val currentTrack: Track?
        get() = episode?.getTrackAtTime((player.currentPosition).toInt())

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build()
        mediaSession = MediaSession.Builder(this, player).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

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
            episode =
                Json.decodeFromString<Episode>(string = intent.extras?.getString("episode") as String)
        } else {
            println("Needs valid episode and player")
        }

        if (episode?.audioURL != null) {
            player.setMediaItem(MediaItem.fromUri(episode!!.audioURL))

            println("Playing episode from URI ${episode!!.audioURL}")

            player.prepare()

            player.playWhenReady = true;
        }
    }

    fun interpolateTrackPositions() {
        if (player == null || episode == null || episode?.tracklistParseState == TracklistParseState.FAILED) {
            return
        }

        if (episode?.tracklistParseState == TracklistParseState.UNPARSED) {
            episode?.parseDescription()
        }

        val duration = player.duration

        val amountOfTracks = episode!!.tracks.size

        // If the amount is less than 5 then something has certainly gone wrong
        // If the duration is -9223372036854775807 then the length has not yet been set
        if (amountOfTracks < 5 || duration == -9223372036854775807) {
            return
        }

        println("Duration: $duration, amountOfTracks: $amountOfTracks")

        for ((index, track) in episode!!.tracks.withIndex()) {
            val timeTrackStarts = (duration / amountOfTracks) * index
            track.timestamp = timeTrackStarts
            episode!!.tracks[index] = track
            print(track.toString())
        }

        episode?.tracklistParseState = TracklistParseState.PARSED_WITH_TIMES
        // if the loop has been established for this episode
        if (loopEpisodeName == episode?.title) {
            return
        } else {
            // startTrackingLoop()
        }
    }

//    fun startTrackingLoop() {
//        CoroutineScope(IO).launch {
//            delay(1000)
//            if (player.isPlaying) {
//                CoroutineScope(Main).launch {
//                    if (tracksThisSession?.get(currentTrack) != null) {
//                        tracksThisSession!![currentTrack!!] = tracksThisSession!![currentTrack]!! + 1
//                        var trackLength: Long = episode!!.tracks[episode!!.tracks.indexOf(currentTrack)+1].timestamp - currentTrack!!.timestamp
//                        println("${tracksThisSession!![currentTrack!!]}/${trackLength}")
//                        if (tracksThisSession!![currentTrack!!]!! > trackLength) {
//                            println("Track has been sufficiently played! It's scrobblin' time")
//                        }
//                    }
//                    startTrackingLoop()
//                }
//            }
//        }
//    }

    fun play() {
        player.play()
    }

    fun pause() {
        player.pause()
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    inner class LocalBinder : Binder() {
        fun getService(): MusicPlayerService = this@MusicPlayerService
    }

    override fun onBind(intent: Intent?): IBinder? {
        super.onBind(intent)
        return binder
    }
}
