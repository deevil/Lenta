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

    val unprocessedGoods: MutableLiveData<List<GoodItem>> = MutableLiveData()
    val processedGoods: MutableLiveData<List<GoodItem>> = MutableLiveData()
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
        val goodItem = GoodItem(1, "Good Processed Good", "Што шт.", false)
        val badItem = GoodItem(2, "Bad Processed Good", "Што шт.", true)
        val uglyItem = GoodItem(3, "Ugly Processed Good", "Што шт.", false)
        processedGoods.postValue(listOf(goodItem, badItem, uglyItem))
    }

    fun updateUnprocessed() {
        val goodItem = GoodItem(1, "Good Good", "Што шт.", false)
        val badItem = GoodItem(2, "Bad Good", "Што шт.", true)
        val uglyItem = GoodItem(3, "Ugly Good", "Што шт.", false)
        unprocessedGoods.postValue(listOf(goodItem, badItem, uglyItem))
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

data class GoodItem(
        val number: Int,
        val name: String,
        val quantity: String,
        val even: Boolean
//        val productInfo: ProductInfo
) : Evenable {
    override fun isEven() = even
}