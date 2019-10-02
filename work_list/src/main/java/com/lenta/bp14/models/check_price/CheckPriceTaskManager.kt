package com.lenta.bp14.models.check_price

import com.google.gson.Gson
import com.lenta.bp14.models.BaseTaskManager
import com.lenta.bp14.models.check_price.repo.ActualPriceRepo
import com.lenta.bp14.models.check_price.repo.CheckPriceResultsRepo
import com.lenta.bp14.platform.IVibrateHelper
import com.lenta.bp14.platform.sound.ISoundPlayer
import com.lenta.bp14.requests.check_price.ICheckPriceNetRequest
import com.lenta.shared.di.AppScope
import com.lenta.shared.platform.time.ITimeMonitor
import javax.inject.Inject

@AppScope
class CheckPriceTaskManager @Inject constructor(
        private val timeMonitor: ITimeMonitor,
        private val gson: Gson,
        private val soundPlayer: ISoundPlayer,
        private val vibrateHelper: IVibrateHelper,
        private val priceInfoParser: IPriceInfoParser,
        private val checkPriceRequest: ICheckPriceNetRequest
) : BaseTaskManager<ICheckPriceTask, CheckPriceTaskDescription>() {


    override fun newTask(taskDescription: CheckPriceTaskDescription): ICheckPriceTask? {
        _task = CheckPriceTask(
                taskDescription = taskDescription,
                actualPricesRepo = ActualPriceRepo(checkPriceRequest),
                readyResultsRepo = CheckPriceResultsRepo(),
                priceInfoParser = priceInfoParser,
                gson = gson,
                soundPlayer = soundPlayer,
                vibrateHelper = vibrateHelper
        )

        return _task
    }

    override fun getComponentClass(): Class<out Any> {
        return Any::class.java
    }

}