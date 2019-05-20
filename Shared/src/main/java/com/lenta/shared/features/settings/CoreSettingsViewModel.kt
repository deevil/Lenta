package com.lenta.shared.features.settings

import androidx.lifecycle.MutableLiveData
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

abstract class CoreSettingsViewModel : CoreViewModel(){

    @Inject
    lateinit var hyperHive: HyperHive

    var isMainMenu: MutableLiveData<Boolean> = MutableLiveData(true)

    abstract fun onClickBack()
    abstract fun onClickPrinter()
    abstract fun onClickWork()
    abstract fun onClickTechLog()
    abstract fun onBackPressed()
}