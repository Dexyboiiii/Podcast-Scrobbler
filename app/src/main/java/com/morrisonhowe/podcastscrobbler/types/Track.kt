package com.morrisonhowe.podcastscrobbler.types

import kotlinx.serialization.Serializable

@Serializable
class Track {
    var trackArtist: String
        private set
    var trackName: String
        private set
    private var trackLabel: String? = null
    var trackTimestamp = 0
        private set

    constructor(artist: String, name: String) {
        trackArtist = artist
        trackName = name
    }

    constructor(artist: String, name: String, label: String?, timestamp: Int) {
        trackArtist = artist
        trackName = name
        trackLabel = label
        trackTimestamp = timestamp
    }

    override fun toString(): String {
        return """
               Artist:    $trackArtist
               Track:     $trackName
               Label:     $trackLabel
               """.trimIndent()
    }
}