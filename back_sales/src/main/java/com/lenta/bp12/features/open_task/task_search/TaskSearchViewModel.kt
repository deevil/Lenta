package com.lenta.bp12.features.open_task.task_search

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import javax.inject.Inject

class TaskSearchViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo


    val title by lazy {
        "ТК - ${sessionInfo.market}"
    }


    val provider = MutableLiveData("")

    val good = MutableLiveData("")

    val mark = MutableLiveData("")

    val section = MutableLiveData("")

    // -----------------------------

    fun onClickSearch() {

    }

}
