package com.lenta.shared.utilities.databinding

import android.os.Build
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.databinding.BindingAdapter
import com.lenta.shared.view.OnPositionClickListener

@BindingAdapter(value = ["items", "position", "onPositionClickListener"], requireAll = true)
fun setupSpinner(spinner: Spinner, items: List<String>?, position: Int?, onPositionClickListener: OnPositionClickListener) {
    var adapter: ArrayAdapter<String>
    if (spinner.adapter == null) {
        val mutableList: MutableList<String> = (items ?: listOf()).toMutableList()
        spinner.tag = mutableList
        adapter = ArrayAdapter(spinner.context, android.R.layout.simple_spinner_item, mutableList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.postDelayed({
            spinner.setSelection(position ?: 0)
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(adapterView: AdapterView<*>, view: View, position: Int, l: Long) {
                    onPositionClickListener.onClickPosition(position)
                }

                override fun onNothingSelected(adapterView: AdapterView<*>) {
                }
            }
        }, 100)
    }
    @Suppress("UNCHECKED_CAST")
    adapter = (spinner.adapter as ArrayAdapter<String>)
    adapter.clear()
    items?.let {
        adapter.addAll(it)
        val isMoreThenOne = it.size > 1
        spinner.isEnabled = isMoreThenOne
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            spinner.focusable = if (isMoreThenOne) View.FOCUSABLE else View.NOT_FOCUSABLE
        }

    }
    adapter.notifyDataSetChanged()
}