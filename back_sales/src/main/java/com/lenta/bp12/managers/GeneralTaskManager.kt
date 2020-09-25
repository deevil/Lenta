package com.lenta.bp12.managers

import com.lenta.bp12.managers.interfaces.IGeneralTaskManager
import com.lenta.bp12.model.WorkType
import com.lenta.bp12.request.SendTaskDataParams
import com.lenta.shared.platform.time.ITimeMonitor
import javax.inject.Inject
import kotlin.properties.Delegates

class GeneralTaskManager @Inject constructor(
        private val timeMonitor: ITimeMonitor
) : IGeneralTaskManager {

    private var taskDataParams by Delegates.notNull<SendTaskDataParams>()
    private var workType: WorkType = WorkType.OPEN

    override fun setSendTaskDataParams(params: SendTaskDataParams, newWorkType: WorkType) {
        taskDataParams = params
        workType = newWorkType
    }

    override fun getSendTaskDataParams(): SendTaskDataParams {
        return taskDataParams
    }

    override fun getWorkType(): WorkType {
        return workType
    }

}