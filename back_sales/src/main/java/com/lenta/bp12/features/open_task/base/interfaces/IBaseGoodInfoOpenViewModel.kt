package com.lenta.bp12.features.open_task.base.interfaces

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.managers.interfaces.IOpenTaskManager
import com.lenta.bp12.model.pojo.Basket
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.platform.resource.IResourceManager
import com.lenta.bp12.repository.IDatabaseRepository
import com.lenta.bp12.request.ScanInfoNetRequest
import com.lenta.bp12.request.pojo.ProviderInfo
import com.lenta.shared.account.ISessionInfo

/**
 * Базовый интерфейс для viewmodel карточки товара в разделе Работа с заданиями
 * Имплементации:
 * @see BaseGoodInfoOpenViewModel
 * @see com.lenta.bp12.features.open_task.marked_good_info.MarkedGoodInfoOpenViewModel
 * @see com.lenta.bp12.features.open_task.good_info.GoodInfoOpenViewModel
 * */
interface IBaseGoodInfoOpenViewModel {
    var database: IDatabaseRepository
    var navigator: IScreenNavigator
    var resource: IResourceManager
    var scanInfoNetRequest: ScanInfoNetRequest
    var sessionInfo: ISessionInfo
    var manager: IOpenTaskManager

    val isWholesaleTaskType: MutableLiveData<Boolean>

    val quantityFieldEnabled : MutableLiveData<Boolean>
    val quantity: MutableLiveData<Double>

    val totalTitle: MutableLiveData<String>
    val totalQuantity: MutableLiveData<Double>
    val totalWithUnits: MutableLiveData<String>

    val basketNumber: LiveData<String>
    val basketQuantity: LiveData<Double>
    val basketQuantityWithUnits: MutableLiveData<String>

    val sourceProviders: MutableLiveData<MutableList<ProviderInfo>>
    val providers: MutableLiveData<List<ProviderInfo>>
    val providerList: MutableLiveData<List<String?>>
    val providerEnabled: MutableLiveData<Boolean>
    val providerPosition: MutableLiveData<Int>
    val isProviderSelected: MutableLiveData<Boolean>

    val basketTitle: MutableLiveData<String>

    val applyEnabled: LiveData<Boolean>
    val rollbackVisibility: MutableLiveData<Boolean>
    val closeVisibility: MutableLiveData<Boolean>
    val closeEnabled: MutableLiveData<Boolean>

    fun updateProviders(providers: MutableList<ProviderInfo>)
    fun onScanResult(number: String)

    fun checkSearchNumber(number: String)
    suspend fun getBasket(): Basket?

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

    fun isFactQuantityMoreThanPlanned(): Boolean
}