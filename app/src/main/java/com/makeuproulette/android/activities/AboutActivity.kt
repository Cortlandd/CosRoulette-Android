package com.makeuproulette.android.activities

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import com.makeuproulette.android.fragments.FAQFragment
import com.makeuproulette.android.fragments.LicensesFragment
import com.makeuproulette.android.R
import com.makeuproulette.android.adapters.SectionsPageAdapter
import kotlinx.android.synthetic.main.activity_about.*

class AboutActivity : AppCompatActivity() {

    var mSectionsPageAdapter: SectionsPageAdapter? = null

    var mViewPager: ViewPager? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        mSectionsPageAdapter = SectionsPageAdapter(supportFragmentManager)

        mViewPager = findViewById<ViewPager>(R.id.container)
        setupViewPager(mViewPager!!)

        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(mViewPager)

        close_about_fab.setOnClickListener {
            finish()
        }

    }

    fun setupViewPager(viewPager: ViewPager) {
        var adapter: SectionsPageAdapter = SectionsPageAdapter(supportFragmentManager)
        adapter.addFragment(FAQFragment(), "FAQ")
        adapter.addFragment(LicensesFragment(), "Open Source Licenses")
        viewPager.adapter = adapter
    }

    override fun onDestroy() {
        super.onDestroy()
        finish()
    }
}