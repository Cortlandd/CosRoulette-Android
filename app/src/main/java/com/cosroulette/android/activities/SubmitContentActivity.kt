package com.cosroulette.android.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.cosroulette.android.R
import kotlinx.android.synthetic.main.activity_submit_content.*
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import android.view.ViewGroup
import android.view.MotionEvent
import android.view.View.OnTouchListener
import android.widget.ScrollView
import android.widget.EditText
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter


class SubmitContentActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_submit_content)

        submit_content_url.setOnFocusChangeListener { v, hasFocus ->
            val inputManager: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(submit_content_url.windowToken, InputMethodManager.SHOW_FORCED)
        }

        submit_content_submit_button.setOnClickListener {
            alertView("Content Submitted!")
        }

        ArrayAdapter.createFromResource(
                this,
                R.array.cos_categories_array,
                android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            submit_content_categories.adapter = adapter
        }
        submit_content_categories.onItemSelectedListener = this

        close_submit_content_fab.setOnClickListener {
            finish()
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

}
