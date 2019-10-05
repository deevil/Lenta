package com.lenta.bp14.features.check_list.ean_scanner

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.ml.CheckStatus
import com.lenta.bp14.models.check_list.GoodRequestResult
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

    private var lastCode: String? = null
    private var goodRequestResult: GoodRequestResult? = null

    fun checkStatus(rawCode: String): CheckStatus? {
        Logg.d { "rawCode: $rawCode" }

        if (lastCode != rawCode) {
            lastCode = rawCode
            viewModelScope.launch {
                goodRequestResult = task.getGoodRequestResult(rawCode)
            }
        }

        if (goodRequestResult != null) {
            val good = goodRequestResult!!.good
            goodRequestResult = null

            good?.let {
                isAdded.value = true
                productTitle.value = it.material.takeLast(6)
                return CheckStatus.VALID
            }

            isAdded.value = false
            productTitle.value = "Не найден"
            return CheckStatus.ERROR
        }

        return null
    }

}
