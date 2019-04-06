package com.makeuproulette.android

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson

data class YouTubeResponse(val items: Array<Items>) {

    class Deserializer: ResponseDeserializable<YouTubeResponse> {
        override fun deserialize(content: String): YouTubeResponse? = Gson().fromJson(content, YouTubeResponse::class.java)
    }
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