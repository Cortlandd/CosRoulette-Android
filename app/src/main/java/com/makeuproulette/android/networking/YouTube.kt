package com.makeuproulette.android.networking


import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.result.Result
import com.makeuproulette.android.data.YouTubeSearchResponse
import com.makeuproulette.android.data.YouTubeVideoResponse
import java.util.*
import kotlin.collections.ArrayList

class YouTube {

    companion object {

        const val API_KEY = "AIzaSyB3sP8V6Ufg0BUaf7YntWUv1aygEAP2lfQ"

        const val YOUTUBE_SEARCH_URL = "https://www.googleapis.com/youtube/v3/search"

        const val YOUTUBE_VIDEO_URL = "https://www.googleapis.com/youtube/v3/videos"

        fun search_youtube(search_params: List<Pair<String, Any?>>): ArrayList<MutableMap<String, Any>> {

            // List of videos returned
            var videosArray = arrayListOf(mutableMapOf<String, Any>())
            var mVideosArray = mutableMapOf<String, Any>()

            // HTTP Request. Calling on NONE blocking mode in
            val (request, response, result) = Fuel.get(YOUTUBE_SEARCH_URL, search_params).responseObject<YouTubeSearchResponse>()

            // Log the requested link
            Log.i("Request URL", request.url.toString())

            when (result) {
                is Result.Success -> {
                    println("Success ")
                    result.get().items.forEach {
                        mVideosArray = mutableMapOf(
                                "videoId" to it.id.videoId,
                                "title" to it.snippet.title,
                                "thumbnail" to it.snippet.thumbnails.default.url,
                                "channelTitle" to it.snippet.channelTitle
                        )
                        videosArray.addAll(listOf(mVideosArray))
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