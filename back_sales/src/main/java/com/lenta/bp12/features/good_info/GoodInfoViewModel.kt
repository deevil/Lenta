package com.lenta.bp12.features.good_info

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.account.ISessionInfo
import javax.inject.Inject

class GoodInfoViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo


    val title by lazy {
        "ТК - ${sessionInfo.market}"
    }

    val rollbackVisibility = MutableLiveData(true)

    val detailsVisibility = MutableLiveData(true)

    val missingVisibility = MutableLiveData(true)

    val rollbackEnabled = MutableLiveData(false)

    val missingEnabled = MutableLiveData(false)

    val applyEnabled = MutableLiveData(true)

    // -----------------------------

    fun onClickRollback() {

    }

    fun onClickDetails() {

    }

    fun onClickMissing() {

    }

    fun onClickApply() {

    }

}

data class GoodInfoUi(
        val position: String
)