package com.lenta.bp15.features.discrepancy_list

import androidx.lifecycle.MutableLiveData
import com.lenta.bp15.platform.navigation.IScreenNavigator
import com.lenta.bp15.platform.resource.IResourceManager
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import javax.inject.Inject

class DiscrepancyListViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var resource: IResourceManager


    val title by lazy {
        "ПНБ(ТК)-303 / Постановка на баланс"
    }

    val discrepancyList = MutableLiveData(
            List((3..7).random()) {
                val position = (it + 1).toString()
                ItemDiscrepancyUi(
                        position = position,
                        name = "Test name $position",
                        quantity = (1..25).random().toString()
                )
            }
    )

    fun onClickSkip() {

    }

}