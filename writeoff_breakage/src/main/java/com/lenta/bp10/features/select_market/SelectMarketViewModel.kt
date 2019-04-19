package com.lenta.bp10.features.select_market

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp10.fmp.resources.permissions.ZfmpUtzWob01V001
import com.lenta.bp10.requests.db.PermissionsDbRequest
import com.lenta.bp10.requests.network.FastResourcesNetRequest
import com.lenta.bp10.requests.network.SlowResourcesNetRequest
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class SelectMarketViewModel : CoreViewModel(), OnPositionClickListener {
    @Inject
    lateinit var permissionsDbRequest: PermissionsDbRequest
    @Inject
    lateinit var fastResourcesNetRequest: FastResourcesNetRequest
    @Inject
    lateinit var slowResourcesNetRequest: SlowResourcesNetRequest

    val markets: MutableLiveData<List<MarketUi>> = MutableLiveData()
    val marketsNames: MutableLiveData<List<String>> = markets.map { markets ->
        markets?.map { it.number }
    }
    val selectedPosition: MutableLiveData<Int> = MutableLiveData()
    val selectedAddress: MutableLiveData<String> = selectedPosition.map {
        it?.let {
            markets.value?.getOrNull(it)?.address
        }
    }

    init {
        viewModelScope.launch {
            permissionsDbRequest(null).either(::handleFailure, ::handlePermissions)
        }
    }


    private fun handlePermissions(list: List<ZfmpUtzWob01V001.ItemLocal_ET_WERKS>) {
        markets.value = list.map { MarketUi(number = it.werks, address = it.addres) }
        if (list.isNotEmpty() && selectedPosition.value == null) {
            selectedPosition.value = 0
        }
    }

    fun onClickNext() {
        viewModelScope.launch {
            Logg.d { "start loading resources" }
            fastResourcesNetRequest(null).either(::handleFailure, ::handleSuccessFastResources)
            slowResourcesNetRequest(null).either(::handleFailure, ::handleSuccessSlowResources)
            Logg.d { "finish loading resources" }
        }
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        Logg.d { "$failure" }
    }

    private fun handleSuccessFastResources(@Suppress("UNUSED_PARAMETER") b: Boolean) {
        Logg.d { "handleSuccessFastResources" }
    }

    private fun handleSuccessSlowResources(@Suppress("UNUSED_PARAMETER") b: Boolean) {
        Logg.d { "handleSuccessSlowResources" }
    }

    override fun onClickPosition(position: Int) {
        selectedPosition.value = position
    }

}

data class MarketUi(
        val number: String,
        val address: String

)