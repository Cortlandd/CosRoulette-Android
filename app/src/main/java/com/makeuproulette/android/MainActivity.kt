package com.makeuproulette.android

import android.arch.lifecycle.Lifecycle
import android.database.DataSetObserver
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.NavigationView
import android.support.v4.app.DialogFragment
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import com.pierfrancescosoffritti.androidyoutubeplayer.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.player.YouTubePlayerView
import com.pierfrancescosoffritti.androidyoutubeplayer.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.player.listeners.YouTubePlayerListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.doAsyncResult
import org.jetbrains.anko.uiThread
import org.jetbrains.anko.videoView
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, NewFilterDialogFragment.NewFilterDialogListener, View.OnClickListener {

    // TODO: Resolve issue of changing orientation removing all filters.

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

    override fun onDialogPositiveClick(dialog: DialogFragment, filter: String) {

        if ("newfilter" == dialog.tag) {
            filterListItems.add(filter)
            listAdapter?.notifyDataSetChanged()

            Snackbar.make(fab, "Filter Added", Snackbar.LENGTH_LONG).setAction("Action", null).show()
        } else if ("updatefilter" == dialog.tag) {
            filterListItems[selectedItem] = filter

            listAdapter?.notifyDataSetChanged()

            selectedItem = -1

            Snackbar.make(fab, "Filter Updated", Snackbar.LENGTH_LONG).setAction("Action", null).show()
        }

    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        Snackbar.make(fab, "Cancelled", Snackbar.LENGTH_LONG).setAction("Action", null).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        initializeWidgets()
        initYoutubePlayerView()

        searchButton = findViewById(R.id.search_button)
        searchButton?.setOnClickListener(this)
        listView = findViewById(R.id.filter_list)
        listAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, filterListItems)
        listView?.adapter = listAdapter
        listView?.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id -> showUpdateFilterUI(position) }

        // Add "tutorials" to filter list by default
        filterListItems.add("tutorial")

    }

    fun initYoutubePlayerView() {

        playerView?.let { lifecycle.addObserver(it) }

        playerView = findViewById(R.id.player_view)

        playerView?.initialize({ youTubePlayer ->
            youTubePlayer.addListener(object: AbstractYouTubePlayerListener() {
                override fun onReady() {
                    super.onReady()
                    youTubePlayer.cueVideo("", 0f)
                    initializedYouTubePlayer = youTubePlayer
                }
            })
        }, true)

    }

    override fun onClick(v: View?) {

        if (v == searchButton) {

            allFiltersStringText = filterListItems.joinToString(" ")
            Log.i("R allFiltersStringText", allFiltersStringText)

            var search_params = listOf(
                    "q" to "makeup tutorials $allFiltersStringText",
                    "part" to "id, snippet",
                    "key" to YouTube.API_KEY,
                    "safeSearch" to "none",
                    "type" to "video"
            )

            if (youtubeArray.isEmpty()) {

                doAsyncResult {
                    val r = YouTube.search_youtube(search_params)
                    uiThread {
                        youtubeArray.addAll(r)
                        var randomVid: String = youtubeArray[Random().nextInt(youtubeArray.size)]
                        initializedYouTubePlayer!!.loadVideo(randomVid, 0f)
                        System.out.println("Playing Video: " + randomVid)
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
                var randomVid: String = youtubeArray[Random().nextInt(youtubeArray.size)]

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

    fun initializeWidgets() {
        fab.setOnClickListener { showNewFilterUI() }

        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
    }

    fun showNewFilterUI() {
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
                selectedItem = -1
                Snackbar.make(fab, "Filter removed", Snackbar.LENGTH_LONG).setAction("Action", null).show()
            } else if (R.id.action_settings == item.itemId) {
                return true
            }
        }
        return super.onOptionsItemSelected(item)

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_camera -> {
                // Handle the camera action
            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_manage -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}
