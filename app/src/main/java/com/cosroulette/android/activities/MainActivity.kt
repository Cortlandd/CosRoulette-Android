package com.cosroulette.android.activities

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import com.google.android.material.navigation.NavigationView
import androidx.core.view.GravityCompat
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.snackbar.Snackbar
import com.lukedeighton.wheelview.WheelView
import com.lukedeighton.wheelview.adapter.WheelAdapter
import com.cosroulette.android.BuildConfig
import com.cosroulette.android.utils.FullScreenHelper
import com.cosroulette.android.fragments.NewFilterDialogFragment
import com.cosroulette.android.R
import com.cosroulette.android.database.BookmarksDBHelper
import com.cosroulette.android.models.BookmarkModel
import com.cosroulette.android.fragments.BookmarksFragment
import com.cosroulette.android.fragments.FiltersFragment
import com.cosroulette.android.networking.YouTube
import com.cosroulette.android.utils.FilterPreferences
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerFullScreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.loadOrCueVideo
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.jetbrains.anko.doAsyncResult
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, BookmarksFragment.OnBookmarkInteractionListener, AdapterView.OnItemSelectedListener, FiltersFragment.OnFilterInteractionListener {


    // Array of filters
    private var filterListItems = ArrayList<String>()
    // Youtube Player View
    var playerView: YouTubePlayerView? = null
    var initializedYouTubePlayer: YouTubePlayer? = null
    // Array of returned Youtube Videos
    private var youtubeArray = ArrayList<String>()
    // String representation of the filters created in the ListView. Separated with a space.
    private var allFiltersStringText: String = ""
    private var showMenuItems = false
    private var selectedItem = -1
    private var fullScreenHelper: FullScreenHelper = FullScreenHelper(this)
    private var bookmarkButton: ImageButton? = null
    private var filterButton: Button? = null
    private var tracker = YouTubePlayerTracker()
    var bookmarksFragment: BookmarksFragment? = null
    var filtersFragment: FiltersFragment? = null
    var fm: FragmentManager? = null
    var searchResult = ArrayList<MutableMap<String, Any>>()
    private var wheelView: WheelView? = null
    private var revolverSpin: MediaPlayer? = null
    private var revolverFullSpin: MediaPlayer? = null
    private var baseCategorySpinner: Spinner? = null
    private var mFilterPreferences: FilterPreferences? = null
    private var prefListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        initializeWidgets()
        initYoutubePlayerView()
        initializeBookmarksButton()

        bookmarksFragment = BookmarksFragment()
        filtersFragment = FiltersFragment()

        mFilterPreferences = FilterPreferences(this)
        prefListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            when (key) {
                "filters_list" -> {
                    youtubeArray.clear()
                    searchResult.clear()
                    println("Preferences changed")
                }
            }
        }
        mFilterPreferences?.getFilterPreferences()!!.registerOnSharedPreferenceChangeListener(prefListener)

        fm = supportFragmentManager
        revolverSpin = MediaPlayer.create(applicationContext, R.raw.revolver_spin2)
        revolverFullSpin = MediaPlayer.create(applicationContext, R.raw.revolver_full_spin)
        baseCategorySpinner = findViewById(R.id.base_category)
        filterButton = findViewById(R.id.filters_button)
        filterButton?.setOnClickListener {
            fm?.beginTransaction()
            filtersFragment?.show(fm, "FILTERS_TAG")
        }

        ArrayAdapter.createFromResource(
                this,
                R.array.cos_categories_array,
                android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            baseCategorySpinner?.adapter = adapter
        }
        baseCategorySpinner?.onItemSelectedListener = this

        wheelView = findViewById(R.id.wheelview)
        wheelView?.isClickable = false
        wheelView?.adapter = object : WheelAdapter {

            override fun getDrawable(position: Int): Drawable? {
                return null
            }

            override fun getCount(): Int {
                return 6
            }
        }

        wheelView?.setOnWheelItemSelectedListener(object: WheelView.OnWheelItemSelectListener {

            override fun onWheelItemSelected(parent: WheelView?, itemDrawable: Drawable?, position: Int) {
                revolverSpin?.start()
            }

        })

        wheelView?.onWheelItemClickListener = WheelView.OnWheelItemClickListener { parent, position, isSelected ->
            rotate()
            System.out.println("Clicked")
        }

    }

    override fun onPause() {
        super.onPause()

        if (playerView!!.isFullScreen()) {
            playerView!!.exitFullScreen()
        }

    }

    /**
     * Roate the wheel image in 360 * 12 degree around the center of the wheel image in 3 seconds
     */
    fun rotate() {
        val mAngleToRotate = 360f * 12 // rotate 12 rounds
        val wheelRotation: RotateAnimation = RotateAnimation(
                // From degrees
                wheelView!!.angle,
                // To degrees
                mAngleToRotate,
                // pivotX
                wheelView!!.wheelDrawable.bounds.centerX().toFloat(),
                // pivotY
                wheelView!!.wheelDrawable.bounds.centerY().toFloat()
        )
        wheelRotation.setDuration(1000) // rotate 12 rounds in 3 seconds
        wheelRotation.setInterpolator(this, android.R.interpolator.accelerate_decelerate)
        wheelView?.startAnimation(wheelRotation)

        wheelRotation.setAnimationListener(object: Animation.AnimationListener {

            override fun onAnimationStart(animation: Animation?) {
                revolverFullSpin?.start()
            }

            override fun onAnimationRepeat(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
                searchVideo()
            }
        })

    }

    private fun initYoutubePlayerView() {

        playerView = findViewById(R.id.player_view)
        lifecycle.addObserver(playerView!!)

        playerView?.addYouTubePlayerListener(object: AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                super.onReady(youTubePlayer)
                initializedYouTubePlayer = youTubePlayer
                initializedYouTubePlayer!!.addListener(tracker)
                addFullScreenListener()
                wheelView?.isClickable = true
                toast("Ready To Spin")
            }

            override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                super.onStateChange(youTubePlayer, state)
                when (state) {
                    PlayerConstants.PlayerState.UNKNOWN -> {
                        bookmarkButton?.visibility = GONE
                    }
                    PlayerConstants.PlayerState.UNSTARTED -> {
                        bookmarkButton?.visibility = GONE
                    }
                    else -> {
                        bookmarkValidation()
                        bookmarkButton?.visibility = VISIBLE
                    }
                }
            }
        })
    }

    private fun addFullScreenListener() {

        // TODO: There has to be a better way of handling fullscreen and exit fullscreen
        playerView!!.addFullScreenListener(object: YouTubePlayerFullScreenListener {
            override fun onYouTubePlayerEnterFullScreen() {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                supportActionBar?.hide()
                wheelView?.visibility = GONE
                window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                fullScreenHelper.enterFullScreen()
            }

            override fun onYouTubePlayerExitFullScreen() {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                supportActionBar?.show()
                wheelView?.visibility = VISIBLE
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                fullScreenHelper.exitFullScreen()
            }
        })
    }

    override fun onClick(v: View?) {

        if (v == bookmarkButton) {

            var videoId = ""
            var thumbnail = ""
            var title = ""
            var channelTitle = ""

            if (tracker.videoId == null) {
                Snackbar.make(filters_button, "Search a Video to save as a Bookmark.", Snackbar.LENGTH_LONG).setAction("Action", null).show()
                return
            } else {
                videoId = tracker.videoId!!
            }

            if (bookmarkButton!!.isSelected and (videoId != "")) {
                val dbHandler = BookmarksDBHelper(this, null)
                dbHandler.removeBookmark(videoId)
                dbHandler.close()
                bookmarkButton?.isSelected = false
            } else {

                searchResult.forEach {

                    if (it.values.contains(videoId)) {

                        it.forEach { (key, value) ->

                            when (key) {
                                "thumbnail" -> {
                                    thumbnail = value.toString()
                                }
                                "title" -> {
                                    title = value.toString()
                                }
                                "channelTitle" -> {
                                    channelTitle = value.toString()
                                }
                            }

                        }
                    }

                }
                val dbHandler = BookmarksDBHelper(this, null)
                val bookmark = BookmarkModel(videoId, title, thumbnail, channelTitle)
                dbHandler.addBookmark(bookmark)
                dbHandler.close()
                bookmarkButton?.isSelected = true
            }
        }

    }

    private fun searchVideo() {

        bookmarkButton?.isSelected = false

        val filterPrefs = mFilterPreferences?.getFilters()!!
        filterPrefs.forEach {
            filterListItems.add(it.title!!)
        }

        // Clear because sometimes the filter doesn't clear when there is 1 left.
        if (filterListItems.isEmpty()) {
            youtubeArray.clear()
            searchResult.clear()
        }
        allFiltersStringText = filterListItems.joinToString(" ")
        println("\nFilters to string: $allFiltersStringText\n")

        var baseCategoryText = baseCategorySpinner?.selectedItem.toString()

        var searchParams = listOf(
                "q" to "$baseCategoryText $allFiltersStringText",
                "part" to "id, snippet",
                "key" to YouTube.API_KEY,
                "safeSearch" to "none",
                "type" to "video"
        )

        if (youtubeArray.isEmpty()) {

            doAsyncResult {
                val r = YouTube.search_youtube(searchParams)
                uiThread {
                    //youtubeArray.addAll(r)
                    searchResult.addAll(r)
                    searchResult.forEach {
                        it.forEach { (key, value) ->
                            if (key == "videoId") {
                                youtubeArray.add(value.toString())
                            }
                        }
                    }

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

    private fun initializeBookmarksButton() {

        bookmarkButton = findViewById(R.id.bookmark_button)
        bookmarkButton!!.setOnClickListener(this)

    }

    private fun bookmarkValidation() {

        if (bookmarkButton!!.isSelected) {
            return
        } else {
            bookmarkButton?.isSelected = false
        }

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
                if (it.videoId == currentVideo) {
                    bookmarkButton?.isSelected = true
                    return
                }
            }
        }
    }

    private fun initializeWidgets() {

        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        // Give navigationdrawer items color
        nav_view.itemIconTintList = null
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

    override fun onResume() {
        super.onResume()
        bookmarkValidation()
        mFilterPreferences!!.mSharedPreferences?.registerOnSharedPreferenceChangeListener(prefListener)
    }

    /**
     * Override methods for select options (Hair, Skin, Nails, and Makeup)
     */
    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (!youtubeArray.isEmpty()) {
            youtubeArray.clear()
            searchResult.clear()
        }
    }

    /**
     * Override method for top left Navigation Drawer
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_bookmarks -> {
                fm?.beginTransaction()
                bookmarksFragment?.show(fm, "BOOKMARKS_TAG")
            }
            R.id.nav_submit_content -> {
                val i: Intent = Intent(this, SubmitContentActivity::class.java)
                startActivity(i)
            }
            R.id.nav_instagram -> {
                var url: Uri = Uri.parse("https://instagram.com/cosroulette")
                var i: Intent = Intent(Intent.ACTION_VIEW, url)
                startActivity(i)
            }
            R.id.nav_sendemail -> {
                val deviceModel = Build.MODEL
                val appVersion = BuildConfig.VERSION_NAME
                val body =
                    """
                        -------------------- <br/>
                        Device: $deviceModel <br/>
                        App Version: $appVersion
                    """.trimIndent()

                val emailIntent = Intent(Intent.ACTION_SENDTO)
                emailIntent.type = "message/rfc822"
                emailIntent.data = Uri.parse("mailto:cortland12@icloud.com")
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Support - Cos Roulette")
                emailIntent.putExtra(Intent.EXTRA_TEXT, body)
                startActivity(Intent.createChooser(emailIntent, "Send feedback"))
            }
            R.id.nav_about -> {
                val i: Intent = Intent(this, AboutActivity::class.java)
                startActivity(i)
            }
            R.id.nav_donate -> {

            }
        }

        // Method to close drawer once a Navigation item is clicked
        //drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    /**
     * Do once a Bookmark is clicked/tapped.
     */
    override fun GetVideoId(videoId: String) {
        playBookmark(videoId)
    }

    /**
     * Fetch video to play from Bookmarks page
     */
    fun playBookmark(videoId: String) {
        initializedYouTubePlayer!!.loadOrCueVideo(lifecycle, videoId, 0f)
        bookmarkValidation()
        bookmarksFragment?.dismiss()

    }

    override fun onFragmentInteraction(uri: Uri) {

    }

}
