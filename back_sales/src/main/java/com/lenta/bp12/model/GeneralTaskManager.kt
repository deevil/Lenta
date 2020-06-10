package com.lenta.bp12.model

import com.lenta.bp12.request.SendTaskDataParams
import com.lenta.shared.platform.time.ITimeMonitor
import javax.inject.Inject

class GeneralTaskManager @Inject constructor(
        private val timeMonitor: ITimeMonitor
) : IGeneralTaskManager {

    private lateinit var sendTaskDataParams: SendTaskDataParams

    override fun setSendTaskDataParams(params: SendTaskDataParams) {
        sendTaskDataParams = params
    }

    override fun getSendTaskDataParams(): SendTaskDataParams {
        return sendTaskDataParams
    }

}


interface IGeneralTaskManager {

    fun setSendTaskDataParams(params: SendTaskDataParams)
    fun getSendTaskDataParams(): SendTaskDataParams

}