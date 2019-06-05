package com.cosroulette.android.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cosroulette.android.R
import com.cosroulette.android.fragments.FiltersFragment
import com.cosroulette.android.models.FilterModel
import com.cosroulette.android.utils.FilterPreferences

class FilterAdapter(val context: Context, val mFilters: ArrayList<FilterModel>, val filterListener: FiltersFragment.OnFilterInteractionListener): RecyclerView.Adapter<FilterAdapter.ViewHolder>() {

    var mOnClickListener: View.OnClickListener? = null

    init {
        mOnClickListener = View.OnClickListener { v ->
            val filter = v.tag
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.filter_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mFilters.size
    }

    override fun onBindViewHolder(holder: FilterAdapter.ViewHolder, position: Int) {

        val filter = mFilters[position]

        holder.mTitleView.text = filter.title

        with(holder.mView) {
            tag = filter
            setOnClickListener(mOnClickListener)
        }
    }

    /**
     * Method used to implement auto refreshing on Adapter as a user adds a filter.
     * If many filters, I can potentially see speed issue.
     *
     * @param newData: The new ArrayList<FilterModel> from preferences
     * */
    fun setData(newData: ArrayList<FilterModel>) {
        this.mFilters.clear()
        mFilters.addAll(newData)
        notifyDataSetChanged()
    }

    inner class ViewHolder(val mView: View): RecyclerView.ViewHolder(mView) {
        val mTitleView: TextView = mView.findViewById(R.id.filter_layout_text)
    }

}