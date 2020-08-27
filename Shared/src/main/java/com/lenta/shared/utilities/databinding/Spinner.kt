package com.lenta.shared.utilities.databinding

import android.os.Build
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import com.lenta.shared.view.OnPositionClickListener

@BindingAdapter(value = ["items", "position", "onPositionClickListener", "android:enabled"], requireAll = false)
fun setupSpinner(spinner: Spinner, items: List<String>?, position: Int?, onPositionClickListener: OnPositionClickListener?, enabled: Boolean?) {
    var adapter: ArrayAdapter<String>
    if (spinner.adapter == null) {
        val mutableList: MutableList<String> = items.orEmpty().toMutableList()
        spinner.tag = mutableList
        adapter = ArrayAdapter(spinner.context, android.R.layout.simple_spinner_item, mutableList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    if (spinner.onItemSelectedListener == null && onPositionClickListener != null) {
        spinner.postDelayed({
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(adapterView: AdapterView<*>, view: View?, position: Int, l: Long) {
                    onPositionClickListener.onClickPosition(position)
                }

                override fun onNothingSelected(adapterView: AdapterView<*>) = Unit
            }
        }, 500)
    }

    @Suppress("UNCHECKED_CAST")
    adapter = (spinner.adapter as ArrayAdapter<String>)
    adapter.clear()
    items?.let {
        adapter.addAll(it)
        val isEnabled = enabled ?: (it.size > 1)
        spinner.isEnabled = isEnabled
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            spinner.focusable = if (isEnabled) View.FOCUSABLE else View.NOT_FOCUSABLE
        }

    }
    if (position != null && spinner.selectedItemPosition != position) {
        spinner.setSelection(position)
    }
    adapter.notifyDataSetChanged()
}

@BindingAdapter("onPositionClicked")
fun onPositionClicked(spinner: Spinner, handlerLiveData: MutableLiveData<Int>?) {
    if (spinner.onItemSelectedListener == null && handlerLiveData != null) {
        spinner.postDelayed({
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(adapterView: AdapterView<*>, view: View?, position: Int, l: Long) {
                    handlerLiveData.value = position
                }

                override fun onNothingSelected(adapterView: AdapterView<*>) = Unit
            }
        }, 500)
    } else if (handlerLiveData == null) {
        spinner.onItemSelectedListener = null
    }
}