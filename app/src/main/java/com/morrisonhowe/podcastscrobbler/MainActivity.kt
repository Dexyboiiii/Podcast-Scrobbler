package com.morrisonhowe.podcastscrobbler

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*

enum class Screen() {
    PODCASTS_LIST,
    EPISODES_LIST,
    EPISODE,
    ADD_PODCAST
}

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                PodcastScrobbler()
            }
        }
    }
}