package com.makeuproulette.android.fragments


import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

import com.makeuproulette.android.R
import com.makeuproulette.android.activities.MainActivity
import com.makeuproulette.android.adapters.BookmarkAdapter
import com.makeuproulette.android.database.BookmarksDBHelper
import com.makeuproulette.android.database.model.BookmarkModel
import kotlinx.android.synthetic.main.fragment_bookmarks.*

/**
 * A simple [DialogFragment] subclass.
 *
 */
class BookmarksFragment : DialogFragment() {

    var closeFab: FloatingActionButton? = null

    var recyclerView: RecyclerView? = null
    var bookmarkAdapter: BookmarkAdapter? = null
    var mBookmarks: ArrayList<BookmarkModel> = ArrayList<BookmarkModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_bookmarks, container, false)

        closeFab = view.findViewById(R.id.close_bookmarks_fab)

        // RecyclerView
        recyclerView = view.findViewById(R.id.bookmark_recyclerview)
        recyclerView?.layoutManager = LinearLayoutManager(this.activity)

        // Database handler to make queries to local SQLite DB
        val dbHandler = BookmarksDBHelper(this.activity!!, null)

        // Get all data from local SQLite DB and append to mBookmarks
        mBookmarks.addAll(dbHandler.allBookmarksList())

        // Adapter
        bookmarkAdapter = BookmarkAdapter(this.activity!!, mBookmarks)
        recyclerView?.adapter = bookmarkAdapter

        closeFab!!.setOnClickListener {
            dismiss()
        }

        return view

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


}
