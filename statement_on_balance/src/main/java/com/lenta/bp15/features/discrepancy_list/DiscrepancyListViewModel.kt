package com.lenta.bp15.features.discrepancy_list

import androidx.lifecycle.map
import com.lenta.bp15.features.task_list.TaskListFragment
import com.lenta.bp15.model.ITaskManager
import com.lenta.bp15.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.mapSkipNulls
import javax.inject.Inject

class DiscrepancyListViewModel : CoreViewModel() {

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

    val discrepancyList by lazy {
        task.mapSkipNulls { task ->
            task.goods.filter { it.isExistUnprocessedMarks() }.mapIndexed { index, good ->
                good.convertToItemDiscrepancyUi(index)
            }
        }
    }

    /**
    Методы
     */

    fun onClickSkip() {
        navigator.showUnprocessedGoodsInTask(
                publishedCallback = {
                    manager.makeCurrentTaskUnfinished()
                    saveTaskData()
                },
                processedCallback = {
                    navigator.showRequiredToDestroyNonGluedMarks {
                        saveTaskData()
                    }
                }
        )
    }

    private fun saveTaskData() {
        launchUITryCatch {
            manager.saveTaskDataToServer(::handleSaveDataSuccess)
        }
    }

    private fun handleSaveDataSuccess() {
        navigator.goBackTo(TaskListFragment::class.simpleName)
        navigator.showSuccessSaveData()
    }

}