package com.lenta.bp10.requests.db

import android.content.Context
import com.lenta.bp10.features.job_card.GisControl
import com.lenta.bp10.features.job_card.TaskSetting
import com.lenta.bp10.fmp.resources.dao_ext.getMaterialTypes
import com.lenta.bp10.fmp.resources.dao_ext.getMotionTypes
import com.lenta.bp10.fmp.resources.fast.ZmpUtz32V001
import com.lenta.bp10.fmp.resources.fast.ZmpUtz34V001
import com.lenta.bp10.models.task.TaskDescription
import com.lenta.bp10.models.task.TaskType
import com.lenta.bp10.models.task.WriteOffReason
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

class TaskDescriptionDbRequest @Inject constructor(
        private val hyperHive: HyperHive,
        private val sessionInfo: ISessionInfo,
        private val context: Context
) : UseCase<TaskDescription, TaskCreatingParams> {

    override suspend fun run(params: TaskCreatingParams): Either<Failure, TaskDescription> {
        val gisControls = params.gisControlList.map { it.code }
        val taskDescription = TaskDescription(
                taskName = params.taskName,
                taskType = TaskType(code = params.taskSetting.taskType, name = params.taskSetting.name),
                stock = params.stock,
                moveTypes = getMoveTypes(params, gisControls),
                gisControls = gisControls,
                materialTypes = getMaterialTypes(params),
                perNo = sessionInfo.personnelNumber.orEmpty(),
                printer = sessionInfo.printer.orEmpty(),
                tkNumber = sessionInfo.market.orEmpty(),
                ipAddress = context.getDeviceIp())

        return Either.Right(taskDescription)

    }

    private fun getMaterialTypes(params: TaskCreatingParams): List<String> {
        return ZmpUtz34V001(hyperHive).getMaterialTypes(params.taskSetting.taskType).mapNotNull { it.mtart }
    }


    private fun getMoveTypes(params: TaskCreatingParams, gisControls: List<String>): List<WriteOffReason> {
        return ZmpUtz32V001(hyperHive).getMotionTypes(params.taskSetting.taskType, gisControls)
                .mapNotNull {
                    it.takeIf {
                        it.reason != null && it.grtxt != null && it.taskCntrl != null
                    }?.run {
                        WriteOffReason(
                                code = it.reason.orEmpty(),
                                name = it.grtxt.orEmpty(),
                                gisControl = it.taskCntrl.orEmpty()
                        )
                    }
                }
    }
}

data class TaskCreatingParams(
        val taskName: String,
        val gisControlList: List<GisControl>,
        val taskSetting: TaskSetting,
        val stock: String
)
