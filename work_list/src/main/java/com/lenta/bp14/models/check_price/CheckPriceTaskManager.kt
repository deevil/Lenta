package com.lenta.bp14.models.check_price

import com.google.gson.Gson
import com.lenta.bp14.models.ITaskManager
import com.lenta.bp14.models.check_price.repo.ActualPriceRepoForTest
import com.lenta.bp14.models.check_price.repo.CheckPriceResultsRepo
import com.lenta.shared.platform.time.ITimeMonitor

class CheckPriceTaskManager(private val timeMonitor: ITimeMonitor, private val gson: Gson) : ITaskManager<ICheckPriceTask, CheckPriceTaskDescription> {

    private var checkPriceTask: ICheckPriceTask? = null

    override fun getTask(): ICheckPriceTask? {
        return checkPriceTask
    }

    override fun newTask(taskDescription: CheckPriceTaskDescription?): ICheckPriceTask? {
        checkPriceTask = CheckPriceTask(
                actualPricesRepo = ActualPriceRepoForTest(),
                readyResultsRepo = CheckPriceResultsRepo(),
                priceInfoParser = PriceInfoParser(),
                timeMonitor = timeMonitor,
                gson = gson
        )
        return checkPriceTask
    }

    override fun clearTask() {
        checkPriceTask = null
    }

    override fun setTask(inventoryTask: ICheckPriceTask?) {
        checkPriceTask = inventoryTask
    }


}