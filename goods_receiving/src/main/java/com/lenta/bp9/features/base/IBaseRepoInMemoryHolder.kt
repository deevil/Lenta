package com.lenta.bp9.features.base

import com.lenta.bp9.model.task.TaskManufacturersForZBatches
import com.lenta.bp9.repos.IRepoInMemoryHolder

interface IBaseRepoInMemoryHolder {
    val repoInMemoryHolder: IRepoInMemoryHolder

    val manufacturersForZBatches : List<TaskManufacturersForZBatches>?
        get() = repoInMemoryHolder.manufacturersForZBatches.value
}
