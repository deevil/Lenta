package com.lenta.bp9.features.base

import com.lenta.bp9.model.task.TaskManufacturersForZBatches
import com.lenta.bp9.model.task.TaskZBatchInfo
import com.lenta.bp9.repos.IRepoInMemoryHolder

interface IBaseRepoInMemoryHolder {
    val repoInMemoryHolder: IRepoInMemoryHolder

    val taskZBatchesInfo: List<TaskZBatchInfo>?
        get() = repoInMemoryHolder.taskZBatchInfo.value

    val manufacturersForZBatches : List<TaskManufacturersForZBatches>?
        get() = repoInMemoryHolder.manufacturersForZBatches.value
}
