package com.cosroulette.android.utils

import android.content.Context
import android.content.SharedPreferences
import com.cosroulette.android.models.FilterModel
import com.google.gson.Gson
import com.google.android.gms.cast.VastAdsRequest.fromJson



class FilterPreferences {

    var mSharedPreferences: SharedPreferences? = null
    var PREFS_NAME: String = "filters_pref"
    var FILTERS_PREF_NAME: String = "filters_list"

    constructor(context: Context) {
        this.mSharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun addFilter(filter: FilterModel) {

        var filters = getFilters()

        if (filters == null) {
            filters = ArrayList<FilterModel>()
        }
        with(mSharedPreferences!!.edit()) {
            filters.add(filter)
            val gson: Gson = Gson()
            val jsonFilters = gson.toJson(filters)
            this.putString(FILTERS_PREF_NAME, jsonFilters)
            this.commit()
        }
    }

    // TODO: Figure out updating filter
    fun updateFilter(filter: FilterModel) {
    }

    fun removeFilter(filterIndex: Int) {
        val filters = getFilters()
        if (filters != null) {
            with(mSharedPreferences!!.edit()) {
                filters.removeAt(filterIndex)
                val gson: Gson = Gson()
                val jsonFilters = gson.toJson(filters)
                this.putString(FILTERS_PREF_NAME, jsonFilters)
                this.commit()
            }
        }
    }

    fun getFilters(): ArrayList<FilterModel>? {

        var filters = ArrayList<FilterModel>()

        if (mSharedPreferences!!.contains(FILTERS_PREF_NAME)) {
            var jsonFilters = mSharedPreferences!!.getString(FILTERS_PREF_NAME, null)
            var gson = Gson()
            var filtersItems = gson.fromJson(jsonFilters, Array<FilterModel>::class.java)

            filters.addAll(filtersItems)
        } else {
            return null
        }

        return filters
    }

    fun getFilterPreferences(): SharedPreferences? {
        return mSharedPreferences
    }

}