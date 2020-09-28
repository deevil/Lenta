package com.lenta.bp10.features.job_card

import android.content.Context
import com.lenta.bp10.R
import com.lenta.bp10.fmp.resources.dao_ext.getMaterialTypes
import com.lenta.bp10.fmp.resources.fast.ZmpUtz33V001
import com.lenta.bp10.fmp.resources.fast.ZmpUtz34V001
import com.lenta.bp10.fmp.resources.fast.ZmpUtz36V001
import com.lenta.bp10.fmp.resources.gis_control.ZmpUtz35V001Rfc
import com.lenta.bp10.fmp.resources.tasks_settings.ZmpUtz29V001Rfc
import com.lenta.bp10.repos.IRepoInMemoryHolder
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.utilities.date_time.DateTimeUtil.formatCurrentDate
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class JobCardRepo @Inject constructor(
        val hyperHive: HyperHive,
        val context: Context,
        val sessionInfo: ISessionInfo,
        val repoInMemoryHolder: IRepoInMemoryHolder
) : IJobCardRepo {
    override suspend fun getAllTaskSettings(): List<TaskSetting> {
        return withContext(Dispatchers.IO) {
            @Suppress("INACCESSIBLE_TYPE")
            return@withContext ZmpUtz29V001Rfc(hyperHive).localHelper_ET_TASK_TPS.all.mapNotNull {
                it.takeIf { it.longName != null && it.taskType != null }?.run {
                    TaskSetting(
                            name = longName.orEmpty(),
                            motionType = bwart.orEmpty(),
                            taskType = taskType.orEmpty()
                    )
                }
            }
        }
    }

    override suspend fun getMaterialTypes(taskType: String?): List<String> {
        if (taskType == null) {
            return emptyList()
        }
        return withContext(Dispatchers.IO) {
            return@withContext ZmpUtz34V001(hyperHive).getMaterialTypes(taskType).mapNotNull {
                it.mtart
            }
        }
    }

    override suspend fun getGisControlList(taskType: String?): List<GisControl> {
        if (taskType == null) {
            return emptyList()
        }
        return withContext(Dispatchers.IO) {

            @Suppress("INACCESSIBLE_TYPE")
            return@withContext ZmpUtz35V001Rfc(hyperHive).localHelper_ET_CNTRL
                    .getWhere("TASK_TYPE = \"$taskType\"")
                    .mapNotNull {
                        ZmpUtz36V001(hyperHive).localHelper_ET_CNTRL_TXT
                                .getWhere("TASK_CNTRL = \"${it.taskCntrl}\"")
                                .getOrNull(0)?.cntrlTxt?.run {
                                    GisControl(
                                            name = this,
                                            code = it.taskCntrl.orEmpty()
                                    )
                                }
                    }
        }
    }

    @Suppress("INACCESSIBLE_TYPE")
    override suspend fun getStores(taskType: String?): List<String> {
        if (taskType == null) {
            return emptyList()
        }
        return withContext(Dispatchers.IO) {

            val lgortList = ZmpUtz33V001(hyperHive).localHelper_ET_LGORT
                    .getWhere("TASK_TYPE = \"$taskType\" and (WERKS = \"*\" OR WERKS = \"${sessionInfo.market}\")")

            if (lgortList.size == 1 && lgortList[0].lgort == "*") {
                return@withContext repoInMemoryHolder.stockLockRequestResult?.run { stocksLocks?.filter { it.locked == "" }?.mapNotNull { it.storloc } }.orEmpty()
            }

            return@withContext lgortList.mapNotNull {
                it.lgort
            }
        }
    }

    override fun generateNameTask(): String = context.getString(R.string.write_off_from, formatCurrentDate("dd.MM HH:mm"))
}

interface IJobCardRepo {
    suspend fun getAllTaskSettings(): List<TaskSetting>
    suspend fun getMaterialTypes(taskType: String?): List<String>
    suspend fun getGisControlList(taskType: String?): List<GisControl>
    suspend fun getStores(taskType: String?): List<String>
    fun generateNameTask(): String
}

data class TaskSetting(val name: String, val motionType: String, val taskType: String)
data class GisControl(val name: String, val code: String)