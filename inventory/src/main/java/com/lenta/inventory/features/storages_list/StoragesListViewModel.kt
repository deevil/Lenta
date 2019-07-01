package com.lenta.inventory.features.storages_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.databinding.Evenable
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch

class StoragesListViewModel: CoreViewModel(), OnOkInSoftKeyboardListener {

    val unprocessedStorages: MutableLiveData<List<StoragePlace>> = MutableLiveData()
    val processedStorages: MutableLiveData<List<StoragePlace>> = MutableLiveData()
    var selectedPage = MutableLiveData(0)

    val storageNumber: MutableLiveData<String> = MutableLiveData()
    val requestFocusToStorageNumber: MutableLiveData<Boolean> = MutableLiveData()

    val deleteEnabled: MutableLiveData<Boolean> = selectedPage.map { it ?: 0 != 0 }

    init {
        viewModelScope.launch {
            updateUnprocessed()
            updateProcessed()
        }

    }

    fun onResume() {
        updateUnprocessed()
        updateProcessed()
    }

    fun getTitle(): String {
        return "Номер задания - тип задания"
    }

    fun updateProcessed() {
        val goodItem = StoragePlace(1, "Good Processed Storage", 20, false)
        val badItem = StoragePlace(2, "Bad Processed Storage", 23, true)
        val uglyItem = StoragePlace(3, "Ugly Processed Storage", 10050, false)
        processedStorages.postValue(listOf(goodItem, badItem, uglyItem))
    }

    fun updateUnprocessed() {
        val goodItem = StoragePlace(1, "Good Unrocessed Storage", 40, false)
        val badItem = StoragePlace(2, "Bad Unprocessed Storage", 43, true)
        val uglyItem = StoragePlace(3, "Ugly Unprocessed Storage", 40050, false)
        unprocessedStorages.postValue(listOf(goodItem, badItem, uglyItem))
    }

    fun onClickClean() {
        return
    }

    fun onClickComplete() {
        return
    }

    fun onClickRefresh() {
        return
    }

    fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onDoubleClickPosition(position: Int) {
    }

    fun onDigitPressed(digit: Int) {
        requestFocusToStorageNumber.value = true
        storageNumber.value = storageNumber.value ?: "" + digit
    }

    fun onScanResult(data: String) {
        storageNumber.value = data
    }

    override fun onOkInSoftKeyboard(): Boolean {

        return true
    }
}

data class StoragePlace(
        val number: Int,
        val name: String,
        val goodsQuantity: Int,
        val even: Boolean
) : Evenable {
    override fun isEven() = even
}