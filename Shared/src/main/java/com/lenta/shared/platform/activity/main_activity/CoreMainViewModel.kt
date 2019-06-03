package com.lenta.shared.platform.activity.main_activity

import com.lenta.shared.features.loading.ICoreLoadingViewModel
import com.lenta.shared.features.loading.TimerLoadingViewModel
import com.lenta.shared.platform.statusbar.StatusBarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel

abstract class CoreMainViewModel : CoreViewModel() {
    abstract var statusBarUiModel: StatusBarUiModel
    val topToolbarUiModel: TopToolbarUiModel = TopToolbarUiModel()
    val bottomToolbarUiModel: BottomToolbarUiModel = BottomToolbarUiModel()
    val loadingViewModel: ICoreLoadingViewModel = TimerLoadingViewModel()
    abstract fun onNewEnter()
    abstract fun showSimpleProgress(title: String)
    abstract fun hideProgress()
    abstract fun onPause()
}