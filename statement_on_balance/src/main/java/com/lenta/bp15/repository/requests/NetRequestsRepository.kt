package com.lenta.bp15.repository.requests

import com.lenta.bp15.repository.requests.pojo.*
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.functional.flatMap
import com.lenta.shared.requests.FmpRequestsHelper
import com.lenta.shared.utilities.extentions.getResult
import javax.inject.Inject

class NetRequestsRepository @Inject constructor(
        private val fmpRequestsHelper: FmpRequestsHelper
) : INetRequestsRepository {

    override suspend fun getUserPermissions(params: UserPermissionsParams): Either<Failure, UserPermissionsResult> {
        return fmpRequestsHelper.restRequest(FMP_USER_PERMISSIONS, params, UserPermissionsStatus::class.java).getResult()
    }

    override suspend fun getTaskList(params: TaskListParams): Either<Failure, TaskListResult> {
        return fmpRequestsHelper.restRequest(FMP_TASK_LIST, params, TaskListStatus::class.java).getResult()
    }

    override suspend fun unlockTask(params: UnlockTaskParams): Either<Failure, UnlockTaskResult> {
        return fmpRequestsHelper.restRequest(FMP_LOCK_TASK, params, UnlockTaskStatus::class.java).getResult()
    }

    override suspend fun getTaskContent(params: TaskContentParams): Either<Failure, TaskContentResult> {
        return fmpRequestsHelper.restRequest(FMP_TASK_CONTENT, params, TaskContentStatus::class.java).getResult()
    }

    override suspend fun saveData(params: SaveDataParams): Either<Failure, Boolean> {
        val result = fmpRequestsHelper.restRequest(FMP_SAVE_DATA, params, SaveDataStatus::class.java)
        return result.flatMap {
            Either.Right(true)
        }
    }

    companion object {
        // Получение полномочий пользователя
        private const val FMP_USER_PERMISSIONS = "ZMP_UTZ_SOB_01_V001"

        // Получение списока заданий / поиск заданий
        private const val FMP_TASK_LIST = "ZMP_UTZ_SOB_02_V001"

        // Разблокировка задания
        private const val FMP_LOCK_TASK = "ZMP_UTZ_SOB_05_V001"

        // Получение состава задания
        private const val FMP_TASK_CONTENT = "ZMP_UTZ_SOB_03_V001"

        // Сохранение данных
        private const val FMP_SAVE_DATA = "ZMP_UTZ_SOB_04_V001"
    }

    internal class UserPermissionsStatus : ObjectRawStatus<UserPermissionsResult>()

    internal class TaskListStatus : ObjectRawStatus<TaskListResult>()

    internal class UnlockTaskStatus : ObjectRawStatus<UnlockTaskResult>()

    internal class TaskContentStatus : ObjectRawStatus<TaskContentResult>()

    internal class SaveDataStatus : ObjectRawStatus<SaveDataResult>()

}

interface INetRequestsRepository {

    suspend fun getUserPermissions(params: UserPermissionsParams): Either<Failure, UserPermissionsResult>

    suspend fun getTaskList(params: TaskListParams): Either<Failure, TaskListResult>

    suspend fun unlockTask(params: UnlockTaskParams): Either<Failure, UnlockTaskResult>

    suspend fun getTaskContent(params: TaskContentParams): Either<Failure, TaskContentResult>

    suspend fun saveData(params: SaveDataParams): Either<Failure, Boolean>

}