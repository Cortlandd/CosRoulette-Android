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
import org.jetbrains.anko.toast




class SubmitContentActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.cosroulette.android.R.layout.activity_submit_content)

        submit_content_url.setOnFocusChangeListener { v, hasFocus ->
            val inputManager: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(submit_content_url.windowToken, InputMethodManager.SHOW_FORCED)
        }

        var mURLTextWatcher = object : TextWatcher {

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
            if (validateUrl() and validateCategory()) {
                submit_content_progressbar.visibility = VISIBLE
                submit_content_submit_button.visibility = GONE
                toast("ITS GOOD")

                // TODO: Network Request

            } else {
                toast("ITS BAD")
            }
        }

        close_submit_content_fab.setOnClickListener {
            finish()
        }
    }

    fun validateUrl(): Boolean {

        return if (isYoutubeUrl(submit_content_url.text.toString())) {
            valid_youtube_url_text.visibility = VISIBLE
            invalid_youtube_url_text.visibility = GONE
            true
        } else {
            invalid_youtube_url_text.visibility = VISIBLE
            valid_youtube_url_text.visibility = GONE
            false
        }

    }

    fun validateCategory(): Boolean {

        return if (submit_content_categories.selectedItemPosition != 0) {
            true
        } else {
            toast("Select a Category for your Content")
            false
        }

    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        parent?.getItemAtPosition(position)
    }

    private fun alertView(message: String) {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Submit Content")
                .setMessage(message)
                .setPositiveButton("OK") { dialoginterface, i ->
                    dialoginterface.dismiss()
                }.create().show()
    }

    override fun onDestroy() {
        super.onDestroy()
        finish()
    }

    fun isYoutubeUrl(youTubeURl: String): Boolean {
        val success: Boolean
        val pattern = "^(http(s)?:\\/\\/)?((w){3}.)?youtu(be|.be)?(\\.com)?\\/.+"
        if (!youTubeURl.isEmpty() && youTubeURl.matches(pattern.toRegex())) {
            success = true
        } else {
            // Not Valid youtube URL
            success = false
        }
        return success
    }

}
