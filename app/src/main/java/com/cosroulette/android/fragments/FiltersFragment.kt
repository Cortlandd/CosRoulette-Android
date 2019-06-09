package com.cosroulette.android.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cosroulette.android.R
import com.cosroulette.android.adapters.FilterAdapter
import com.cosroulette.android.models.FilterModel
import com.cosroulette.android.utils.FilterPreferences
import com.cosroulette.android.utils.RecyclerItemClickListener
import com.cosroulette.android.utils.RecyclerItemClickListener.OnItemClickListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_filters.*
import org.jetbrains.anko.toast


/**
 * A simple [DialogFragment] subclass.
 * Activities that contain this fragment must implement the
 * [FiltersFragment.OnFilterInteractionListener] interface
 * to handle interaction events.
 *
 */
class FiltersFragment : DialogFragment(), Toolbar.OnMenuItemClickListener {

    // TODO: Implement highlighting for each filter in recyclerview when clicked

    var recyclerView: RecyclerView? = null
    var filterAdapter: FilterAdapter? = null
    var mFilters : ArrayList<FilterModel> = ArrayList<FilterModel>()
    private var filterListener: OnFilterInteractionListener? = null
    var filterToolbar: Toolbar? = null
    var mFilterPreferences: FilterPreferences? = null
    var closeFab: FloatingActionButton? = null
    var filterInstructions: TextView? = null
    var filterHelp: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_filters, container, false)

        mFilterPreferences = FilterPreferences(context!!)
        mFilterPreferences?.getFilters()?.let { mFilters.addAll(it) }

        closeFab = view.findViewById(R.id.close_filters_fab)

        filterInstructions = view.findViewById(R.id.filter_instruction_text)
        filterHelp = view.findViewById(R.id.filters_help_text)

        filterToolbar = view.findViewById(R.id.filter_toolbar)
        filterToolbar?.inflateMenu(R.menu.main)
        filterToolbar?.setOnMenuItemClickListener(this)

        recyclerView = view.findViewById<RecyclerView>(R.id.filter_recyclerview)
        recyclerView?.layoutManager = LinearLayoutManager(this.activity)

        filterAdapter = FilterAdapter(this.activity!!, mFilters, filterListener!!)
        recyclerView?.adapter = filterAdapter
        filterAdapter?.notifyDataSetChanged()
        recyclerView?.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                mFilterPreferences?.removeFilter(position)
                mFilters.removeAt(position)
                recyclerView!!.adapter?.notifyItemRemoved(position)
                validateFilterSize()
            }

        }

        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        // TODO: Implement ability to update filter as opposed to deleted and adding another
        recyclerView?.addOnItemTouchListener(RecyclerItemClickListener(context!!, recyclerView!!, object: RecyclerItemClickListener.OnItemClickListener {

            override fun onItemClick(view: View, position: Int) {
                activity?.toast(mFilters[position].title!!)
                /*
                val builder: AlertDialog.Builder = AlertDialog.Builder(activity!!)
                builder.setTitle("Edit Filter")
                val dialogView = activity?.layoutInflater?.inflate(R.layout.dialog_new_filter, null)
                val filter = dialogView!!.findViewById<EditText>(R.id.filter)
                filter.setText(mFilters[position].title)
                builder.setView(dialogView)
                        .setPositiveButton("Update") { dialog, id ->
                            mFilters[position].title = filter.text.toString()
                            filterAdapter?.notifyDataSetChanged()
                        }
                        .setNegativeButton("Cancel") { dialog, id ->
                        }
                builder.create().show()
                */

            }

            override fun onLongItemClick(view: View?, position: Int) {

            }

        }))

        validateFilterSize()

        closeFab!!.setOnClickListener {
            dismiss()
        }

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFilterInteractionListener) {
            filterListener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mFilters.clear()
        filterListener = null
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window?.setLayout(width, height)
        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.add_item -> {
                val builder: AlertDialog.Builder = AlertDialog.Builder(context!!)
                builder.setTitle("Add Filter")

                val dialogView = activity?.layoutInflater?.inflate(R.layout.dialog_new_filter, null)
                val filterText = dialogView!!.findViewById<EditText>(R.id.filter)

                builder.setView(dialogView)
                        .setPositiveButton("Save") { dialog, id ->
                            val filter = FilterModel(filterText.text.toString())
                            mFilterPreferences?.addFilter(filter)
                            filterAdapter?.setData(mFilterPreferences?.getFilters()!!)
                            validateFilterSize()
                            //recyclerView!!.adapter?.notifyDataSetChanged()

                        }
                        .setNegativeButton("Cancel") { dialog, id ->

                        }

                builder.create().show()
            }
        }

        return true
    }

    fun validateFilterSize() {

        val filters = mFilterPreferences!!.getFilters()
        if (filters.isNullOrEmpty()) {
            filterInstructions?.visibility = VISIBLE
            filterHelp?.visibility = GONE
            filterAdapter?.notifyDataSetChanged()
        } else {
            filterHelp?.visibility = VISIBLE
            filterInstructions?.visibility = GONE
            filterAdapter?.notifyDataSetChanged()
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFilterInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

}
