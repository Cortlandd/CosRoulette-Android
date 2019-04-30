package com.makeuproulette.android


import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.result.Result
import java.util.*

class YouTube {

    companion object {

        const val API_KEY = "AIzaSyB3sP8V6Ufg0BUaf7YntWUv1aygEAP2lfQ"

        const val YOUTUBE_SEARCH_URL = "https://www.googleapis.com/youtube/v3/search"

        const val YOUTUBE_VIDEO_URL = "https://www.googleapis.com/youtube/v3/videos"

        fun search_youtube(search_params: List<Pair<String, Any?>>): ArrayList<String> {

            // List of videos returned
            var videosArray = ArrayList<String>()

            // HTTP Request. Calling on NONE blocking mode in
            val (request, response, result) = Fuel.get(YOUTUBE_SEARCH_URL, search_params).responseObject<YouTubeSearchResponse>()

            // Log the requested link
            Log.i("Request URL", request.url.toString())

            when (result) {
                is Result.Success -> {
                    println("Success ")
                    result.get().items.forEach {
                        videosArray.add(it.id.videoId)
                    }
                }
                is Result.Failure -> {
                    println("Error ")
                }
            }

            return videosArray
        }
    }

    fun fetchVideoData(videoId: String) {

        var videoParams = listOf(
                "part" to "snippet",
                "id" to videoId,
                "key" to API_KEY
        )

        var (request, response, result) = Fuel.get(YOUTUBE_VIDEO_URL, videoParams).responseObject<YouTubeVideoResponse>()

        when (result) {
            is Result.Success -> {
                println("Success")
            }

            is Result.Failure -> {
                println("Error ")
            }
        }

    }

    fun fetchRelatedVideos(videoId: String) {

        var relatedVideoParams = listOf(
                "key" to API_KEY,
                "relatedToVideoId" to videoId,
                "part" to "id, snippet",
                "type" to "video"
        )

        // HTTP Request. Calling on NONE blocking mode in
        val (request, response, result) = Fuel.get(YOUTUBE_SEARCH_URL, relatedVideoParams).responseObject<YouTubeSearchResponse>()

        when (result) {

            is Result.Success -> {
                println("Success")
            }

            is Result.Failure -> {
                println("Error")
            }

        }


    }



}