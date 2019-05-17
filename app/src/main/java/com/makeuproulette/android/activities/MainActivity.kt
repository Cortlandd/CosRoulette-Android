package com.makeuproulette.android.activities

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.os.Bundle
import com.google.android.material.navigation.NavigationView
import androidx.core.view.GravityCompat
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.VISIBLE
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import com.google.android.material.snackbar.Snackbar
import com.makeuproulette.android.utils.FullScreenHelper
import com.makeuproulette.android.fragments.NewFilterDialogFragment
import com.makeuproulette.android.R
import com.makeuproulette.android.database.BookmarksDBHelper
import com.makeuproulette.android.database.model.BookmarkModel
import com.makeuproulette.android.fragments.BookmarksFragment
import com.makeuproulette.android.networking.YouTube
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerFullScreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.loadOrCueVideo
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import org.jetbrains.anko.doAsyncResult
import org.jetbrains.anko.uiThread
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, NewFilterDialogFragment.NewFilterDialogListener, View.OnClickListener, BookmarksFragment.OnBookmarkInteractionListener {

    // Array of filters
    private var filterListItems = ArrayList<String>()
    // Search Button
    private var searchButton: Button? = null
    // Filter ListView
    private var listView: ListView? = null
    // Youtube Player View
    var playerView: YouTubePlayerView? = null
    var initializedYouTubePlayer: YouTubePlayer? = null
    // Adapter for filter list
    private var listAdapter: ArrayAdapter<String>? = null
    // Array of returned Youtube Videos
    private var youtubeArray = ArrayList<String>()
    // String representation of the filters created in the ListView. Separated with a space.
    private var allFiltersStringText: String = ""
    private var showMenuItems = false
    private var selectedItem = -1
    private var fullScreenHelper: FullScreenHelper = FullScreenHelper(this)
    private var addFilterNotice: TextView? = null
    private var bookmarkButton: ImageButton? = null
    private var tracker = YouTubePlayerTracker()
    var bookmarksFragment = BookmarksFragment()
    var fm: FragmentManager? = null

    override fun onDialogPositiveClick(dialog: androidx.fragment.app.DialogFragment, filter: String) {

        if ("newfilter" == dialog.tag) {
            filterListItems.add(filter)
            listAdapter?.notifyDataSetChanged()
            youtubeArray.clear()
            println("Cleared Youtube Array")

            // TODO: Decide if these are really necessary
            //Snackbar.make(fab, "Filter Added", Snackbar.LENGTH_LONG).setAction("Action", null).show()
        } else if ("updatefilter" == dialog.tag) {
            filterListItems[selectedItem] = filter

            listAdapter?.notifyDataSetChanged()

            selectedItem = -1

            // TODO: Decide if these are really necessary
            //Snackbar.make(fab, "Filter Updated", Snackbar.LENGTH_LONG).setAction("Action", null).show()
        }

    }

    override fun onDialogNegativeClick(dialog: androidx.fragment.app.DialogFragment) {
        // TODO: Decide if these are really necessary
        //Snackbar.make(fab, "Cancelled", Snackbar.LENGTH_LONG).setAction("Action", null).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        initializeWidgets()
        initYoutubePlayerView()
        initializeBookmarksButton()

        fm = supportFragmentManager
        searchButton = findViewById(R.id.search_button)
        searchButton?.setOnClickListener(this)
        addFilterNotice = findViewById(R.id.add_filter_notice)
        listView = findViewById(R.id.filter_list)
        listView?.emptyView = findViewById(R.id.add_filter_notice)
        listAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, filterListItems)
        listView?.adapter = listAdapter
        listView?.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id -> showUpdateFilterUI(position) }

        // Add "tutorials" to filter list by default
        //filterListItems.add("tutorial")

    }

    override fun onPause() {
        super.onPause()

        if (playerView!!.isFullScreen()) {
            playerView!!.exitFullScreen()
        }

    }

    private fun initYoutubePlayerView() {

        playerView = findViewById(R.id.player_view)
        lifecycle.addObserver(playerView!!)

        playerView?.addYouTubePlayerListener(object: AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                super.onReady(youTubePlayer)
                youTubePlayer.loadOrCueVideo(lifecycle, "", 0f)
                initializedYouTubePlayer = youTubePlayer
                initializedYouTubePlayer!!.addListener(tracker)
                addFullScreenListener()
                searchButton?.visibility = VISIBLE
            }

            override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                super.onStateChange(youTubePlayer, state)
                if (state == PlayerConstants.PlayerState.PLAYING) {
                    bookmarkValidation()
                }
            }
        })
    }

    private fun addFullScreenListener() {

        playerView!!.addFullScreenListener(object: YouTubePlayerFullScreenListener {
            override fun onYouTubePlayerEnterFullScreen() {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                fab.hide()
                supportActionBar?.hide()
                window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                fullScreenHelper.enterFullScreen()
            }

            override fun onYouTubePlayerExitFullScreen() {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                fab.show()
                supportActionBar?.show()
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                fullScreenHelper.exitFullScreen()
            }
        })
    }

    override fun onClick(v: View?) {

        if (v == bookmarkButton) {

            val videoId = tracker.videoId

            when (tracker.state) {
                PlayerConstants.PlayerState.UNSTARTED -> {
                    return
                }
                PlayerConstants.PlayerState.UNKNOWN -> {
                    return
                }
                else -> {

                    // This means that the video is already bookmarked
                    if (bookmarkButton?.isSelected == true) {
                        val dbHandler = BookmarksDBHelper(this, null)
                        dbHandler.removeBookmark(videoId!!)
                        dbHandler.close()
                        bookmarkButton?.isSelected = false
                    } else {
                        val dbHandler = BookmarksDBHelper(this, null)
                        val bookmark = BookmarkModel(videoId!!, "Test title", "Test URL")
                        dbHandler.addBookmark(bookmark)
                        dbHandler.close()
                        bookmarkButton?.isSelected = true
                    }
                }
            }

        }

        if (v == searchButton) {

            bookmarkButton?.isSelected = false

            allFiltersStringText = filterListItems.joinToString(" ")
            Log.i("R allFiltersStringText", allFiltersStringText)

            var searchParams = listOf(
                    "q" to "makeup tutorials $allFiltersStringText",
                    "part" to "id, snippet",
                    "key" to YouTube.API_KEY,
                    "safeSearch" to "none",
                    "type" to "video"
            )

            if (youtubeArray.isEmpty()) {

                doAsyncResult {
                    val r = YouTube.search_youtube(searchParams)
                    uiThread {
                        youtubeArray.addAll(r)
                        val randomVid: String = youtubeArray[Random().nextInt(youtubeArray.size)]
                        initializedYouTubePlayer!!.loadVideo(randomVid, 0f)
                        System.out.println("Playing Video: $randomVid")
                        // Get random element position
                        val randomVidIndex = youtubeArray.indexOf(randomVid)
                        // Remove random element from list
                        youtubeArray.removeAt(randomVidIndex)
                        System.out.println(youtubeArray.toString())
                    }
                }

            }
            if (!youtubeArray.isEmpty()) {

                // Get random string videoId from youtube array
                val randomVid: String = youtubeArray[Random().nextInt(youtubeArray.size)]

                // Play video with random array
                initializedYouTubePlayer!!.loadVideo(randomVid, 0f)
                System.out.println("Playing Video: " + randomVid)

                // Get random element position
                val randomVidIndex = youtubeArray.indexOf(randomVid)

                // Remove random element from list
                youtubeArray.removeAt(randomVidIndex)

                // Print remaining videos
                System.out.println(youtubeArray.toString())

            }

        }
    }

    private fun initializeBookmarksButton() {

        bookmarkButton = findViewById(R.id.bookmark_button)
        bookmarkButton?.setOnClickListener(this)

    }

    private fun bookmarkValidation() {

        bookmarkButton?.isSelected = false
        val dbHelper = BookmarksDBHelper(this, null)

        var currentVideo: String? = null
        if (tracker.videoId != null) {
            currentVideo = tracker.videoId
        }

        if (currentVideo != null) {
            val allBookmarks = dbHelper.allBookmarksList()
            allBookmarks.forEach {
                // Bookmark is selected state based on if current video is in table
                //bookmarkButton?.isSelected = it.id == currentVideo
                if (it.id == currentVideo) {
                    bookmarkButton?.isSelected = true
                    return
                }
            }
        }
    }

    private fun initializeWidgets() {
        fab.setOnClickListener { showNewFilterUI() }

        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
    }

    private fun showNewFilterUI() {
        val newFragment = NewFilterDialogFragment.newInstance(R.string.add_new_filter_dialog_title, null)
        newFragment.show(supportFragmentManager, "newfilter")
    }

    fun showUpdateFilterUI(selected: Int) {
        selectedItem = selected
        showMenuItems = true
        invalidateOptionsMenu()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else if(playerView!!.isFullScreen()) {
            playerView!!.exitFullScreen()
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        val inflater = menuInflater
        inflater.inflate(R.menu.main, menu)
        val editItem = menu.findItem(R.id.edit_item)
        val deleteItem = menu.findItem(R.id.delete_item)

        if (showMenuItems) {
            editItem.isVisible = true
            deleteItem.isVisible = true
        }

        return true

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        when (item.itemId) {
//            R.id.action_settings -> return true
//            else -> return super.onOptionsItemSelected(item)
//        }

        if (-1 != selectedItem) {
            if (R.id.edit_item == item.itemId) {
                val updateFragment = NewFilterDialogFragment.newInstance(R.string.update_filter_dialog_title, filterListItems[selectedItem])
                updateFragment.show(supportFragmentManager, "updatefilter")
            } else if (R.id.delete_item == item.itemId) {
                filterListItems.removeAt(selectedItem)
                listAdapter?.notifyDataSetChanged()
                youtubeArray.clear()
                println("Cleared Youtube Array")
                selectedItem = -1
                // TODO: Decide if these are really necessary
                //Snackbar.make(fab, "Filter removed", Snackbar.LENGTH_LONG).setAction("Action", null).show()
            }
        }
        return super.onOptionsItemSelected(item)

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_bookmarks -> {
                fm?.beginTransaction()
                bookmarksFragment.show(fm, "BOOKMARKS_TAG")
            }
            R.id.nav_contact -> {
                val i: Intent = Intent(this, ContactActivity::class.java)
                startActivity(i)
            }
            R.id.nav_help -> {
                val i: Intent = Intent(this, AboutActivity::class.java)
                startActivity(i)
            }
            R.id.nav_donate -> {

            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    /**
     * Do once a Bookmark is clicked/tapped.
     */
    override fun GetVideoId(videoId: String) {

        // Create Alert Dialog for bookmarks
        var options = listOf("Play Video", "Remove Video")
        var adapter = ArrayAdapter<String>(this, android.R.layout.select_dialog_item, options)
        var builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Choose")
        builder.setAdapter(adapter, object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
                when (options.get(which)) {
                    "Play Video" -> {
                        playBookmark(videoId)
                    }
                    "Remove Video" -> {
                        removeBookmark(videoId, which)
                    }
                }
            }

        }).create().show()

        println(videoId)
    }

    fun removeBookmark(videoId: String, position: Int) {
        val dbHelper = BookmarksDBHelper(this, null)
        dbHelper.removeBookmark(videoId)
        bookmarksFragment.bookmarkAdapter?.notifyItemRemoved(position)
        bookmarksFragment.bookmarkAdapter?.notifyDataSetChanged()
    }

    fun playBookmark(videoId: String) {
        initializedYouTubePlayer!!.loadOrCueVideo(lifecycle, videoId, 0f)
        bookmarksFragment.dismiss()
    }

}
