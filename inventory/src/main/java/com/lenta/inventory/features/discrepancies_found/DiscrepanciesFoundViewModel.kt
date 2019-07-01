package com.lenta.inventory.features.discrepancies_found

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.shared.platform.viewmodel.CoreViewModel
import kotlinx.coroutines.launch
import com.lenta.shared.utilities.databinding.Evenable

class DiscrepanciesFoundViewModel : CoreViewModel() {

    val discrepanciesByGoods: MutableLiveData<List<Discrepancy>> = MutableLiveData()
    val discrepanciesByStorage: MutableLiveData<List<Discrepancy>> = MutableLiveData()

    var selectedPage = MutableLiveData(0)

    init {
        viewModelScope.launch {
            updateByGoods()
            updateByStorage()
        }
    }

    fun onResume() {
        updateByGoods()
        updateByStorage()
    }

    fun updateByGoods() {
        val dis1 = Discrepancy(1, "Товар1", "", false)
        val dis2 = Discrepancy(2, "Товар2", "", true)
        val dis3 = Discrepancy(3, "Товар3", "", false)
        discrepanciesByGoods.postValue(listOf(dis1, dis2, dis3))
    }

    fun updateByStorage() {
        val dis1 = Discrepancy(1, "Товар1", "МХ-123455", false)
        val dis2 = Discrepancy(2, "Товар2", "МХ-543211", true)
        val dis3 = Discrepancy(3, "Товар3", "МХ-123321", false)
        discrepanciesByStorage.postValue(listOf(dis1, dis2, dis3))
    }

    fun onClickMissing() {
        return
    }

    fun onClickSkip() {
        return
    }

    fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onDoubleClickPosition(position: Int) {
    }
}

data class Discrepancy(
        val number: Int,
        val goods: String,
        val place: String,
        val even: Boolean
) : Evenable {
    override fun isEven() = even
}