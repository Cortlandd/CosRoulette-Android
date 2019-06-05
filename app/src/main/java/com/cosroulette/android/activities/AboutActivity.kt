package com.cosroulette.android.activities

import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import com.cosroulette.android.fragments.FAQFragment
import com.cosroulette.android.fragments.LicensesFragment
import com.cosroulette.android.R
import com.cosroulette.android.adapters.SectionsPageAdapter
import kotlinx.android.synthetic.main.activity_about.*

class AboutActivity : AppCompatActivity() {

    // Page section adapter representing multiple pages: FAQ and Open Source Licenses
    private var mSectionsPageAdapter: SectionsPageAdapter? = null
    // Android ViewPager
    private var mViewPager: ViewPager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        mSectionsPageAdapter = SectionsPageAdapter(supportFragmentManager)

        setupViewPager(view_pager_container)

        tabs.setupWithViewPager(view_pager_container)

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