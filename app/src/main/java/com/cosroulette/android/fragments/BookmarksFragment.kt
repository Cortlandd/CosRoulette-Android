package com.cosroulette.android.fragments


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

import com.cosroulette.android.R
import com.cosroulette.android.adapters.BookmarkAdapter
import com.cosroulette.android.database.BookmarksDBHelper
import com.cosroulette.android.models.BookmarkModel

/**
 * A simple [DialogFragment] subclass.
 *
 */
class BookmarksFragment : DialogFragment() {

    var closeFab: FloatingActionButton? = null

    var recyclerView: RecyclerView? = null
    var bookmarkAdapter: BookmarkAdapter? = null
    var mBookmarks: ArrayList<BookmarkModel> = ArrayList<BookmarkModel>()
    var bookmarkListener: OnBookmarkInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_bookmarks, container, false)

        closeFab = view.findViewById<FloatingActionButton>(R.id.close_bookmarks_fab)

        // RecyclerView
        recyclerView = view.findViewById<RecyclerView>(R.id.bookmark_recyclerview)
        recyclerView?.layoutManager = LinearLayoutManager(this.activity)

        // Database handler to make queries to local SQLite DB
        val dbHandler = BookmarksDBHelper(this.activity!!, null)

        // Get all data from local SQLite DB and append to mBookmarks
        mBookmarks.addAll(dbHandler.allBookmarksList())
        dbHandler.close()

        // Adapter
        bookmarkAdapter = BookmarkAdapter(this.activity!!, mBookmarks, bookmarkListener!!)
        recyclerView?.adapter = bookmarkAdapter
        bookmarkAdapter?.notifyDataSetChanged()

        val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                mBookmarks.removeAt(position)
                recyclerView!!.adapter?.notifyItemRemoved(position)
                removeBookmarkItem(viewHolder.itemView.id)
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        closeFab!!.setOnClickListener {
            dismiss()
        }


        return view

    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnBookmarkInteractionListener) {
            bookmarkListener = context
        } else {
            throw RuntimeException(context.toString() + "Must implement")
        }
    }

    override fun onDetach() {
        super.onDetach()
        // TODO: Handle data in BookmarksFragment so it doesn't constantly query SQLITE
        mBookmarks.clear()
        bookmarkListener = null
    }

    override fun onStart() {
        super.onStart()
        var dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window?.setLayout(width, height)
        }
    }

    /**
     * Used to remove swiped Bookmarks
     */
    fun removeBookmarkItem(id: Int) {
        val dbHelper = BookmarksDBHelper(context!!, null)
        var db = dbHelper.writableDatabase
        db.delete(BookmarksDBHelper.TABLE_NAME, BookmarksDBHelper.COLUMN_ID + "=" + id, null)
        db.close()
    }

    interface OnBookmarkInteractionListener {
        fun GetVideoId(videoId: String)
    }


}
