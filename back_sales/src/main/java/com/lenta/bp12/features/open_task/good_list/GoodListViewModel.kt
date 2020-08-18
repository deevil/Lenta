package com.lenta.bp12.features.open_task.good_list

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.features.basket.ItemWholesaleBasketUi
import com.lenta.bp12.model.IOpenTaskManager
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.platform.resource.IResourceManager
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.device_info.DeviceInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
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

    @Inject
    lateinit var resource: IResourceManager


    val processingSelectionsHelper = SelectionItemsHelper()

    val processedSelectionsHelper = SelectionItemsHelper()

    val basketSelectionsHelper = SelectionItemsHelper()

    val selectedPage = MutableLiveData(0)

    val task by lazy {
        manager.currentTask
    }

    val title by lazy {
        task.map { task ->
            "${task?.type?.code}-${task?.number} // ${task?.name}"
        }
    }

    val description by lazy {
        if (manager.isWholesaleTaskType) resource.taskContent() else resource.goodList()
    }

    val numberField: MutableLiveData<String> = MutableLiveData("")

    val requestFocusToNumberField by lazy {
        MutableLiveData(true)
    }

    val processing by lazy {
        task.map { currentTask ->
            currentTask?.let { task ->
                task.goods.filter { !it.isDeleted && !it.isCounted }.let { filtered ->
                    filtered.mapIndexed { index, good ->
                        ItemGoodProcessingUi(
                                position = "${filtered.size - index}",
                                name = good.getNameWithMaterial(),
                                material = good.material,
                                providerCode = good.provider.code,
                                quantity = "${good.planQuantity.dropZeros()} ${good.commonUnits.name}"
                        )
                    }
                }
            }
        }
    }

    val processed by lazy {
        task.map { currentTask ->
            currentTask?.let { task ->
                task.goods.filter { it.isCounted }.let { filtered ->
                    filtered.mapIndexed { index, good ->
                        ItemGoodProcessedUi(
                                position = "${filtered.size - index}",
                                name = good.getNameWithMaterial(),
                                quantity = "${good.getTotalQuantity().dropZeros()} ${good.commonUnits.name}",
                                material = good.material,
                                providerCode = good.provider.code
                        )
                    }
                }
            }
        }
    }

    val wholesaleBaskets by lazy {
        task.map {
            it?.let { task ->
                emptyList<ItemWholesaleBasketUi>()

                task.baskets.reversed().mapIndexed { index, basket ->
                    val position = task.baskets.size - index
                    ItemWholesaleBasketUi(
                            basket = basket,
                            position = "$position",
                            name = resource.basket("$position"),
                            description = basket.getDescription(task.type?.isDivBySection ?: false),
                            quantity = basket.quantity.orEmpty(),
                            isPrinted = basket.isPrinted,
                            isLocked = basket.isLocked
                    )
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

    val deleteEnabled = selectedPage.combineLatest(processingSelectionsHelper.selectedPositions)
            .combineLatest(processedSelectionsHelper.selectedPositions).map {
                it?.let {
                    val page = it.first.first
                    val isSelectedProcessing = it.first.second.isNotEmpty()
                    val isSelectedProcessed = it.second.isNotEmpty()

                    (page == 0 && isSelectedProcessing) || (page == 1 && isSelectedProcessed)
                }
            }

    val printVisibility by lazy {
        selectedPage.map { tab ->
            manager.isWholesaleTaskType && tab == 2
        }
    }

    val printEnabled by lazy {
        wholesaleBaskets.map {
            it?.isNotEmpty()
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

    fun onScanResult(data: String) {
        openGoodInfoByNumber(data)
    }

    override fun onOkInSoftKeyboard(): Boolean {
        openGoodInfoByNumber(numberField.value.orEmpty())
        return true
    }

    private fun openGoodByMaterial(material: String) {
        task.value?.let { task ->
            task.goods.find { it.material == material }?.let { good ->
                manager.searchNumber = material
                manager.isSearchFromList = true
                navigator.openGoodInfoOpenScreen()
            }
        }
    }

    private fun openGoodInfoByNumber(number: String) {
        numberField.value = ""

        if (isCommonFormatNumber(number)) {
            manager.searchNumber = number
            manager.isSearchFromList = true
            navigator.openGoodInfoOpenScreen()
        } else {
            navigator.showIncorrectEanFormat()
        }
    }

    fun onClickDelete() {
        selectedPage.value?.let { page ->
            when (page) {
                0 -> {
                    val materials = mutableListOf<String>()
                    processingSelectionsHelper.selectedPositions.value?.mapNotNullTo(materials) { position ->
                        processing.value?.get(position)?.material
                    }

                    processingSelectionsHelper.clearPositions()
                    manager.markGoodsDeleted(materials)
                }
                1 -> {
                    val materials = mutableListOf<String>()
                    processedSelectionsHelper.selectedPositions.value?.mapNotNullTo(materials) { position ->
                        processed.value?.get(position)?.material
                    }

                    processedSelectionsHelper.clearPositions()
                    manager.markGoodsUncounted(materials)
                }
                else -> throw IllegalArgumentException("Wrong pager position!")
            }
        }
    }

    fun onClickSave() {
        task.value?.let { task ->
            if (task.isExistUncountedGood()) {
                navigator.openDiscrepancyListScreen()
            } else {
                navigator.showMakeTaskCountedAndClose {
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
    }

    fun getCountTab(): Int {
        return if (manager.isWholesaleTaskType) 3 else 2
    }

    fun onPrint() {

    }

}

data class ItemGoodProcessingUi(
        val position: String,
        val name: String,
        val material: String,
        val providerCode: String,
        val quantity: String
)

data class ItemGoodProcessedUi(
        val position: String,
        val name: String,
        val quantity: String,
        val material: String,
        val providerCode: String
)
