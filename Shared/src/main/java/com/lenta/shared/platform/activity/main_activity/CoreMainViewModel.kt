package com.lenta.shared.platform.activity.main_activity

import com.lenta.shared.platform.statusbar.StatusBarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel

abstract class CoreMainViewModel : CoreViewModel() {
    abstract var statusBarUiModel: StatusBarUiModel
    val topToolbarUiModel: TopToolbarUiModel = TopToolbarUiModel()
    val bottomToolbarUiModel: BottomToolbarUiModel = BottomToolbarUiModel()
    abstract fun onNewEnter()
}