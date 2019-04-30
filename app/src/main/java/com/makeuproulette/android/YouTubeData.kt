package com.makeuproulette.android

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson

data class YouTubeSearchResponse(val items: Array<Items>) {

    class Deserializer: ResponseDeserializable<YouTubeSearchResponse> {
        override fun deserialize(content: String): YouTubeSearchResponse? = Gson().fromJson(content, YouTubeSearchResponse::class.java)
    }


    data class Items(val id: Id) {

        class Deserializer: ResponseDeserializable<Items> {
            override fun deserialize(content: String): Items? = Gson().fromJson(content, Items::class.java)
        }

    }

    data class Id(var videoId: String) {

        class Deserializer: ResponseDeserializable<Id> {
            override fun deserialize(content: String): Id? = Gson().fromJson(content, Id::class.java)
        }

    }
}


/**
 * Response represents Youtube Video list API Response.
 *
 * Information from: https://developers.google.com/youtube/v3/docs/videos/list
 */
data class YouTubeVideoResponse(val items: Array<Items>) {

    class Deserializer: ResponseDeserializable<YouTubeVideoResponse> {
        override fun deserialize(content: String): YouTubeVideoResponse? = Gson().fromJson(content, YouTubeVideoResponse::class.java)
    }

    data class Items(val snippet: Snippet) {

        class Deserializer: ResponseDeserializable<Items> {
            override fun deserialize(content: String): Items? = Gson().fromJson(content, Items::class.java)
        }
    }

    data class Snippet(val title: String, val description: String, val tags: Array<String>) {

        class Deserializer: ResponseDeserializable<Snippet> {
            override fun deserialize(content: String): Snippet? = Gson().fromJson(content, Snippet::class.java)
        }

    }

}