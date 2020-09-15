package com.lenta.bp9.features.base

import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.shared.platform.viewmodel.CoreViewModel
import javax.inject.Inject

abstract class BaseFeatures : CoreViewModel(),
        IBaseTaskManager,
        IBaseRepoInMemoryHolder
{
    @Inject
    override lateinit var taskManager: IReceivingTaskManager

    @Inject
    override lateinit var repoInMemoryHolder: IRepoInMemoryHolder
}