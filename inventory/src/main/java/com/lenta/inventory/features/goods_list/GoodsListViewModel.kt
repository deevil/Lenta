package com.lenta.inventory.features.goods_list

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.inventory.models.RecountType
import com.lenta.inventory.models.StorePlaceLockMode
import com.lenta.inventory.models.task.IInventoryTaskManager
import com.lenta.inventory.models.task.StorePlaceProcessing
import com.lenta.inventory.platform.navigation.IScreenNavigator
import com.lenta.inventory.requests.network.StorePlaceLockNetRequest
import com.lenta.inventory.requests.network.StorePlaceLockParams
import com.lenta.inventory.requests.network.StorePlaceLockRestInfo
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.Evenable
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodsListViewModel : CoreViewModel(), OnOkInSoftKeyboardListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var lockRequest: StorePlaceLockNetRequest
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var context: Context
    @Inject
    lateinit var taskManager: IInventoryTaskManager

    val unprocessedGoods: MutableLiveData<List<ProductInfoVM>> = MutableLiveData()
    val processedGoods: MutableLiveData<List<ProductInfoVM>> = MutableLiveData()
    var selectedPage = MutableLiveData(0)

    val eanCode: MutableLiveData<String> = MutableLiveData()
    val requestFocusToEan: MutableLiveData<Boolean> = MutableLiveData()

    val deleteEnabled: MutableLiveData<Boolean> = selectedPage.map { it ?: 0 != 0 }

    var storePlaceManager: StorePlaceProcessing? = null
    var justCreated: Boolean = true

    fun getTitle(): String {
        return "Номер задания - тип задания"
    }

    init {

    }

    fun onResume() {
        if (justCreated) {
            val recountType = taskManager.getInventoryTask()?.taskDescription?.recountType
            if (recountType != RecountType.Simple) {
                makeLockUnlockRequest(recountType, StorePlaceLockMode.Lock, ::handleLockSuccess)
                justCreated = false
            }
        }
        updateUnprocessed()
        updateProcessed()
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.openAlertScreen(failure)
    }

    private fun handleLockSuccess(storePlaceLockInfo: StorePlaceLockRestInfo) {
        Logg.d { "tasksListRestInfo $storePlaceLockInfo" }
    }

    private fun handleUnlockSuccess(storePlaceLockInfo: StorePlaceLockRestInfo) {
        Logg.d { "tasksListRestInfo $storePlaceLockInfo" }
        screenNavigator.goBack()
    }

    fun updateProcessed() {
        val processed = storePlaceManager?.getProcessedProducts() ?: emptyList()
        processedGoods.postValue(processed.mapIndexed { index, productInfo ->
            ProductInfoVM(number = processed.size - index,
                    name = "${productInfo.getMaterialLastSix()} ${productInfo.description}",
                    quantity = "${productInfo.factCount.toStringFormatted()} ${productInfo.uom.name}")
        })
    }

    fun updateUnprocessed() {
        val unprocessed = storePlaceManager?.getNotProcessedProducts() ?: emptyList()
        unprocessedGoods.postValue(unprocessed.mapIndexed { index, productInfo -> ProductInfoVM(number = unprocessed.size - index, name = "${productInfo.getMaterialLastSix()} ${productInfo.description}\"", quantity = productInfo.factCount.toString()) })
    }

    fun onClickClean() {
        return
    }

    fun onClickComplete() {
        return
    }

    fun onClickBack() {
        val recountType = taskManager.getInventoryTask()?.taskDescription?.recountType
        if (recountType != RecountType.Simple) {
            makeLockUnlockRequest(recountType, StorePlaceLockMode.Unlock, ::handleUnlockSuccess)
        } else {
            screenNavigator.goBack()
        }
    }

    private fun makeLockUnlockRequest(recountType: RecountType?, mode: StorePlaceLockMode, successCallback: (StorePlaceLockRestInfo) -> Unit)
    {
        viewModelScope.launch {
            screenNavigator.showProgress(lockRequest)
            taskManager.getInventoryTask()?.let {
                val userNumber = if (recountType == RecountType.ParallelByPerNo) sessionInfo.personnelNumber ?: "" else "" // указываем номер только при пересчете по номерам
                val storePlaceCode = if (recountType == RecountType.ParallelByStorePlaces) storePlaceManager?.storePlaceNumber ?: "" else "" //указываем номер только при пересчете по МХ
                lockRequest(StorePlaceLockParams(ip = context.getDeviceIp(),
                        taskNumber = it.taskDescription.taskNumber,
                        storePlaceCode = storePlaceCode,
                        mode = mode.mode,
                        userNumber = userNumber)).either(::handleFailure, successCallback)
            }
            screenNavigator.hideProgress()
        }
    }

    fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onDoubleClickPosition(position: Int) {
    }

    fun onDigitPressed(digit: Int) {
        requestFocusToEan.value = true
        eanCode.value = eanCode.value ?: "" + digit
    }

    fun onScanResult(data: String) {
        eanCode.value = data
    }

    override fun onOkInSoftKeyboard(): Boolean {


        return true
    }
}

data class ProductInfoVM(
        val number: Int,
        val name: String,
        val quantity: String
)