package com.lenta.bp15.features.search_task

import androidx.lifecycle.MutableLiveData
import com.lenta.bp15.platform.navigation.IScreenNavigator
import com.lenta.bp15.platform.resource.IResourceManager
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import javax.inject.Inject

class SearchTaskViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var resource: IResourceManager


    val title by lazy {
        resource.tk(sessionInfo.market.orEmpty())
    }

    val search = MutableLiveData("")

    val requestFocusToSearch = MutableLiveData(false)

    val isMan = MutableLiveData(false)

    val isWoman = MutableLiveData(false)

    val isChildren = MutableLiveData(false)

    val isUnisex = MutableLiveData(false)

    val searchEnabled = MutableLiveData(false)

    fun onClickSearch() {

    }

    fun onScanResult(data: String) {

    }

}