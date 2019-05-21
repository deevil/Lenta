package com.lenta.bp10.features.select_market

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp10.fmp.resources.permissions.ZfmpUtzWob01V001
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.bp10.requests.db.PermissionsDbRequest
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.settings.IAppSettings
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class SelectMarketViewModel : CoreViewModel(), OnPositionClickListener {
    @Inject
    lateinit var permissionsDbRequest: PermissionsDbRequest
    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var appSettings: IAppSettings


    private val markets: MutableLiveData<List<MarketUi>> = MutableLiveData()
    val marketsNames: MutableLiveData<List<String>> = markets.map { markets ->
        markets?.map { it.number }
    }
    val selectedPosition: MutableLiveData<Int> = MutableLiveData()
    val selectedAddress: MutableLiveData<String> = selectedPosition.map {
        it?.let { position ->
            markets.value?.getOrNull(position)?.address
        }
    }

    init {
        viewModelScope.launch {
            permissionsDbRequest(null).either(::handleFailure, ::handlePermissions)
        }
    }


    private fun handlePermissions(list: List<ZfmpUtzWob01V001.ItemLocal_ET_WERKS>) {
        markets.value = list.map { MarketUi(number = it.werks, address = it.addres) }

        if (selectedPosition.value == null) {
            if (appSettings.lastTK != null) {
                list.forEachIndexed { index, itemLocal_ET_WERKS ->
                    if (itemLocal_ET_WERKS.werks == appSettings.lastTK) {
                        onClickPosition(index)
                    }
                }
            } else {
                onClickPosition(0)
            }
        }



        if (list.size == 1) {
            onClickNext()
        }
    }

    fun onClickNext() {
        screenNavigator.openFastDataLoadingScreen()
    }


    override fun onClickPosition(position: Int) {
        selectedPosition.value = position
        markets.value?.getOrNull(position)?.number?.let {
            sessionInfo.market = it
            appSettings.lastTK = it
        }
    }

}

data class MarketUi(
        val number: String,
        val address: String

)