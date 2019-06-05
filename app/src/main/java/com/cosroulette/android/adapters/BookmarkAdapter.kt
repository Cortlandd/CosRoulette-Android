package com.cosroulette.android.adapters

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cosroulette.android.R
import com.cosroulette.android.database.BookmarksDBHelper
import com.cosroulette.android.models.BookmarkModel

import com.cosroulette.android.fragments.BookmarksFragment.OnBookmarkInteractionListener
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.net.URL

class BookmarkAdapter(val context: Context, val mBookmarks: List<BookmarkModel>, val bookmarkListner: OnBookmarkInteractionListener): RecyclerView.Adapter<BookmarkAdapter.ViewHolder>() {

    var mOnClickListener: View.OnClickListener? = null

    init {
        mOnClickListener = View.OnClickListener {v ->
            val bookmark = v.tag as BookmarkModel
            bookmarkListner.GetVideoId(bookmark.videoId!!)
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

        holder.mTitleView.text = bookmark.title
        holder.mChannelTitleView.text = bookmark.channelTitle

        val dbHandler = BookmarksDBHelper(context, null)
        val cursor = dbHandler.getAllVideoIds()
        if (!cursor!!.moveToPosition(position)) {
            return
        }
        holder.itemView.id = cursor.getInt(cursor.getColumnIndex(BookmarksDBHelper.COLUMN_ID))
        cursor.close()


        doAsync {
            var url = URL(bookmark.thumbnail.toString())
            var bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())

            uiThread {
                holder.mThumbnailImage.setImageBitmap(bitmap)
            }

        }

        with(holder.mView) {
            tag = bookmark
            setOnClickListener(mOnClickListener)
        }
    }

    fun refreshRecyclerview() {
        notifyDataSetChanged()
    }

    inner class ViewHolder(val mView: View): RecyclerView.ViewHolder(mView) {
        val mTitleView: TextView = mView.findViewById(R.id.bookmark_title)
        val mChannelTitleView: TextView = mView.findViewById(R.id.bookmark_channel_title)
        val mThumbnailImage: ImageView = mView.findViewById(R.id.bookmark_thumbnail_image)

    }
}