package com.morrisonhowe.podcastscrobbler.types

import kotlinx.serialization.Serializable

@Serializable
class Episode {
    var tracks: ArrayList<Track>

    /**
     * @param title the title to set
     */
    var title: String = ""

    /**
     * @param link the link to set
     */
    var link: String = ""
    var length // Size of file in bytes
            : String = ""
    var type // The format of the file
            : String = ""

    /**
     * @param desc the desc to set
     */
    var desc: String = ""
    var contentEncoded: String = ""

    /**
     * @param date the date to set
     */
    var date: String = ""

    /**
     * @param imageURL the imageURL to set
     */
    var imageURL: String = ""

    init {
        tracks = ArrayList()
    }

    fun insertTrack(trackToInsert: Track) {
        tracks.add(trackToInsert)
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
        if (tracks.isEmpty() == false) {
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