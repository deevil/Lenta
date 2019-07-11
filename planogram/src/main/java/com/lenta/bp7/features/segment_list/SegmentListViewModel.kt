package com.lenta.bp7.features.segment_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp7.platform.navigation.IScreenNavigator
import com.lenta.bp7.repos.IDatabaseRepo
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.launch
import javax.inject.Inject

class SegmentListViewModel : CoreViewModel() {

    @Inject
    lateinit var hyperHive: HyperHive
    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var database: IDatabaseRepo

    private val storeNumber: MutableLiveData<String> = MutableLiveData("0000")

    init {
        viewModelScope.launch {
            storeNumber.value = sessionInfo.market
        }
    }

    fun getStoreNumber(): String? {
        return storeNumber.value
    }

    fun onClickSave() {
        // Заглушка
        navigator.openShelfListScreen()
    }
}
