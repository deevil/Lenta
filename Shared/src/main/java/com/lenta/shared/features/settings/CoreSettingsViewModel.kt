package com.lenta.shared.features.settings

import androidx.lifecycle.MutableLiveData
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import javax.inject.Inject

abstract class CoreSettingsViewModel : CoreViewModel(){

    @Inject
    lateinit var sessionInfo: ISessionInfo

    var isMainMenu: MutableLiveData<Boolean> = MutableLiveData(true)

    fun setIsMainMenu(userName: String?) {
        val auth: String? = userName
        isMainMenu = if (auth == null) MutableLiveData(false) else MutableLiveData(true)
    }

    abstract fun onClickBack()
    abstract fun onClickExit()
    abstract fun onClickPrinter()
    abstract fun onClickWork()
    abstract fun onClickTechLog()
    abstract fun onBackPressed()
}