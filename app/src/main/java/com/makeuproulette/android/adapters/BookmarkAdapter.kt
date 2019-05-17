package com.makeuproulette.android.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.makeuproulette.android.R
import com.makeuproulette.android.database.model.BookmarkModel
import com.makeuproulette.android.fragments.BookmarksFragment

import com.makeuproulette.android.fragments.BookmarksFragment.OnBookmarkInteractionListener
import kotlinx.android.synthetic.main.bookmark_layout.view.*

class BookmarkAdapter(val context: Context, val mBookmarks: List<BookmarkModel>, val bookmarkListner: OnBookmarkInteractionListener): RecyclerView.Adapter<BookmarkAdapter.ViewHolder>() {

    var mOnClickListener: View.OnClickListener? = null

    init {
        mOnClickListener = View.OnClickListener {v ->
            val bookmark = v.tag as BookmarkModel
            bookmarkListner.GetVideoId(bookmark.id!!)
        }
    }

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

        with(holder.mView) {
            tag = bookmark
            setOnClickListener(mOnClickListener)
        }
    }

    inner class ViewHolder(val mView: View): RecyclerView.ViewHolder(mView) {
        val mVideoIdView: TextView = mView.findViewById(R.id.bookmark_videoid)
        val mTitleView: TextView = mView.findViewById(R.id.bookmark_title)
        val mThumbnailView: TextView = mView.findViewById(R.id.bookmark_thumbnail)

    }
}