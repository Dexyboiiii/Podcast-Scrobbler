package com.morrisonhowe.podcastscrobbler.parser

import com.morrisonhowe.podcastscrobbler.types.Episode
import com.morrisonhowe.podcastscrobbler.types.Track
import org.apache.commons.text.StringEscapeUtils
import java.util.regex.Matcher
import java.util.regex.Pattern


fun getTracks(episodeToParse: Episode) {
    var getTracksErrorLog = ""
    var description = episodeToParse.desc
    // If content:encoded exists, use that.
    if (episodeToParse.contentEncoded != null) {
        // Some podcasts will get better results from the description tag than content:encoded
        // If there are more linebreaks in the description tag than in content:encoded, use description
        val doNotUseContentEncoded =
            linebreakCompare(description, episodeToParse.contentEncoded)
        if (!doNotUseContentEncoded) {
            description = episodeToParse.contentEncoded
        }
    }

    getTracksErrorLog += description
    // Removes p tags and turns line breaks into.. line breaks
    description = description.replace("<br />|<br>|<br/>".toRegex(), "\n")
    description = description.replace("<.*?>".toRegex(), "")
    // For recent mind over matter podcasts
    description = description.replace("â€“".toRegex(), "-")
    description = StringEscapeUtils.unescapeXml(description)
    getTracksErrorLog += "\nPARSED\n$description"
    val splitDescription: Array<String?> =
        description.split("\\r?\\n".toRegex()).toTypedArray()
    var tracklistLength = 0
    var tracklistStart = -1
    for (i in splitDescription.indices) {
        if ((splitDescription[i]!!.contains(" - ")) or splitDescription[i]!!.contains(" – ")) {
            if (tracklistStart == -1) {
                tracklistStart = i
            }
            tracklistLength++
        } else if (tracklistLength >= 1 && splitDescription[i] == null) {
            for (j in i until splitDescription.size) {
                splitDescription[i] = splitDescription[i + 1]
            }
        } else if (tracklistLength >= 3) {
            break
        } else {
            tracklistStart = -1
            tracklistLength = 0
        }
    }
    getTracksErrorLog += """
               
               ${"Starts at line " + tracklistStart + " until line " + (tracklistStart + tracklistLength)}
               """.trimIndent()


    // If there aren't enough lines for it to look like a tracklist, give up.
    if (tracklistLength < 3) {
        getTracksErrorLog += "Not enough tracks for a proper tracklist"
        episodeToParse.tracklistLog = getTracksErrorLog
        return
    }
    val rawTracklist = arrayOfNulls<String>(tracklistLength)
    var positionInUnparsedTracklist = 0
    for (i in tracklistStart until tracklistLength + tracklistStart) {
        rawTracklist[positionInUnparsedTracklist] = splitDescription[i]
        positionInUnparsedTracklist++
    }
    for (i in rawTracklist.indices) {
        rawTracklist[i] = rawTracklist[i]!!.trim { it <= ' ' }
    }

    // If numbering is present, remove it.
    if (checkIfNumbered(rawTracklist, getTracksErrorLog)) {
        val numberedSplitPattern = Pattern.compile("(\\d+(\\.|\\))? +)+(.+)$")
        var numberedSplitMatcher: Matcher
        for (i in rawTracklist.indices) {
            numberedSplitMatcher = numberedSplitPattern.matcher(rawTracklist[i])
            numberedSplitMatcher.matches()
            try {
                rawTracklist[i] = numberedSplitMatcher.group(3)
            } catch (e: Exception) {
                getTracksErrorLog += """
                        ERROR IN REMOVING NUMBERING
                        ${rawTracklist[i]}
                        """.trimIndent()
            }
        }
    }

    // TODO: Update to Kotlin pattern matching
    val artistTrackSplitterPattern = Pattern.compile("(.+)( – | - )(.+)")
    var artistTrackSplitterMatcher: Matcher
    val labelScraperPatternPattern = Pattern.compile("(.+)( – | - )(.+)(\\[)(.+)(])")
    var labelScraperPatternMatcher: Matcher
    var trackObj: Track
    for (unsplitTrack in rawTracklist) {
        artistTrackSplitterMatcher = artistTrackSplitterPattern.matcher(unsplitTrack)
        labelScraperPatternMatcher = labelScraperPatternPattern.matcher(unsplitTrack)
        // If there is a record label present...
        if (labelScraperPatternMatcher.matches()) {
            trackObj = Track(
                labelScraperPatternMatcher.group(1),
                labelScraperPatternMatcher.group(3),
                labelScraperPatternMatcher.group(5),
                -1
            )
            episodeToParse.insertTrack(trackObj)
            // If there isn't a record label present...
        } else if (artistTrackSplitterMatcher.matches()) {
            trackObj =
                Track(artistTrackSplitterMatcher.group(1), artistTrackSplitterMatcher.group(3))
            episodeToParse.insertTrack(trackObj)
        } else {
            getTracksErrorLog += """
                    
                    Could not parse: $unsplitTrack
                    """.trimIndent()
            println(getTracksErrorLog)
        }
    }
    println(getTracksErrorLog)
    println(episodeToParse.toString())

    episodeToParse.tracklistLog = getTracksErrorLog

    // TODO: I need to check if tracks are numbered, timestamped, or neither.
    // For timestamped, I could check if there is a colon on each set of numbers, and if they're in ascending order.
}

private fun checkIfNumbered(tracksArray: Array<String?>, getTracksErrorLog: String): Boolean {
    var getTracksErrorLog: String? = getTracksErrorLog
    for (string in tracksArray) {
        // Creds to Emily for the regex
        // TODO: fix this with intothedeep.rss
        if (Pattern.matches("(\\d+(\\.|\\))? +)+(.+)$", string)) {
            getTracksErrorLog += "Failed checking if numbered"
            return true
        }
    }
    return false
}

// Compares the amount of linebreaks in two strings
fun linebreakCompare(str1: String, str2: String): Boolean {
    val linebreakCountPattern = Pattern.compile("<br />|<br>")
    var linebreakCountMatcher = linebreakCountPattern.matcher(str1)
    var str1Linebreaks = 0
    while (linebreakCountMatcher.find()) {
        str1Linebreaks++
    }
    linebreakCountMatcher = linebreakCountPattern.matcher(str2)
    var str2Linebreaks = 0
    while (linebreakCountMatcher.find()) {
        str2Linebreaks++
    }
    // Returns true if there are more linebreaks in the first string
    return str1Linebreaks > str2Linebreaks
}