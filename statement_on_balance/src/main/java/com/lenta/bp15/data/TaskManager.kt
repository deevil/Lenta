package com.lenta.bp15.data

import com.lenta.bp15.repository.database.IDatabaseRepository
import com.lenta.bp15.repository.net_requests.INetRequestsRepository
import javax.inject.Inject

class TaskManager @Inject constructor(
        private val database: IDatabaseRepository,
        private val netRequests: INetRequestsRepository
) : ITaskManager {



}


interface ITaskManager {


}