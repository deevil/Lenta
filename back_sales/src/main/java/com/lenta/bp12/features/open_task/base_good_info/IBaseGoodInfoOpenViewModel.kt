package com.lenta.bp12.features.open_task.base_good_info

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.model.IOpenTaskManager
import com.lenta.bp12.model.pojo.Basket
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.platform.resource.IResourceManager
import com.lenta.bp12.repository.IDatabaseRepository
import com.lenta.bp12.request.ScanInfoNetRequest
import com.lenta.bp12.request.pojo.ProviderInfo
import com.lenta.shared.account.ISessionInfo

interface IBaseGoodInfoOpenViewModel {
    var database: IDatabaseRepository
    var navigator: IScreenNavigator
    var resource: IResourceManager
    var scanInfoNetRequest: ScanInfoNetRequest
    var sessionInfo: ISessionInfo
    var manager: IOpenTaskManager

    val quantityFieldEnabled : MutableLiveData<Boolean>
    val quantity: MutableLiveData<Double>

    val totalTitle: MutableLiveData<String>
    val totalQuantity: MutableLiveData<Double>
    val totalWithUnits: MutableLiveData<String>

    val basketNumber: MutableLiveData<String>
    val basketQuantity: MutableLiveData<Double>
    val basketQuantityWithUnits: MutableLiveData<String>

    val sourceProviders: MutableLiveData<MutableList<ProviderInfo>>
    val providers: MutableLiveData<List<ProviderInfo>>
    val providerList: MutableLiveData<List<String?>>
    val providerEnabled: MutableLiveData<Boolean>
    val providerPosition: MutableLiveData<Int>
    val isProviderSelected: MutableLiveData<Boolean>

    val basketTitle: MutableLiveData<String>

    val applyEnabled: MutableLiveData<Boolean>
    val rollbackVisibility: MutableLiveData<Boolean>
    val closeVisibility: MutableLiveData<Boolean>
    val closeEnabled: MutableLiveData<Boolean>

    fun updateProviders(providers: MutableList<ProviderInfo>)
    fun onScanResult(number: String)

    fun checkSearchNumber(number: String)
    fun getBasket(): Basket?

    fun onClickApply()
    fun saveChangesAndExit()

    suspend fun saveChanges()
    fun onClickDetails()
    fun onClickRollback()
    fun onClickClose()
    fun onBackPressed()

    fun addProvider()

    fun getProvider(): ProviderInfo

    fun loadBoxInfo(number: String)
}