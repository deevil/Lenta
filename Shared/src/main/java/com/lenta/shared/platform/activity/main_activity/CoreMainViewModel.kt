package com.lenta.shared.platform.activity.main_activity

import androidx.lifecycle.MutableLiveData
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel

open class CoreMainViewModel : CoreViewModel() {
    val topToolbarUiModel: MutableLiveData<TopToolbarUiModel> = MutableLiveData()
    val bottomToolbarUiModel: BottomToolbarUiModel = BottomToolbarUiModel()
}