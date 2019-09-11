package com.lenta.bp14.models.check_price

import com.google.gson.Gson
import com.lenta.bp14.models.ITaskDescription
import com.lenta.bp14.models.ITaskManager
import com.lenta.bp14.models.check_price.repo.ActualPriceRepoForTest
import com.lenta.bp14.models.check_price.repo.CheckPriceResultsRepo
import com.lenta.bp14.models.general.ITaskType
import com.lenta.bp14.models.general.TaskTypes
import com.lenta.shared.platform.time.ITimeMonitor

class CheckPriceTaskManager(private val timeMonitor: ITimeMonitor, private val gson: Gson) : ITaskManager<ICheckPriceTask, CheckPriceTaskDescription> {

    private var checkPriceTask: ICheckPriceTask? = null

    override fun getTask(): ICheckPriceTask? {
        return checkPriceTask
    }

    override fun newTask(taskDescription: CheckPriceTaskDescription): ICheckPriceTask? {
        checkPriceTask = CheckPriceTask(
                taskDescription = taskDescription,
                actualPricesRepo = ActualPriceRepoForTest(),
                readyResultsRepo = CheckPriceResultsRepo(),
                priceInfoParser = PriceInfoParser(),
                timeMonitor = timeMonitor,
                gson = gson
        )
        return checkPriceTask
    }

    override fun clearTask(): Boolean {
        if (checkPriceTask == null) {
            return false
        }
        checkPriceTask = null
        return true
    }

    override fun setTask(inventoryTask: ICheckPriceTask?) {
        checkPriceTask = inventoryTask
    }

    override fun getCurrentTaskType(): ITaskType? {
        return if (getTask() != null) TaskTypes.CheckPrice.taskType else null
    }



}