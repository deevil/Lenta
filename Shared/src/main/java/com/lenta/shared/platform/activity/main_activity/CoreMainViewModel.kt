package com.lenta.shared.platform.activity.main_activity

import androidx.lifecycle.viewModelScope
import com.lenta.shared.account.IAuthenticator
import com.lenta.shared.auto_exit.AutoExitManager
import com.lenta.shared.features.loading.ICoreLoadingViewModel
import com.lenta.shared.features.loading.TimerLoadingViewModel
import com.lenta.shared.fmp.resources.dao_ext.getAutoExitTimeInMinutes
import com.lenta.shared.fmp.resources.fast.ZmpUtz14V001
import com.lenta.shared.only_one_app.LockManager
import com.lenta.shared.platform.high_priority.PriorityAppManager
import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.platform.statusbar.StatusBarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.Logg
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.Authenticator
import javax.inject.Inject

abstract class CoreMainViewModel : CoreViewModel() {
    abstract var statusBarUiModel: StatusBarUiModel
    @Inject
    lateinit var coreNavigator: ICoreNavigator
    @Inject
    lateinit var hyperHive: HyperHive
    @Inject
    lateinit var lockManager: LockManager
    @Inject
    lateinit var priorityAppManager: PriorityAppManager

    val zmpUtz14V001: ZmpUtz14V001 by lazy { ZmpUtz14V001(hyperHive) }
    val topToolbarUiModel: TopToolbarUiModel = TopToolbarUiModel()
    val bottomToolbarUiModel: BottomToolbarUiModel = BottomToolbarUiModel()
    val loadingViewModel: ICoreLoadingViewModel = TimerLoadingViewModel()
    var autoExitManager: AutoExitManager? = null
    abstract fun onNewEnter()
    abstract fun showSimpleProgress(title: String)
    abstract fun hideProgress()

    open fun onPause() {
        priorityAppManager.setHighPriority()
        autoExitManager?.setLastActiveTime()

    }

    fun onResume() {
        autoExitManager?.checkLastTime()
        priorityAppManager.setLowPriority()
        lockManager.getActiveAppPackageName()?.let { packageName ->
            coreNavigator.openAlertAnotherAppInProcess(packageName)
        }
    }

    init {
        viewModelScope.launch {
            autoExitManager = AutoExitManager {
                coreNavigator.finishApp(restart = false)
            }.apply {
                autoExitTimeInMinutes = withContext(Dispatchers.IO) {
                    return@withContext zmpUtz14V001.getAutoExitTimeInMinutes().apply {
                        Logg.d { "autoExitTimeInMinutes: $this" }
                    }
                }
            }

        }
    }

}