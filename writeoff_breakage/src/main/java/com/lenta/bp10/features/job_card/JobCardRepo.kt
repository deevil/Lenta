package com.lenta.bp10.features.job_card

import android.content.Context
import com.lenta.bp10.R
import com.lenta.bp10.fmp.resources.fast.ZmpUtz34V001
import com.lenta.bp10.fmp.resources.fast.ZmpUtz36V001
import com.lenta.bp10.fmp.resources.gis_control.ZmpUtz35V001Rfc
import com.lenta.bp10.fmp.resources.permissions.ZfmpUtzWob01V001
import com.lenta.bp10.fmp.resources.tasks_settings.ZmpUtz29V001Rfc
import com.lenta.shared.utilities.date_time.DateTimeUtil.formatCurrentDate
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class JobCardRepo @Inject constructor(val hyperHive: HyperHive, val context: Context) : IJobCardRepo {
    override suspend fun getAllTaskSettings(): List<TaskSetting> {
        return withContext(Dispatchers.IO) {
            return@withContext ZmpUtz29V001Rfc(hyperHive).localHelper_ET_TASK_TPS.all.map {
                TaskSetting(
                        name = it.longName,
                        motionType = it.bwart,
                        taskType = it.taskType
                )
            }
        }
    }

    override suspend fun getProductTypes(taskType: String?): List<String> {
        if (taskType == null) {
            return emptyList()
        }
        return withContext(Dispatchers.IO) {
            return@withContext ZmpUtz34V001(hyperHive).localHelper_ET_MTART.getWhere("TASK_TYPE = \"$taskType\"").map {
                it.mtart
            }
        }
    }

    override suspend fun getGisControlList(taskType: String?): List<GisControl> {
        if (taskType == null) {
            return emptyList()
        }
        return withContext(Dispatchers.IO) {

            return@withContext ZmpUtz35V001Rfc(hyperHive).localHelper_ET_CNTRL
                    .getWhere("TASK_TYPE = \"$taskType\"").map {
                        GisControl(
                                name =
                                ZmpUtz36V001(hyperHive).localHelper_ET_CNTRL_TXT
                                        .getWhere("TASK_CNTRL = \"${it.taskCntrl}\"")
                                        .getOrNull(0)?.cntrlTxt ?: "",
                                code = it.taskCntrl
                        )

                    }
        }
    }

    override suspend fun getStores(): List<String> {
        //TODO (BD) Нужно сделать правильную выюорку складов
        return withContext(Dispatchers.IO) {
            return@withContext ZfmpUtzWob01V001(hyperHive).localHelper_ET_WERKS
                    .getAll().map {
                        it.werks
                    }
        }
    }


    override fun generateNameTask(): String = context.getString(R.string.write_off_from, formatCurrentDate("dd.MM HH:mm"))


}

interface IJobCardRepo {
    suspend fun getAllTaskSettings(): List<TaskSetting>
    suspend fun getProductTypes(taskType: String?): List<String>
    suspend fun getGisControlList(taskType: String?): List<GisControl>
    suspend fun getStores(): List<String>
    fun generateNameTask(): String
}

data class TaskSetting(val name: String, val motionType: String, val taskType: String)
data class GisControl(val name: String, val code: String)