package com.lenta.bp14.models.check_price

import com.google.gson.Gson
import com.lenta.bp14.models.BaseTaskManager
import com.lenta.bp14.models.check_price.repo.ActualPriceRepoForTest
import com.lenta.bp14.models.check_price.repo.CheckPriceResultsRepo
import com.lenta.bp14.platform.IVibrateHelper
import com.lenta.bp14.platform.sound.ISoundPlayer
import com.lenta.shared.platform.time.ITimeMonitor

class CheckPriceTaskManager(private val timeMonitor: ITimeMonitor,
                            private val gson: Gson,
                            private val soundPlayer: ISoundPlayer,
                            private val vibrateHelper: IVibrateHelper,
                            private val priceInfoParser: IPriceInfoParser) : BaseTaskManager<ICheckPriceTask, CheckPriceTaskDescription>() {


    override fun newTask(taskDescription: CheckPriceTaskDescription): ICheckPriceTask? {
        _task = CheckPriceTask(
                taskDescription = taskDescription,
                actualPricesRepo = ActualPriceRepoForTest(),
                readyResultsRepo = CheckPriceResultsRepo(),
                priceInfoParser = priceInfoParser,
                gson = gson,
                soundPlayer = soundPlayer,
                vibrateHelper = vibrateHelper
        )
        return _task
    }

}