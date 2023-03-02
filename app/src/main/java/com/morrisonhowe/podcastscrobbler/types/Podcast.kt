package com.morrisonhowe.podcastscrobbler.types

import com.morrisonhowe.podcastscrobbler.parser.getTracks.main
import java.util.function.UnaryOperator
import kotlinx.serialization.Serializable

@Serializable
class Podcast {
    // ArrayList of episodes in the podcast
    var episodes: ArrayList<Episode>
    var title: String = ""
    var link: String = ""
    var language: String = ""
    var copyright: String = ""
    var podcastDesc: String = ""
    var contentEncoded: String = ""
    var keywords: String = ""
    var author: String = ""
    var category: String = ""
    var isExplicit = false
    var owner: String = ""

    constructor(title: String, link: String, author: String, description: String) {
        episodes = ArrayList()
        this.title = title
        this.link = link
        this.author = author
        podcastDesc = description
    }

    constructor() {
        episodes = ArrayList()
    }

    fun insertEpisode(episode: Episode) {
        episodes.add(episode)
    }

    fun insertEpisode() {
        episodes.add(Episode())
    }

    // Used if there's a new episode, whether just new to the feed or new as in time
    fun insertEpisode(episode: Episode, index: Int) {
        episodes.add(index, episode)
    }

    fun getEpisodeByIndex(index: Int): Episode {
        return episodes[index]
    }

    fun parseAllEpisodes() {
        episodes.replaceAll(getTracksOnEpisodeUO())
    }

    override fun toString(): String {
        // TODO: Include all attributes
        var strToReturn = """
            
            Title:       $title
            
            Description: $podcastDesc
            
            Link:        $link
            
            Author:      $author
            """.trimIndent()
        var episodesToString = ""
        if (episodes.isNotEmpty()) {
            for (i in episodes.indices) {
                episodesToString += episodes[i].toString()
            }
            strToReturn += "\n\nEpisodes:    $episodesToString"
        }
        return strToReturn
    }

    internal inner class getTracksOnEpisodeUO : UnaryOperator<Episode> {
        override fun apply(e: Episode): Episode {
            return main(e)
        }
    }
}