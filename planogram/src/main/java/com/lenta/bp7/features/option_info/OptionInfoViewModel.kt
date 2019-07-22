package com.lenta.bp7.features.option_info

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp7.data.Enabled
import com.lenta.bp7.data.model.CheckData
import com.lenta.bp7.platform.navigation.IScreenNavigator
import com.lenta.bp7.repos.IDatabaseRepo
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.launch
import javax.inject.Inject

class OptionInfoViewModel : CoreViewModel() {

    @Inject
    lateinit var hyperHive: HyperHive
    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var database: IDatabaseRepo
    @Inject
    lateinit var checkData: CheckData

    val isFacings: MutableLiveData<Boolean> = MutableLiveData(false)
    val isPlaces: MutableLiveData<Boolean> = MutableLiveData(false)

    init {
        viewModelScope.launch {
            val marketNumber = sessionInfo.market
            isFacings.value = database.getFacingsParam(marketNumber) != Enabled.NO.type
            isPlaces.value = database.getPlacesParam(marketNumber) != Enabled.NO.type

            saveCheckState()
        }
    }

    private fun saveCheckState() {
        checkData.isFacings = isFacings.value!!
        checkData.isPlaces = isPlaces.value!!
    }

    fun onClickNext() {
        navigator.openSegmentListScreen()
    }
}
