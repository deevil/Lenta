package com.lenta.shared.platform.main_activity

import androidx.lifecycle.MutableLiveData
import com.lenta.shared.platform.viewmodel.BaseViewModel
import com.lenta.shared.platform.top_toolbar.TopToolbarUiModel

class MainViewModel : BaseViewModel() {
    val topToolbarUiModel: MutableLiveData<TopToolbarUiModel> = MutableLiveData()
}