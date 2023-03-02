package com.morrisonhowe.podcastscrobbler

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import com.morrisonhowe.podcastscrobbler.types.Podcast
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class PodcastsManager(context: Context) {
    var savedPodcasts: SnapshotStateList<Podcast>
    var context: Context = context

    init {
        savedPodcasts = retrievePodcasts()
    }

    fun retrievePodcasts(): SnapshotStateList<Podcast> {
        val fileDir = context.filesDir
        if (File("$fileDir/podcasts.json").isFile) {
            try {
                return Json.decodeFromString<List<Podcast>>(
                    string = File("$fileDir/podcasts.json").readText(
                        Charsets.UTF_8
                    )
                )
                    .toMutableStateList()
            } catch (e: Exception) {
                println(e.message)
            }
        }

        try {
            File("$fileDir/podcasts.json").createNewFile()
        } catch (e: Exception) {
            println(e.message)
        }
        return mutableStateListOf<Podcast>()
    }

    fun addPodcast(podcast: Podcast) {
        val fileDir = context.filesDir

        for (podcastElement in savedPodcasts) {
            if (podcastElement.title == podcast.title) {
                println("$podcastElement already exists in stored podcasts.")
                return
            }
        }

        savedPodcasts.add(podcast)

        File("$fileDir/podcasts.json").writeText(Json.encodeToString(savedPodcasts.toList()))
    }

    fun removePodcast(podcast: Podcast) {
        val fileDir = context.filesDir

        savedPodcasts.removeIf { it.title == podcast.title }

        File("$fileDir/podcasts.json").writeText(Json.encodeToString(savedPodcasts.toList()))
    }
}