package com.makeuproulette.android.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.makeuproulette.android.BuildConfig
import com.makeuproulette.android.R
import kotlinx.android.synthetic.main.activity_contact.*

class ContactActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)

        close_contact_fab.setOnClickListener {
            finish()
        }
    }
}
