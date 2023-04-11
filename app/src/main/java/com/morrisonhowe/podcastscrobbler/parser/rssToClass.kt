package com.morrisonhowe.podcastscrobbler.parser

import com.morrisonhowe.podcastscrobbler.types.Podcast
import org.apache.commons.text.StringEscapeUtils
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.xml.sax.SAXException
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException


@Throws(ParserConfigurationException::class, SAXException::class, IOException::class)
fun rssToClass(rssString: String): Podcast {
    // Initialising instance of podcast
    val podcastData = Podcast()
    val factory = DocumentBuilderFactory.newInstance()
    factory.isNamespaceAware = true
    val loader = factory.newDocumentBuilder()
    var document: Document? = null

    // Some podcasts were incorrectly formatted(specifically, had unescaped ampersands) and so would throw SAXExceptions
    // This fixes that by escaping all unescaped ampersands.
    try {
        document = loader.parse(rssString)
    } catch (e: Exception) {
        var fileContent: String = rssString
        // Find all ampersands, unless there is a character code and semicolon after (otherwise &amp;amp; ensues)
        fileContent = fileContent.replace("&(?!(\\w|#)+;)".toRegex(), "&amp;")
        val pathToFixedFeed = Files.createTempFile("fixedFeed", ".rss")
        Files.write(pathToFixedFeed, fileContent.toByteArray())
        // System.out.println("Written to path: " + pathToFixedFeed);
        try {
            document = loader.parse(String(pathToFixedFeed.toFile().readBytes(), StandardCharsets.UTF_8))
        } catch (e: Exception) {
            document = loader.parse(pathToFixedFeed.toFile())
        }

    }
    val root = document!!.documentElement
    println(root.nodeName)
    println("Root element: " + root.nodeName)
    val nList = document.getElementsByTagName("*")
    var episodeNumber = 0

    // For enclosure tags
    var url: String
    var length: String
    var type: String
    for (i in 0 until nList.length) {
        val nNode = nList.item(i)
        val elem = nNode as Element
        val text = elem.textContent.trim { it <= ' ' }
        val tag = elem.nodeName
        if (tag == "item") {
            episodeNumber++
            podcastData.insertEpisode(episodeNumber - 1)
        }
        if (episodeNumber == 0) {
            when (tag) {
                "title" -> podcastData.title = text
                "link" -> podcastData.link = text
                "itunes:author" -> podcastData.author = text
                "description" -> podcastData.podcastDesc = text
                "copyright" -> podcastData.copyright = text
            }
        } else {
            when (tag) {
                "title" -> podcastData.episodes[episodeNumber - 1].title = text
                "link" -> podcastData.episodes[episodeNumber - 1].link = text
                "enclosure" -> {
                    podcastData.episodes[episodeNumber - 1].audioURL = nNode.getAttribute("url")
                    podcastData.episodes[episodeNumber - 1].length = nNode.getAttribute("length")
                    podcastData.episodes[episodeNumber - 1].type = nNode.getAttribute("type")
                }
                "url" -> podcastData.episodes[episodeNumber - 1].imageURL = text
                "pubDate" -> podcastData.episodes[episodeNumber - 1].date = text
                "description" -> {
                    podcastData.episodes[episodeNumber - 1].desc = text
                    podcastData.episodes[episodeNumber - 1].contentEncoded = text
                    // This code is sHaKy, hence it's in a try catch block
                    try {
                        url = elem.attributes.getNamedItem("url").nodeValue
                        podcastData.episodes[episodeNumber - 1].link = url
                        length = elem.attributes.getNamedItem("length").nodeValue
                        podcastData.episodes[episodeNumber - 1].length = length
                        type = elem.attributes.getNamedItem("type").nodeValue
                        podcastData.episodes[episodeNumber - 1].type = type
                    } catch (e: Exception) {
                    }
                }
                "content:encoded" -> {
                    podcastData.episodes[episodeNumber - 1].contentEncoded = text
                    try {
                        url = elem.attributes.getNamedItem("url").nodeValue
                        podcastData.episodes[episodeNumber - 1].link = url
                        length = elem.attributes.getNamedItem("length").nodeValue
                        podcastData.episodes[episodeNumber - 1].length = length
                        type = elem.attributes.getNamedItem("type").nodeValue
                        podcastData.episodes[episodeNumber - 1].type = type
                    } catch (e: Exception) {
                    }
                }
                "enclosure" -> try {
                    url = elem.attributes.getNamedItem("url").nodeValue
                    podcastData.episodes[episodeNumber - 1].link = url
                    length = elem.attributes.getNamedItem("length").nodeValue
                    podcastData.episodes[episodeNumber - 1].length = length
                    type = elem.attributes.getNamedItem("type").nodeValue
                    podcastData.episodes[episodeNumber - 1].type = type
                } catch (e: Exception) {
                }
            }
        }
    }
    return podcastData
}