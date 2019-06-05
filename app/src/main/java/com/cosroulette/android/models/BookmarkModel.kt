package com.cosroulette.android.models

class BookmarkModel {

    var videoId: String? = null
    var title: String? = null
    var thumbnail: String? = null
    var channelTitle: String? = null

    constructor(videoId: String, title: String, thumbnail: String, channelTitle: String) {
        this.videoId = videoId
        this.title = title
        this.thumbnail = thumbnail
        this.channelTitle = channelTitle
    }

    constructor()

}