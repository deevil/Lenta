package com.lenta.bp12.features.open_task.good_list

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.model.IOpenTaskManager
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.device_info.DeviceInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.ScanCodeInfo
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.isCommonFormatNumber
import javax.inject.Inject

class GoodListViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var deviceInfo: DeviceInfo

    @Inject
    lateinit var manager: IOpenTaskManager


    val processingSelectionsHelper = SelectionItemsHelper()

    val processedSelectionsHelper = SelectionItemsHelper()

    val selectedPage = MutableLiveData(0)

    val task by lazy {
        manager.currentTask
    }

    val title by lazy {
        task.map { task ->
            "${task?.type?.code}-${task?.number} // ${task?.name}"
        }
    }

    val numberField: MutableLiveData<String> = MutableLiveData("")

    val requestFocusToNumberField by lazy {
        MutableLiveData(true)
    }

    val processing by lazy {
        task.map { currentTask ->
            currentTask?.let { task ->
                task.goods.filter { !it.isDeleted && !it.isCounted && !it.isMissing }.let { filtered ->
                    filtered.mapIndexed { index, good ->
                        ItemGoodProcessingUi(
                                position = "${filtered.size - index}",
                                name = good.getNameWithMaterial(),
                                material = good.material,
                                providerCode = good.provider.code.orEmpty()
                        )
                    }
                }
            }
        }
    }

    val processed by lazy {
        task.map { currentTask ->
            currentTask?.let { task ->
                task.goods.filter { !it.isDeleted && (it.isMissing || it.isCounted) }.let { filtered ->
                    filtered.mapIndexed { index, good ->
                        ItemGoodProcessedUi(
                                position = "${filtered.size - index}",
                                name = good.getNameWithMaterial(),
                                quantity = "${good.getQuantity().dropZeros()} ${good.commonUnits.name}",
                                material = good.material,
                                providerCode = good.provider.code.orEmpty()
                        )
                    }
                }
            }
        }
    }

    /**
    Кнопки нижнего тулбара
     */

    val deleteVisible by lazy {
        task.map { task ->
            task?.isStrict == false
        }
    }

    val deleteEnabled = selectedPage.combineLatest(processingSelectionsHelper.selectedPositions).combineLatest(processedSelectionsHelper.selectedPositions).map {
        it?.let {
            val page = it.first.first
            val isSelectedProcessing = it.first.second.isNotEmpty()
            val isSelectedProcessed = it.second.isNotEmpty()

            task.value?.isStrict == false && (page == 0 && isSelectedProcessing || page == 1 && isSelectedProcessed)
        }
    }

    val saveEnabled by lazy {
        task.map {
            it?.isExistProcessedGood()
        }
    }

    /**
    Методы
     */

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onClickItemPosition(position: Int) {
        selectedPage.value?.let { page ->
            when (page) {
                0 -> processing.value?.get(position)?.material
                1 -> processed.value?.get(position)?.material
                else -> null
            }?.let { material ->
                openGoodByMaterial(material)
            }
        }
    }

    private fun openGoodByMaterial(material: String) {
        task.value?.let { task ->
            task.goods.find { it.material == material }?.let { good ->
                manager.searchNumber = material
                manager.searchGoodFromList = true
                manager.updateCurrentGood(good)
                navigator.openGoodInfoOpenScreen()
            }
        }
    }

    fun onScanResult(data: String) {
        checkEnteredNumber(data)
    }

    override fun onOkInSoftKeyboard(): Boolean {
        checkEnteredNumber(numberField.value.orEmpty())
        return true
    }

    private fun checkEnteredNumber(number: String) {
        if (task.value?.isStrict == false) {
            openGoodInfoByNumber(number)
        } else {
            val numberWithoutWeight = ScanCodeInfo(number).eanWithoutWeight
            if (manager.isGoodExist(numberWithoutWeight)) {
                openGoodInfoByNumber(number)
            } else {
                numberField.value = ""
                navigator.showGoodIsMissingInTask()
            }
        }
    }

    private fun openGoodInfoByNumber(number: String) {
        numberField.value = ""

        if (isCommonFormatNumber(number)) {
            manager.searchNumber = number
            manager.searchGoodFromList = true
            navigator.openGoodInfoOpenScreen()
        } else {
            navigator.showIncorrectEanFormat()
        }
    }

    fun onClickDelete() {
        selectedPage.value?.let { page ->
            when (page) {
                PROCESSED_PAGE -> {
                    val materials = mutableListOf<String>()
                    processingSelectionsHelper.selectedPositions.value?.forEach { position ->
                        processing.value?.get(position)?.let { item ->
                            materials.add(item.material)
                        }
                    }

                    manager.markGoodsDeleted(materials)
                }
                TO_PROCESS_PAGE -> {
                    val materials = mutableListOf<String>()
                    processedSelectionsHelper.selectedPositions.value?.forEach { position ->
                        processed.value?.get(position)?.let { item ->
                            materials.add(item.material)
                        }
                    }

                    manager.markGoodsUncounted(materials)
                }
                else -> throw IllegalArgumentException("Wrong pager position!")
            }
        }
    }

    fun onClickSave() {
        task.value?.let { task ->
            if (task.isExistUncountedGood()) {
                navigator.showMakeTaskCountedAndClose {
                    navigator.openDiscrepancyListScreen()
                }
            } else {
                manager.finishCurrentTask()
                manager.prepareSendTaskDataParams(
                        deviceIp = deviceInfo.getDeviceIp(),
                        tkNumber = sessionInfo.market.orEmpty(),
                        userNumber = sessionInfo.personnelNumber.orEmpty()
                )

                navigator.openSaveDataScreen()
            }
        }
    }

    companion object {
        private const val PROCESSED_PAGE = 0
        private const val TO_PROCESS_PAGE = 1
    }

}

data class ItemGoodProcessingUi(
        val position: String,
        val name: String,
        val material: String,
        val providerCode: String
)

data class ItemGoodProcessedUi(
        val position: String,
        val name: String,
        val quantity: String,
        val material: String,
        val providerCode: String
)
