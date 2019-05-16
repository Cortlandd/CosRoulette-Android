package com.makeuproulette.android.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.makeuproulette.android.R
import com.makeuproulette.android.database.model.BookmarkModel
import kotlinx.android.synthetic.main.bookmark_layout.view.*

class BookmarkAdapter(val context: Context, val mBookmarks: List<BookmarkModel>): RecyclerView.Adapter<BookmarkAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.bookmark_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mBookmarks.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var bookmark: BookmarkModel = mBookmarks[position]

        holder.mVideoIdView.text = bookmark.id
        holder.mTitleView.text = bookmark.title
        holder.mThumbnailView.text = bookmark.thumbnail
    }

    inner class ViewHolder(val mView: View): RecyclerView.ViewHolder(mView) {
        val mVideoIdView: TextView = mView.findViewById(R.id.bookmark_videoid)
        val mTitleView: TextView = mView.findViewById(R.id.bookmark_title)
        val mThumbnailView: TextView = mView.findViewById(R.id.bookmark_thumbnail)

    }
}