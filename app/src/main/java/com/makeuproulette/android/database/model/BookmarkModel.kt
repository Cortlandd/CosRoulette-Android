package com.makeuproulette.android.database.model

class BookmarkModel {

    var id: String? = null
    var title: String? = null
    var thumbnail: String? = null

    constructor(id: String, title: String, thumbnail: String) {
        this.id = id
        this.title = title
        this.thumbnail = thumbnail
    }

    constructor()

}