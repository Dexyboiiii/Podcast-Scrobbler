package com.morrisonhowe.podcastscrobbler.types

import kotlinx.serialization.Serializable

@Serializable
class Track {
    var artist: String
        private set
    var title: String
        private set
    private var label: String? = null
    var timestamp = 0
        private set

    constructor(artist: String, name: String) {
        this.artist = artist
        title = name
    }

    constructor(artist: String, name: String, label: String?, timestamp: Int) {
        this.artist = artist
        title = name
        this.label = label
        this.timestamp = timestamp
    }

    override fun toString(): String {
        return """
               Artist:    $artist
               Track:     $title
               Label:     $label
               """.trimIndent()
    }
}