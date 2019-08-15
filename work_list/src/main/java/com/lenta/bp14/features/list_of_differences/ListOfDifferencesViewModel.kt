package com.lenta.bp14.features.list_of_differences

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.shared.platform.viewmodel.CoreViewModel
import kotlinx.coroutines.launch

class ListOfDifferencesViewModel : CoreViewModel() {

    val differences: MutableLiveData<List<DifferenceVM>> = MutableLiveData()

    val enabledMissingButton: MutableLiveData<Boolean> = MutableLiveData(false)


    init {
        viewModelScope.launch {
            differences.value = getTestData()
        }

    }

    private fun getTestData(): List<DifferenceVM>? {
        return List(100) {
            DifferenceVM(it + 1, "0000$it Горбуша")
        }
    }

    fun getTitle(): String? {
        return "???"
    }

    fun onClickSkip() {

    }

    fun onClickMissing() {

    }


}

data class DifferenceVM(
        val number: Int,
        val name: String
)
