package com.lenta.bp15.features.good_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.lenta.bp15.features.task_list.TaskListFragment
import com.lenta.bp15.model.ITaskManager
import com.lenta.bp15.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.actionByNumber
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.mapSkipNulls
import javax.inject.Inject

class GoodListViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var manager: ITaskManager


    /**
    Переменные
     */

    private val task by lazy {
        manager.currentTask
    }

    val title by lazy {
        task.map { it.getCodeWithName() }
    }

    val numberField = MutableLiveData("")

    val requestFocusToNumberField = MutableLiveData(false)

    val processingList by lazy {
        task.mapSkipNulls { task ->
            task.goods.filter { !it.isProcessed() }.mapIndexed { index, good ->
                good.convertToItemGoodUi(index)
            }
        }
    }

    val processedList by lazy {
        task.mapSkipNulls { task ->
            task.goods.filter { it.isProcessed() }.mapIndexed { index, good ->
                good.convertToItemGoodUi(index)
            }
        }
    }

    /**
    Методы
     */

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    override fun onOkInSoftKeyboard(): Boolean {
        numberField.value?.let { number ->
            if (number.isNotEmpty()) {
                onScanResult(number)
            }
        }

        return true
    }

    fun onScanResult(data: String) {
        actionByNumber(
                number = data,
                funcForEan = { ean, _ -> actionWithEan(ean)},
                funcForMaterial = ::openGoodByMaterial,
                funcForNotValidFormat = navigator::showIncorrectEanFormat
        )
    }

    private fun actionWithEan(ean: String) {
        launchUITryCatch {
            manager.getMaterialByEan(ean)?.let {material ->
                openGoodByMaterial(material)
            } ?: navigator::showIncorrectEanFormat
        }
    }

    fun onClickItemProcessingPosition(position: Int) {
        processingList.value?.getOrNull(position)?.material?.let { material ->
            openGoodByMaterial(material)
        }
    }

    fun onClickItemProcessedPosition(position: Int) {
        processedList.value?.getOrNull(position)?.material?.let { material ->
            openGoodByMaterial(material)
        }
    }

    private fun openGoodByMaterial(material: String) {
        task.value?.let { task ->
            task.goods.find { it.material == material }?.let { good ->
                manager.updateCurrentGood(good)
                navigator.openGoodInfoScreen()
            } ?: navigator.showGoodIsMissingFromTask(material)
        }
    }

    fun onClickComplete() {
        if (isExistUnprocessedGoods()) {
            navigator.openDiscrepancyListScreen()
        } else {
            launchUITryCatch {
                manager.saveTaskDataToServer(::handleSaveDataSuccess)
            }
        }
    }

    private fun handleSaveDataSuccess() {
        navigator.goBackTo(TaskListFragment::class.simpleName)
        navigator.showSuccessSaveData()
    }

    private fun isExistUnprocessedGoods(): Boolean {
        return processingList.value?.isNotEmpty() ?: false
    }

}