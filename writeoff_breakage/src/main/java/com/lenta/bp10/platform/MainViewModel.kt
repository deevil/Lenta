package com.lenta.bp10.platform

import androidx.lifecycle.MutableLiveData
import com.lenta.bp10.platform.top_toolbar.TopToolbarUiModel

class MainViewModel : BaseViewModel() {
    val topToolbarUiModel: MutableLiveData<TopToolbarUiModel> = MutableLiveData()
}