package com.morrisonhowe.podcastscrobbler


import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.morrisonhowe.podcastscrobbler.parser.rssToClass
import com.morrisonhowe.podcastscrobbler.types.Podcast
import kotlinx.coroutines.launch

@Preview
@Composable
fun Preview() {
    AddPodcast(PodcastsManager(LocalContext.current), rememberNavController())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPodcast(pm: PodcastsManager, navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var rssURL by remember { mutableStateOf("") }
    val (rssPodcast, setRssPodcast) = remember { mutableStateOf<Podcast>(Podcast()) }
    val (rssLoading, setRssLoading) = remember { mutableStateOf(false) }

    Column {
        Text(text = "Add Podcast", fontSize = 30.sp)


        OutlinedTextField(
            rssURL,
            { rssURL = it },
            label = { Text("RSS Feed URL") },
            singleLine = true
        )

        Button(onClick = { coroutineScope.launch { getRssFeed(context, rssURL, setRssPodcast, setRssLoading) } }) {
            if (rssLoading) { CircularProgressIndicator(color = MaterialTheme.colorScheme.background) } else { Text("OK") }
        }

        Button(onClick = { pm.addPodcast(rssPodcast); navController.popBackStack() }, enabled = rssPodcast.episodes.isNotEmpty()) {
            Text("Add")
        }

        Column(Modifier.verticalScroll(rememberScrollState(), enabled = true)) {
            for (episode in rssPodcast.episodes) {
                EpisodeCard(episode = episode, navController = navController)
            }
        }


    }
}

suspend fun getRssFeed(context: Context, url: String, setRssPodcast: (Podcast) -> Unit, setRssLoading: (Boolean) -> Unit) {
    println("Making RSS Request!")
    setRssLoading(true)
    val queue = Volley.newRequestQueue(context)
    var res: String

    val request = StringRequest(url, { response ->
        res = response.toString()
        println("Successful! \n $response")
        setRssPodcast(rssToClass(res))
        setRssLoading(false)
    }, { res -> println(res.message)
        setRssLoading(false)})

    queue.add(request)
}

