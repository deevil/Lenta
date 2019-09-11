package com.lenta.bp14.models.check_list

import androidx.lifecycle.LiveData
import com.google.gson.Gson
import com.lenta.bp14.models.ITask
import com.lenta.bp14.models.check_price.ICheckPriceResult
import com.lenta.bp14.models.check_price.repo.ICheckPriceResultsRepo
import com.lenta.shared.platform.time.ITimeMonitor


class CheckPriceTask(
        private val readyResultsRepo: ICheckListResultsRepo,
        private val timeMonitor: ITimeMonitor,
        private val gson: Gson
) : ICheckListTask {

    override fun getCheckResults(): LiveData<List<ICheckPriceResult>> {
        return readyResultsRepo.getCheckListResults()
    }

}

interface ICheckListTask : ITask {

    fun getCheckResults(): LiveData<List<ICheckPriceResult>>

}



