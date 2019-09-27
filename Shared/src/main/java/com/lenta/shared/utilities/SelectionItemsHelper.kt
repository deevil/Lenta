package com.lenta.shared.utilities

import androidx.lifecycle.MutableLiveData

class SelectionItemsHelper {

    val selectedPositions = MutableLiveData(mutableSetOf<Int>())

    fun add(position: Int) {
        selectedPositions.value!!.add(position)
        notifyChanged()
    }


    fun remove(position: Int) {
        selectedPositions.value!!.remove(position)
        notifyChanged()
    }

    fun addAll(items: Collection<Any>) {
        selectedPositions.value?.let {
            items.forEachIndexed { index, _ -> it.add(index) }
        }
        notifyChanged()
    }

    fun clearPositions() {
        selectedPositions.value!!.clear()
        notifyChanged()
    }

    fun isSelectedEmpty(): Boolean {
        return selectedPositions.value!!.isEmpty()
    }

    fun revert(position: Int) {
        if (selectedPositions.value!!.contains(position)) {
            remove(position)
        } else {
            add(position)
        }
    }

    fun isSelected(position: Int): Boolean {
        return selectedPositions.value!!.contains(position)
    }

    private fun notifyChanged() {
        selectedPositions.postValue(selectedPositions.value)
    }

}