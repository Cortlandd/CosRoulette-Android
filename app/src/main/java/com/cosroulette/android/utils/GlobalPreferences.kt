package com.cosroulette.android.utils

import android.content.Context
import android.content.SharedPreferences

class GlobalPreferences(context: Context) {

    var mSharedPreferences: SharedPreferences? = null
    val GLOBAL_PREFS = "global_prefs"
    val BASE_CATEGORY = "selected_category"

    init {
        mSharedPreferences = context.getSharedPreferences(GLOBAL_PREFS, Context.MODE_PRIVATE)
    }

    /**
     *
     * Set the selected Category in preferences.
     *
     * @param selected: The selected Category.
     *
     */
    fun setSelectedCategory(selected: Int) {
        with(mSharedPreferences!!.edit()) {
            this.putInt(BASE_CATEGORY, selected)
            this.commit()
        }
    }

    /**
     *
     * Get the stored selected Category
     *
     */
    fun getSelectedCategory(): Int {
        return mSharedPreferences!!.getInt(BASE_CATEGORY, 0)
    }

    /**
     *
     * Return global SharedPreferences variable in GlobalPreferences.
     *
     */
    fun getGlobalPreferences(): SharedPreferences? {
        return mSharedPreferences
    }

}