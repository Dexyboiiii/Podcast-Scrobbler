package com.morrisonhowe.podcastscrobbler.playerservice

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.morrisonhowe.podcastscrobbler.R
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
    private var tracksThisSession: MutableMap<Track, Int> = mutableMapOf<Track, Int>()
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

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Creating a notification channel if one doesn't already exist
        val notificationChannel = NotificationChannel(
            "podcast_playback",
            "Podcast Playback",
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(notificationChannel)

        // Building a notification with the notificationChannel id
        val notificationBuilder = NotificationCompat.Builder(this, notificationChannel.id)
            .setSmallIcon(androidx.media3.session.R.drawable.media_session_service_notification_ic_music_note)
            .setContentTitle(episode?.title)
        val notification = notificationBuilder.build()

        // Instructing Android to start a foreground service with the notification
        startForeground(1, notification)

        if (episode?.audioURL != null) {
            player.setMediaItem(MediaItem.fromUri(episode!!.audioURL))

            println("Playing episode from URI ${episode!!.audioURL}")

            player.prepare()

            player.playWhenReady = true

            player.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)
                    var playing = playbackState == Player.STATE_READY && isPlaying
                    if (episode?.tracklistParseState != TracklistParseState.PARSED_WITH_TIMES) {
                        interpolateTrackPositions()
                    }
                }

                override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                    println("Play when ready changed to $playWhenReady!")
                    super.onPlayWhenReadyChanged(playWhenReady, reason)
                }
            })

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
        startTrackingLoop()
    }

    fun startTrackingLoop() {
        CoroutineScope(Main).launch {
            while (true) {
                delay(1000)
                if (player.isPlaying) {
                    val ct = currentTrack
                    println("${ct?.title} - ${ct?.artist}")
                    if (ct != null) {
                        var trackTime =
                            tracksThisSession.computeIfAbsent(ct) { 0 } + 1000
                        tracksThisSession.put(ct, trackTime)
                        val trackLength: Long? =
                            (episode?.tracks?.get(episode?.tracks!!.indexOf(ct) + 1)?.timestamp
                                    )?.minus(currentTrack!!.timestamp)
                        println("${trackTime}/${trackLength}")
                        if (trackTime > trackLength!! / 2) {
                            println("Track has been sufficiently played!")
                        }
                        postNotification(ct)
                    }
                }
            }
        }
    }

    fun postNotification(track: Track) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationBuilder = NotificationCompat.Builder(this, "podcast_playback")
            .setSmallIcon(androidx.media3.session.R.drawable.media_session_service_notification_ic_music_note)
            .setContentTitle("${track.title} - ${track.artist}")

        notificationManager.notify(1, notificationBuilder.build())
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
