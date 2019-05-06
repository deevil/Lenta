package com.lenta.bp10.requests.db

import android.content.Context
import com.lenta.bp10.features.job_card.TaskSetting
import com.lenta.bp10.models.task.TaskDescription
import com.lenta.bp10.models.task.TaskType
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

class TaskDescriptionDbRequest
@Inject constructor(private val hyperHive: HyperHive,
                    private val sessionInfo: ISessionInfo,
                    private val context: Context) : UseCase<TaskDescription, TaskCreatingParams>() {

    override suspend fun run(params: TaskCreatingParams): Either<Failure, TaskDescription> {
        val taskDescription: TaskDescription = TaskDescription(
                taskName = params.taskName,
                taskType = TaskType(code = params.taskSetting.taskType, name = params.taskSetting.name),
                stock = params.stock,
                moveTypes = getMoveTypes(params),
                gisControls = getGisControls(params),
                materialTypes = getMaterialTypes(params),
                perNo = sessionInfo.personnelNumber?:"",
                printer = sessionInfo.printer ?: "",
                tkNumber = sessionInfo.market!!,
                ipAddress = context.getDeviceIp())

        return Either.Right(taskDescription)

    }

    private fun getMaterialTypes(params: TaskCreatingParams): List<String> {
        //TODO need to implement
        return emptyList()
    }

    private fun getGisControls(params: TaskCreatingParams): List<String> {
        return emptyList()
    }

    private fun getMoveTypes(params: TaskCreatingParams): List<String> {
        return emptyList()
    }


}

data class TaskCreatingParams(
        var taskName: String,
        val taskSetting: TaskSetting,
        val stock: String
)
