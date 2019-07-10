package com.lenta.bp7.features.option

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp7.data.Enabled
import com.lenta.bp7.platform.navigation.IScreenNavigator
import com.lenta.bp7.repos.IDatabaseRepo
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.launch
import javax.inject.Inject

class OptionViewModel : CoreViewModel() {

    @Inject
    lateinit var hyperHive: HyperHive
    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var database: IDatabaseRepo

    val isFacings: MutableLiveData<Boolean> = MutableLiveData(false)
    val isPlaces: MutableLiveData<Boolean> = MutableLiveData(false)

    init {
        viewModelScope.launch {
            val marketNumber = sessionInfo.market
            isFacings.value = database.getFacingsParam(marketNumber) != Enabled.NO.type
            isPlaces.value = database.getPlacesParam(marketNumber) != Enabled.NO.type
        }
    }

    fun onClickNext() {
        // Перейти к следующему экрану
        //navigator.openSomething()
    }
}
