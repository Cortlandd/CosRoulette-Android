package com.makeuproulette.android

import android.provider.Settings.Global.getString
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Parameters
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.gson.jsonBody
import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import org.json.JSONObject
import java.net.URI
import java.net.URL
import java.net.URLEncoder
import java.util.*

class YouTube {

    companion object {

        const val API_KEY = "AIzaSyB3sP8V6Ufg0BUaf7YntWUv1aygEAP2lfQ"

        const val YOUTUBE_URL = "https://www.googleapis.com/youtube/v3/search"

        fun search_youtube(search_params: List<Pair<String, Any?>>): Array<String> {

            // List of videos returned
            var videosArray = emptyArray<String>()

            // HTTP Request. Calling on NONE blocking mode in
            val (request, response, result) = Fuel.get(YOUTUBE_URL, search_params).responseObject<YouTubeResponse>()

            // Log the requested link
            Log.i("Request URL", request.url.toString())

            when (result) {
                is Result.Success -> {
                    print("Success ")
                    result.get().items.forEach {
                        Log.i("Request Id's", it.id.videoId)
                        videosArray += it.id.videoId
                    }
                }
                is Result.Failure -> {
                    print("Error ")
                }
            }

            Log.i("R Videos", Arrays.toString(videosArray))
            return videosArray
        }
    }



}