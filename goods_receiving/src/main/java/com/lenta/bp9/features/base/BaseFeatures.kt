package com.lenta.bp9.features.base

import android.content.Context
import com.lenta.bp9.features.delegates.ISaveProductDelegate
import com.lenta.bp9.features.delegates.SearchProductDelegate
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.shared.platform.time.ITimeMonitor
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

    @Inject
    lateinit var searchProductDelegate: SearchProductDelegate

    @Inject
    lateinit var saveProductDelegate: ISaveProductDelegate

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var dataBase: IDataBaseRepo

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var timeMonitor: ITimeMonitor
}