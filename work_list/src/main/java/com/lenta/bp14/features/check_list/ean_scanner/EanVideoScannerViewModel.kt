package com.lenta.bp14.features.check_list.ean_scanner

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.ml.CheckStatus
import com.lenta.bp14.models.check_list.ICheckListTask
import com.lenta.bp14.models.getTaskName
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import kotlinx.coroutines.launch
import javax.inject.Inject

class EanVideoScannerViewModel : CoreViewModel() {

    @Inject
    lateinit var task: ICheckListTask


    fun getTitle(): String {
        return "${task.getTaskType().taskType} // ${task.getTaskName()}"
    }

    val isAdded = MutableLiveData(false)

    val productTitle = MutableLiveData("")


    fun checkStatus(rawCode: String): CheckStatus? {
        Logg.d { "rawCode: $rawCode" }

        task.getGoodByEanFromList(rawCode)?.let {good ->
            productTitle.value = good.material.takeLast(6)
            isAdded.value = true
            return CheckStatus.VALID
        }

        productTitle.value = ""
        isAdded.value = false

        viewModelScope.launch {
            task.checkProductFromVideoScan(rawCode)
        }

        return CheckStatus.ERROR
    }

}
