package com.lenta.bp15.repository.net_requests

import com.lenta.bp15.repository.net_requests.pojo.TaskListParams
import com.lenta.bp15.repository.net_requests.pojo.TaskListResult
import com.lenta.bp15.repository.net_requests.pojo.UserPermissionsParams
import com.lenta.bp15.repository.net_requests.pojo.UserPermissionsResult
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.utilities.extentions.getResult
import javax.inject.Inject

class NetRequestsRepository @Inject constructor(
        private val fmpRequestsHelper: FmpRequestsHelper
) : INetRequestsRepository {

    override suspend fun getUserPermissions(params: UserPermissionsParams): Either<Failure, UserPermissionsResult> {
        return fmpRequestsHelper.restRequest(FMP_USER_PERMISSIONS, params, UserPermissionsStatus::class.java)
    }

    override suspend fun getTaskList(params: TaskListParams): Either<Failure, TaskListResult> {
        return fmpRequestsHelper.restRequest(FMP_TASK_LIST, params, TaskListStatus::class.java).getResult()
    }

    companion object {
        // Разрешения
        private const val FMP_USER_PERMISSIONS = "ZMP_UTZ_BKS_01_V001" // todo Узнать наименование ресурса

        // Список заданий / поиск заданий
        private const val FMP_TASK_LIST = "ZMP_UTZ_SOB_02_V001"

        // Разблокировка заданий
        private const val FMP_LOCK_TASK = "ZMP_UTZ_SOB_05_V001"

        // Состав задания
        private const val FMP_TASK_CONTENT = "ZMP_UTZ_SOB_03_V001"

        // Сохранение даных
        private const val FMP_SAVE_DATA = "ZMP_UTZ_SOB_04_V001"
    }

    internal class UserPermissionsStatus : ObjectRawStatus<UserPermissionsResult>()

    internal class TaskListStatus : ObjectRawStatus<TaskListResult>()

}

interface INetRequestsRepository {

    suspend fun getUserPermissions(params: UserPermissionsParams): Either<Failure, UserPermissionsResult>

    suspend fun getTaskList(params: TaskListParams): Either<Failure, TaskListResult>

}