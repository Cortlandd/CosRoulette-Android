package com.cosroulette.android.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.cosroulette.android.R
import kotlinx.android.synthetic.main.activity_submit_content.*
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import android.app.Activity
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import android.view.ViewGroup
import android.view.MotionEvent
import android.widget.ScrollView
import android.widget.EditText
import android.view.View
import android.view.View.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.cosroulette.android.networking.NetworkManager
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import java.util.regex.Pattern


class SubmitContentActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.cosroulette.android.R.layout.activity_submit_content)

        submit_content_url.setOnFocusChangeListener { v, hasFocus ->
            val inputManager: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(submit_content_url.windowToken, InputMethodManager.SHOW_FORCED)
        }

        val mURLTextWatcher = object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {
                validateUrl()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateUrl()
            }
        }
        submit_content_url.addTextChangedListener(mURLTextWatcher)

        ArrayAdapter.createFromResource(
                this,
                R.array.cos_categories_array,
                android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            submit_content_categories.adapter = adapter
        }
        submit_content_categories.onItemSelectedListener = this

        submit_content_submit_button.setOnClickListener {

            submit_content_progressbar.visibility = VISIBLE
            submit_content_submit_button.visibility = GONE

            val videoId = getVideoIdFromYoutubeUrl(submit_content_url.text.toString())
            val category = submit_content_categories.selectedItem.toString()

            var status: Int

            doAsync {
                status = NetworkManager.submissionApproval(videoId!!, category)

                uiThread {
                    if (status != 200) {
                        failedSubmission()
                    } else {
                        successfulSubmission()
                    }
                }
            }
        }
        successfulSubmission()

        close_submit_content_fab.setOnClickListener {
            finish()
        }
    }

    private fun validateUrl(): Boolean {

        return if (isYoutubeUrl(submit_content_url.text.toString())) {
            valid_youtube_url_text.visibility = VISIBLE
            invalid_youtube_url_text.visibility = GONE
            submit_content_submit_button.isEnabled = true
            true
        } else {
            invalid_youtube_url_text.visibility = VISIBLE
            valid_youtube_url_text.visibility = GONE
            submit_content_submit_button.isEnabled = false
            false
        }

    }

    private fun validateCategory(): Boolean {

        return if (submit_content_categories.selectedItemPosition != 0) {
            submit_content_submit_button.isEnabled = true
            true
        } else {
            submit_content_submit_button.isEnabled = false
            false
        }

    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        parent?.getItemAtPosition(position)
    }

    // TODO: People WILL want to know if their content is successfully submitted.
    private fun successfulSubmission() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Content Submission")
                .setMessage("Your content has been submitted.")
                .setIcon(R.drawable.ic_checkmark)
                .setPositiveButton("OK") { dialoginterface, i ->
                    finish() // Close activity to reduce spam. And submitting content costs an Ad
                }.create().show()
    }

    private fun failedSubmission() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Content Submission")
                .setMessage("There was an issue submitting your content.").setIcon(android.R.drawable.ic_delete)
                .setPositiveButton("OK") { dialoginterface, i ->
                    submit_content_progressbar.visibility = GONE
                    submit_content_submit_button.visibility = VISIBLE
                    dialoginterface.dismiss()
                }.create().show()
    }

    override fun onDestroy() {
        super.onDestroy()
        finish()
    }

    private fun isYoutubeUrl(youTubeURl: String): Boolean {
        val success: Boolean
        val pattern = "^(http(s)?:\\/\\/)?((w){3}.)?youtu(be|.be)?(\\.com)?\\/.+"
        success = !youTubeURl.isEmpty() && youTubeURl.matches(pattern.toRegex())
        return success
    }

    fun getVideoIdFromYoutubeUrl(youtubeUrl: String): String? {
        /*
           Possibile Youtube urls.
           http://www.youtube.com/watch?v=WK0YhfKqdaI
           http://www.youtube.com/embed/WK0YhfKqdaI
           http://www.youtube.com/v/WK0YhfKqdaI
           http://www.youtube-nocookie.com/v/WK0YhfKqdaI?version=3&hl=en_US&rel=0
           http://www.youtube.com/watch?v=WK0YhfKqdaI
           http://www.youtube.com/watch?feature=player_embedded&v=WK0YhfKqdaI
           http://www.youtube.com/e/WK0YhfKqdaI
           http://youtu.be/WK0YhfKqdaI
        */
        val pattern = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\u200C\u200B2F|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*"
        val compiledPattern = Pattern.compile(pattern)
        //url is youtube url for which you want to extract the id.
        val matcher = compiledPattern.matcher(youtubeUrl)
        return if (matcher.find()) {
            matcher.group()
        } else null
    }

}
