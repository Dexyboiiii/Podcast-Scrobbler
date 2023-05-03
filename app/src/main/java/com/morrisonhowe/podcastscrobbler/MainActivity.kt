package com.morrisonhowe.podcastscrobbler

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.mutableStateOf
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import com.morrisonhowe.podcastscrobbler.playerservice.MusicPlayerService
import com.morrisonhowe.podcastscrobbler.ui.theme.AppTheme

enum class Screen() {
    PODCASTS_LIST,
    EPISODES_LIST,
    EPISODE,
    ADD_PODCAST
}

class MainActivity : ComponentActivity() {

    var service: MusicPlayerService? = null
    var bound = false

    /** Defines callbacks for service binding, passed to bindService().  */
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance.
            val binder = service as MusicPlayerService.LocalBinder
            this@MainActivity.service = binder.getService()
            bound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            bound = false
        }
    }

    override fun onStart() {
        super.onStart()

        Intent(this, MusicPlayerService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        setContent {
            AppTheme {
                PodcastScrobbler(service)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        unbindService(connection)
        bound = false
    }
}