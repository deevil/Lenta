package com.lenta.shared.platform.activity.main_activity

import com.lenta.shared.auto_exit.AutoExitManager
import com.lenta.shared.exception.Failure
import com.lenta.shared.features.loading.ICoreLoadingViewModel
import com.lenta.shared.features.loading.TimerLoadingViewModel
import com.lenta.shared.fmp.resources.dao_ext.getAutoExitTimeInMinutes
import com.lenta.shared.fmp.resources.fast.ZmpUtz14V001
import com.lenta.shared.only_one_app.LockManager
import com.lenta.shared.platform.high_priority.PriorityAppManager
import com.lenta.shared.platform.navigation.ICoreNavigator
import com.lenta.shared.platform.statusbar.StatusBarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.settings.DefaultSettingsManager
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

abstract class CoreMainViewModel : CoreViewModel() {

    @Inject
    lateinit var coreNavigator: ICoreNavigator

    @Inject
    lateinit var hyperHive: HyperHive

    @Inject
    lateinit var lockManager: LockManager

    @Inject
    lateinit var priorityAppManager: PriorityAppManager

    @Inject
    lateinit var defaultSettingsManager: DefaultSettingsManager

    abstract var statusBarUiModel: StatusBarUiModel

    val zmpUtz14V001: ZmpUtz14V001 by lazy { ZmpUtz14V001(hyperHive) }
    val topToolbarUiModel: TopToolbarUiModel = TopToolbarUiModel()
    val bottomToolbarUiModel: BottomToolbarUiModel = BottomToolbarUiModel()
    val loadingViewModel: ICoreLoadingViewModel = TimerLoadingViewModel()
    var autoExitManager: AutoExitManager? = null

    abstract fun onNewEnter()
    abstract fun showSimpleProgress(title: String, handleFailure: ((Failure) -> Unit)? = null)
    abstract fun hideProgress()

    open fun onPause() {
        priorityAppManager.setHighPriority()
    }

    open fun onResume() {
        priorityAppManager.setLowPriority()
        lockManager.getActiveAppPackageName()?.let { packageName ->
            coreNavigator.openAlertAnotherAppInProcess(packageName)
        }
    }

    fun onUserInteraction() {
        autoExitManager?.setLastActiveTime()
    }

    init {
        launchUITryCatch {
            autoExitManager = AutoExitManager {
                preparingForExit()
                coreNavigator.finishApp(restart = false)
            }.apply {
                autoExitTimeInMinutes = withContext(Dispatchers.IO) {
                    if (defaultSettingsManager.isDefaultSettingsChanged()) {
                        coreNavigator.openChangedDefaultSettingsAlert(
                                noCallback = {
                                    defaultSettingsManager.saveLastDefaultSettingsToSettings()
                                },
                                yesCallback = {
                                    defaultSettingsManager.setNewDefaultSettings()
                                    preparingForExit()
                                    coreNavigator.finishApp(restart = true)
                                }
                        )
                    }

                    return@withContext zmpUtz14V001.getAutoExitTimeInMinutes().apply {
                        Logg.d { "autoExitTimeInMinutes: $this" }
                    }
                }
            }
        }
    }

    abstract fun preparingForExit()

}