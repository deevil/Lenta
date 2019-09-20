package com.lenta.bp14.features.check_list.ean_scanner

import androidx.lifecycle.MutableLiveData
import com.lenta.bp14.ml.CheckStatus
import com.lenta.bp14.models.check_list.ICheckListTask
import com.lenta.bp14.models.getTaskName
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import javax.inject.Inject

class EanVideoScannerViewModel : CoreViewModel() {

    @Inject
    lateinit var checkListTask: ICheckListTask

    fun getTitle(): String {
        return "${checkListTask.getTaskType().taskType} // ${checkListTask.getTaskName()}"
    }

    val isAdded = MutableLiveData(false)

    val productTitle = MutableLiveData("")


    fun checkStatus(rawCode: String): CheckStatus? {
        Logg.d { "rawCode: $rawCode" }
        //TODO реализовать поиск товара и взаимодействие с checkListTask

        productTitle.value = rawCode
        isAdded.value = ((rawCode.toLongOrNull()
                ?: 0L) % 2 == 0L)

        return if (isAdded.value == true) CheckStatus.VALID else CheckStatus.NOT_VALID
    }


}
