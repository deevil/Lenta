package com.lenta.bp12.managers.interfaces

import com.lenta.bp12.model.WorkType
import com.lenta.bp12.request.SendTaskDataParams

/**
 * Имплементация:
 * @see com.lenta.bp12.managers.GeneralTaskManager
 * */
interface IGeneralTaskManager {
    fun setSendTaskDataParams(params: SendTaskDataParams, newWorkType: WorkType)
    fun getSendTaskDataParams(): SendTaskDataParams
    fun getWorkType(): WorkType
}