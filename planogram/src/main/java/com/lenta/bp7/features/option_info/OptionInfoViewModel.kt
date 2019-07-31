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
            isFacings.value = database.getFacingsParam(checkData.marketNumber) == Enabled.YES.type
            isPlaces.value = database.getPlacesParam(checkData.marketNumber) == Enabled.YES.type

            saveCheckState()
        }
    }

    private fun saveCheckState() {
        checkData.countFacings = isFacings.value!!
        checkData.checkEmptyPlaces = isPlaces.value!!
    }

    fun onClickNext() {
        navigator.openSegmentListScreen()
    }
}
