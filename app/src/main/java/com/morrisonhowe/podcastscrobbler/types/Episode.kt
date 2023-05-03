package com.morrisonhowe.podcastscrobbler.types

import com.morrisonhowe.podcastscrobbler.utilities.getTracks
import kotlinx.serialization.Serializable

@Serializable
class Episode(val key: Int) {
    var tracks: MutableList<Track> = mutableListOf()
    var title: String = ""
    var link: String = ""
    var length // Size of file in bytes
            : String = ""
    var type // The format of the file
            : String = ""
    var desc: String = ""
    var contentEncoded: String = ""
    var date: String = ""
    var imageURL: String = ""
    var audioURL: String = ""

    var tracklistLog: String = ""

    var tracklistParseState: TracklistParseState = TracklistParseState.UNPARSED

    fun getTrackAtTime(seconds: Int): Track? {
        for (track in tracks) {
            if (track.timestamp < seconds) {
                return track
            }
        }

        return null
    }

    fun insertTrack(trackToInsert: Track) {
        tracks.add(trackToInsert)
    }

    fun parseDescription() {
        val (tracks, tracklistParseState, tracklistLog) = getTracks(this)
        this.tracks = tracks
        this.tracklistParseState = tracklistParseState
        this.tracklistLog = tracklistLog
    }

    override fun toString(): String {
        // TODO: Include all attributes
        var strToReturn = """
               
               Title:       $title
               
               Description: $desc
               
               ContEncoded: $contentEncoded
               
               Link:        $link
               
               Length:        $length
               
               Type:        $type
               
               Image URL:   $imageURL
               """.trimIndent()
        if (tracks.isNotEmpty()) {
            strToReturn += "\n\nTracklist:\n\n"
            for (track in tracks) {
                strToReturn += """
                    $track
                    
                    
                    """.trimIndent()
            }
        } else {
            strToReturn += "No tracklist"
        }
        return strToReturn
    }
}
