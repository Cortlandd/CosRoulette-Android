package com.makeuproulette.android.fragments


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.appcompat.app.AlertDialog
import android.widget.EditText
import com.makeuproulette.android.R

class NewFilterDialogFragment: androidx.fragment.app.DialogFragment() {

    interface NewFilterDialogListener {
        fun onDialogPositiveClick(dialog: androidx.fragment.app.DialogFragment, filter: String)
        fun onDialogNegativeClick(dialog: androidx.fragment.app.DialogFragment)
    }

    var newFilterDialogListener: NewFilterDialogListener? = null

    companion object {
        fun newInstance(title: Int, selected: String?): NewFilterDialogFragment {
            val newFilterDialogFragment = NewFilterDialogFragment()
            val args = Bundle()
            args.putInt("dialog_title", title)
            args.putString("selected_item", selected)
            newFilterDialogFragment.arguments = args
            return newFilterDialogFragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val title = arguments!!.getInt("dialog_title")
        val selectedText = arguments!!.getString("selected_item")
        val builder = AlertDialog.Builder(this!!.activity!!)
        builder.setTitle(title)

        val dialogView = activity?.layoutInflater?.inflate(R.layout.dialog_new_filter, null)
        val filter = dialogView!!.findViewById<EditText>(R.id.filter)

        filter.setText(selectedText)

        builder.setView(dialogView)
                .setPositiveButton("Save") { dialog, id ->
                    newFilterDialogListener?.onDialogPositiveClick(this, filter.text.toString())
                }
                .setNegativeButton("Cancel") { dialog, id ->
                    newFilterDialogListener?.onDialogNegativeClick(this)
                }

        return builder.create()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        try {
            newFilterDialogListener = activity as NewFilterDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + "must implement NewFilterDialogListener")
        }
    }

}