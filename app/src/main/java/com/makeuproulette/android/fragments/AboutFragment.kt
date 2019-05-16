package com.makeuproulette.android.fragments


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.makeuproulette.android.BuildConfig

import com.makeuproulette.android.R
import kotlinx.android.synthetic.main.fragment_about.*

/**
 * A simple [Fragment] subclass.
 *
 */
class AboutFragment : Fragment() {

    var contactVersion: TextView? = null
    var contactInstagram: ImageView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var v = inflater.inflate(R.layout.fragment_about, container, false)

        // Set version to current version
        var appVersion = BuildConfig.VERSION_NAME
        contactVersion = v.findViewById(R.id.contact_version)
        contactVersion?.text = "Version $appVersion"

        contactInstagram = v.findViewById(R.id.contact_instagram)
        contactInstagram?.setOnClickListener {
            var url: Uri = Uri.parse("https://instagram.com/madnegro_")
            var i: Intent = Intent(Intent.ACTION_VIEW, url)
            startActivity(i)
        }

        return v
    }


}
