package com.lenta.bp14.features.check_list.goods_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.models.IGeneralTaskManager
import com.lenta.bp14.models.check_list.Good
import com.lenta.bp14.models.check_list.ICheckListTask
import com.lenta.bp14.models.check_price.IPriceInfoParser
import com.lenta.bp14.models.getTaskName
import com.lenta.bp14.models.getTaskNumber
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.bp14.requests.check_list.CheckListSendReportNetRequest
import com.lenta.bp14.requests.tasks.IUnlockTaskNetRequest
import com.lenta.bp14.requests.tasks.UnlockTaskParams
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.device_info.DeviceInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.actionByNumber
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodsListClViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var task: ICheckListTask
    @Inject
    lateinit var priceInfoParser: IPriceInfoParser
    @Inject
    lateinit var generalTaskManager: IGeneralTaskManager
    @Inject
    lateinit var deviceInfo: DeviceInfo
    @Inject
    lateinit var sentReportRequest: CheckListSendReportNetRequest
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var unlockTaskNetRequest: IUnlockTaskNetRequest


    val selectionsHelper = SelectionItemsHelper()

    val taskName = MutableLiveData("")

    val numberField: MutableLiveData<String> = MutableLiveData("")
    val requestFocusToNumberField: MutableLiveData<Boolean> = MutableLiveData()

    val goods: MutableLiveData<List<ItemGoodUi>> by lazy {
        task.goods.map { list: List<Good>? ->
            list?.mapIndexed { index, good ->
                ItemGoodUi(
                        position = (list.size - index).toString(),
                        name = good.getFormattedMaterialWithName(),
                        quantity = good.quantity,
                        units = good.units
                )
            }
        }
    }

    val deleteButtonEnabled by lazy {
        selectionsHelper.selectedPositions.map { it?.isNotEmpty() ?: false }
    }

    val saveButtonEnabled by lazy {
        goods.map { it?.isNotEmpty() ?: false }
    }

    // -----------------------------

    init {
        launchUITryCatch {
            task.loadMaxTaskPositions()
            requestFocusToNumberField.value = true
            taskName.value = "${task.getTaskType().taskType} // ${task.getTaskName()}"
        }
    }

    // -----------------------------

    fun onClickDelete() {
        task.deleteSelectedGoods(selectionsHelper.selectedPositions.value!!)
        selectionsHelper.clearPositions()
    }

    fun onClickSave() {
        // Подтверждение - Перевести задание в статус "Подсчитано" и закрыть его для редактирования? - Назад / Да
        navigator.showSetTaskToStatusCalculated {
            launchUITryCatch {
                navigator.showProgressLoadingData(::handleFailure)
                sentReportRequest(task.getReportData(deviceInfo.getDeviceIp())).either(
                        {
                            navigator.openAlertScreen(failure = it)
                        }
                ) {
                    Logg.d { "SentReportResult: $it" }
                    generalTaskManager.clearCurrentTask(sentReportResult = it)
                    navigator.openReportResultScreen()
                }
                navigator.hideProgress()
            }
        }
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    override fun onOkInSoftKeyboard(): Boolean {
        checkEnteredNumber(numberField.value.orEmpty())
        return true
    }

    private fun checkEnteredNumber(number: String) {
        actionByNumber(
                number = number,
                funcForEan = { ean, _ -> searchCode(ean = ean) },
                funcForMaterial = { material -> searchCode(material = material) },
                funcForPriceQrCode = { qrCode ->
                    priceInfoParser.getPriceInfoFromRawCode(qrCode)?.let {
                        searchCode(ean = it.eanCode)
                    }
                },
                funcForSapOrBar = navigator::showTwelveCharactersEntered,
                funcForNotValidFormat = navigator::showGoodNotFound
        )
    }

    private fun searchCode(ean: String? = null, material: String? = null) {
        viewModelScope.launch {
            require((ean != null) xor (material != null)) {
                "Only one param allowed - ean: $ean, material: $material"
            }

            navigator.showProgressLoadingData(::handleFailure)

            when {
                !ean.isNullOrBlank() -> task.getGoodByEan(ean)
                !material.isNullOrBlank() -> task.getGoodByMaterial(material)
                else -> null
            }.also {
                numberField.value = ""
                navigator.hideProgress()
            }?.let { good ->
                Logg.d { "--> Max task positions: ${task.getMaxTaskPositions()}" }
                if (task.isReachLimitPositions(good.material)) {
                    navigator.showMaxCountProductAlert()
                    return@launch
                }

                task.addGood(good)
                return@launch
            }

            navigator.showGoodNotFound()
        }
    }

    fun onClickVideo() {
        navigator.openVideoScanProductScreen()

    }

    fun onScanResult(data: String) {
        checkEnteredNumber(data)
    }

    fun showVideoErrorMessage() {
        navigator.showDeviceNotSupportVideoScan()
    }

    fun onBackPressed(): Boolean {
        if (!task.isEmpty() && sessionInfo.isAuthSkipped.value == true) {
            navigator.openConfirmationExitTask(task.getDescription().taskName) {
                clearCurrentTaskAndGoBack()
            }
        } else {
            navigator.goBack()
        }

        return false
    }

    private fun clearCurrentTaskAndGoBack() {
        launchUITryCatch {
            generalTaskManager.getProcessedTask()?.getTaskNumber().let { taskNumber ->
                if (taskNumber?.isNotBlank() == true) {
                    navigator.showProgress(unlockTaskNetRequest)
                    unlockTaskNetRequest(
                            UnlockTaskParams(
                                    ip = deviceInfo.getDeviceIp(),
                                    taskNumber = taskNumber
                            )
                    ).either(::handleFailure) {
                        generalTaskManager.clearCurrentTask()
                        navigator.goBack()
                        true
                    }
                    navigator.hideProgress()
                } else {
                    generalTaskManager.clearCurrentTask()
                    navigator.goBack()
                }
            }
        }
    }

}

data class ItemGoodUi(
        val position: String,
        val name: String,
        val quantity: MutableLiveData<String>,
        val units: Uom
)