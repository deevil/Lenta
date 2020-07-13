package com.lenta.bp9.features.supply_results

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.R
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import javax.inject.Inject

class SupplyResultsViewModel : CoreViewModel() {

    @Inject
    lateinit var taskManager: IReceivingTaskManager
    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var context: Context

    val isAutomaticWriteOff: MutableLiveData<Boolean> = MutableLiveData()
    val message: MutableLiveData<String> = MutableLiveData("")
    val numberSupply: MutableLiveData<String> = MutableLiveData("")

    fun getTitle(): String {
        return taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }

    fun onClickDocs() {
        screenNavigator.openFormedDocsScreen()
    }

    fun onClickNext() {
        if (isAutomaticWriteOff.value == false) {
            nextSupplyResultsSuccess()
        } else {
            specialControlGoods()
        }
    }

    private fun nextSupplyResultsSuccess() {
        if (taskManager.getReceivingTask()?.taskDescription?.isWO == true) {
            screenNavigator.openSupplyResultsScreen(
                    pageNumber = "76",
                    numberSupply = numberSupply.value ?: "",
                    isAutomaticWriteOff = true
            )
        } else {
            specialControlGoods()
        }
    }

    private fun specialControlGoods() {
        if (taskManager.getReceivingTask()?.taskDescription?.isSpecialControlGoods == true) {
            /**В задании присутствуют товары особого контроля. Необходимо указать представителей от секций получивших товар" и переход на обработку секций*/
            screenNavigator.openTransferGoodsSectionScreen()
            screenNavigator.openAlertHaveIsSpecialGoodsScreen()
        } else {
            /**В задании отсутствуют товары особого контроля. Товары будут автоматически переданы в секцию." и переход на список заданий*/
            screenNavigator.openMainMenuScreen()
            screenNavigator.openTaskListScreen()
            screenNavigator.openAlertNoIsSpecialGoodsScreen()
        }
    }
}
