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
import androidx.fragment.app.FragmentManager
import com.google.android.material.snackbar.Snackbar
import com.cosroulette.android.BuildConfig
import com.cosroulette.android.utils.FullScreenHelper
import com.cosroulette.android.R
import com.cosroulette.android.database.BookmarksDBHelper
import com.cosroulette.android.models.BookmarkModel
import com.cosroulette.android.fragments.BookmarksFragment
import com.cosroulette.android.fragments.FiltersFragment
import com.cosroulette.android.networking.YouTube
import com.cosroulette.android.utils.FilterPreferences
import com.cosroulette.android.utils.GlobalPreferences
import com.lukedeighton.wheelview.WheelView
import com.lukedeighton.wheelview.adapter.WheelAdapter
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerFullScreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.loadOrCueVideo
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.jetbrains.anko.doAsyncResult
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, BookmarksFragment.OnBookmarkInteractionListener, AdapterView.OnItemSelectedListener, FiltersFragment.OnFilterInteractionListener {


    // Array of filters
    private var filterListItems = ArrayList<String>()
    // YouTube Player to be used to play videos.
    private var initializedYouTubePlayer: YouTubePlayer? = null
    // Array of returned Youtube Videos
    private var youtubeArray = ArrayList<String>()
    // String representation of filters from preferences
    private var allFiltersStringText: String = ""
    // Helper to manage screen orientation of YouTubePlayerView
    private var fullScreenHelper: FullScreenHelper = FullScreenHelper(this)
    // Track the state of YouTubePlayerView
    private var tracker = YouTubePlayerTracker()
    // Fragment containing Bookmarks
    private var bookmarksFragment: BookmarksFragment? = null
    // Fragment containing Filters
    private var filtersFragment: FiltersFragment? = null
    // Managing fragments
    private var fm: FragmentManager? = null
    // Result of fetched videos. Includes: videoId, channelTitle, title, and thumbnail
    private var searchResult = ArrayList<MutableMap<String, Any>>()
    // Wheel cycle spin sound effect
    private var revolverSpin: MediaPlayer? = null
    // Full Wheel spin cycle sound effect
    private var revolverFullSpin: MediaPlayer? = null
    // Preferences for Filters
    private var mFilterPreferences: FilterPreferences? = null
    // Preferences for Global Preferences
    private var mGlobalPreferences: GlobalPreferences? = null
    // Listener for changes to Filter Preferences
    private var prefListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        initializeWidgets()
        initYoutubePlayerView()
        initializeBookmarksButton()

        mFilterPreferences = FilterPreferences(this)
        mGlobalPreferences = GlobalPreferences(this)
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
        mGlobalPreferences?.getGlobalPreferences()!!.registerOnSharedPreferenceChangeListener(prefListener)

        fm = supportFragmentManager
        revolverSpin = MediaPlayer.create(applicationContext, R.raw.revolver_spin2)
        revolverFullSpin = MediaPlayer.create(applicationContext, R.raw.revolver_full_spin)
        filters_button?.setOnClickListener {
            filtersFragment = FiltersFragment()
            fm?.beginTransaction()
            filtersFragment?.show(fm, "FILTERS_TAG")
        }

        ArrayAdapter.createFromResource(
                this,
                R.array.cos_categories_array,
                android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            base_category?.adapter = adapter
        }
        base_category?.onItemSelectedListener = this
        base_category?.setSelection(mGlobalPreferences!!.getSelectedCategory())
        if (base_category?.selectedItemPosition != 0) {
            category_help_text.visibility = VISIBLE
        } else {
            category_help_text.visibility = GONE
        }
        
        wheelview?.isClickable = false
        wheelview?.adapter = object : WheelAdapter {

            override fun getDrawable(position: Int): Drawable? {
                return null
            }

            override fun getCount(): Int {
                return 6
            }
        }

        wheelview?.setOnWheelItemSelectedListener(object: WheelView.OnWheelItemSelectListener {

            override fun onWheelItemSelected(parent: WheelView?, itemDrawable: Drawable?, position: Int) {
                revolverSpin?.start()
            }

        })

        wheelview?.onWheelItemClickListener = WheelView.OnWheelItemClickListener { parent, position, isSelected ->
            if (base_category.selectedItemPosition != 0) {
                rotate()
            } else {
                toast("Select Content")
            }
        }

    }

    override fun onPause() {
        super.onPause()

        if (player_view!!.isFullScreen()) {
            player_view!!.exitFullScreen()
        }

    }

    /**
     *
     * Rotate the cylinder in 360 degrees 12 times around its center for 1 second.
     *
     */
    fun rotate() {
        val mAngleToRotate = 360f * 12 // rotate 12 rounds
        val wheelRotation: RotateAnimation = RotateAnimation(
                // From degrees
                wheelview!!.angle,
                // To degrees
                mAngleToRotate,
                // pivotX
                wheelview!!.wheelDrawable.bounds.centerX().toFloat(),
                // pivotY
                wheelview!!.wheelDrawable.bounds.centerY().toFloat()
        )
        wheelRotation.setDuration(1000) // rotate 12 rounds in 3 seconds
        wheelRotation.setInterpolator(this, android.R.interpolator.accelerate_decelerate)
        wheelview?.startAnimation(wheelRotation)

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

    /**
     *
     * Method used to initialize YouTubePlayerView.
     *
     */
    private fun initYoutubePlayerView() {

        lifecycle.addObserver(player_view!!)

        player_view?.addYouTubePlayerListener(object: AbstractYouTubePlayerListener() {

            override fun onReady(youTubePlayer: YouTubePlayer) {
                super.onReady(youTubePlayer)
                initializedYouTubePlayer = youTubePlayer
                initializedYouTubePlayer!!.addListener(tracker)
                addFullScreenListener()
                wheelview?.isClickable = true
                toast("Ready To Spin")
            }

            override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                super.onStateChange(youTubePlayer, state)
                when (state) {
                    PlayerConstants.PlayerState.UNKNOWN -> {
                        bookmark_button?.visibility = GONE
                    }
                    PlayerConstants.PlayerState.UNSTARTED -> {
                        bookmark_button?.visibility = GONE
                    }
                    else -> {
                        bookmarkValidation()
                        bookmark_button?.visibility = VISIBLE
                    }
                }
            }
        })
    }

    /**
     *
     * Method used to handle YouTubePlayerView screen orientation changes.
     *
     */
    private fun addFullScreenListener() {

        // TODO: There has to be a better way of handling fullscreen and exit fullscreen
        player_view!!.addFullScreenListener(object: YouTubePlayerFullScreenListener {
            override fun onYouTubePlayerEnterFullScreen() {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                supportActionBar?.hide()
                wheelview?.visibility = GONE
                window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                fullScreenHelper.enterFullScreen()
            }

            override fun onYouTubePlayerExitFullScreen() {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                supportActionBar?.show()
                wheelview?.visibility = VISIBLE
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                fullScreenHelper.exitFullScreen()
            }
        })
    }

    /**
     *
     * Method used to search videos on youtube using Cos Category, and Filters from perferences
     * converted to a string of filters spaced in between.
     *
     */
    private fun searchVideo() {

        bookmark_button?.isSelected = false

        val filterPrefs = mFilterPreferences!!.getFilters()
        filterPrefs?.forEach {
            filterListItems.add(it.title!!)
        }

        // Clear because sometimes the filter doesn't clear when there is 1 left.
        if (filterListItems.isEmpty()) {
            youtubeArray.clear()
            searchResult.clear()
        }

        allFiltersStringText = filterListItems.joinToString(" ")

        var baseCategoryText = ""

        if (base_category.selectedItemPosition != 0) {
            baseCategoryText = base_category?.selectedItem.toString()
        }

        println("Base Category Text: $baseCategoryText")

        val searchParams = listOf(
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

    /**
     *
     * Method used to initialize bookmark button.
     *
     */
    private fun initializeBookmarksButton() {

        bookmark_button.setOnClickListener {

            var videoId = ""
            var thumbnail = ""
            var title = ""
            var channelTitle = ""

            if (tracker.videoId == null) {
                Snackbar.make(filters_button, "Search content to save it as a bookmark", Snackbar.LENGTH_LONG).setAction("Action", null).show()
            } else {
                videoId = tracker.videoId!!
            }

            if (bookmark_button!!.isSelected and (videoId != "")) {
                val dbHandler = BookmarksDBHelper(this, null)
                dbHandler.removeBookmark(videoId)
                dbHandler.close()
                bookmark_button?.isSelected = false
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
                bookmark_button?.isSelected = true
            }
        }

    }

    private fun bookmarkValidation() {

        if (bookmark_button!!.isSelected) {
            return
        } else {
            bookmark_button?.isSelected = false
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
                //bookmark_button?.isSelected = it.id == currentVideo
                if (it.videoId == currentVideo) {
                    bookmark_button?.isSelected = true
                    return
                }
            }
        }
    }

    /**
     * Method used to initialize Navigation widgets.
     */
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
        } else if (player_view!!.isFullScreen()) {
            player_view!!.exitFullScreen()
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
        mGlobalPreferences?.setSelectedCategory(position)

        var selectedItemText = parent?.getItemAtPosition(position).toString()

        if (parent!!.getItemAtPosition(position).equals("Select Content")) {
            category_help_text.visibility = GONE
        } else {
            category_help_text.visibility = VISIBLE
        }


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
                bookmarksFragment = BookmarksFragment()
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
            R.id.nav_sendfeedback -> {
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
                emailIntent.data = Uri.parse("mailto:admin@cosroulette.com")
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Support - Android")
                emailIntent.putExtra(Intent.EXTRA_TEXT, body)
                startActivity(Intent.createChooser(emailIntent, "Send Feedback"))
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
     *
     * Method used to handle actions when a Bookmark is clicked/tapped.
     *
     */
    override fun GetVideoId(videoId: String) {
        playBookmark(videoId)
    }

    /**
     *
     * Fetch video to play from Bookmarks page
     * @param videoId: The videoId of the Bookmark to play.
     *
     */
    fun playBookmark(videoId: String) {
        initializedYouTubePlayer!!.loadOrCueVideo(lifecycle, videoId, 0f)
        bookmarkValidation()
        bookmarksFragment?.dismiss()

    }

    override fun onFragmentInteraction(uri: Uri) {

    }

}
