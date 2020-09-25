package com.lenta.bp12.features.create_task.base.interfaces

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.managers.interfaces.ICreateTaskManager
import com.lenta.bp12.model.pojo.Basket
import com.lenta.bp12.model.pojo.Good
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.platform.resource.IResourceManager
import com.lenta.bp12.repository.IDatabaseRepository
import com.lenta.bp12.request.ScanInfoNetRequest
import com.lenta.bp12.request.ScanInfoResult
import com.lenta.bp12.request.pojo.ProviderInfo
import com.lenta.shared.account.ISessionInfo

/**
 * Базовый интерфейс для viewmodel карточки товара в разделе Создание задания
 * Имплементации:
 * @see BaseGoodInfoCreateViewModel
 * @see com.lenta.bp12.features.create_task.good_info.GoodInfoCreateViewModel
 * @see com.lenta.bp12.features.create_task.marked_good_info.MarkedGoodInfoCreateViewModel
 * */
interface IBaseGoodInfoCreateViewModel {
    var database: IDatabaseRepository

    var navigator: IScreenNavigator
    var resource: IResourceManager
    var scanInfoNetRequest: ScanInfoNetRequest
    var sessionInfo: ISessionInfo
    var manager: ICreateTaskManager

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
    suspend fun getBasket(good: Good): Basket?

    fun updateData()
    fun onClickApply()
    fun saveChangesAndExit(result: ScanInfoResult? = null)

    suspend fun saveChanges(result: ScanInfoResult? = null)
    fun onClickDetails()
    fun onClickRollback()
    fun onClickClose()
    fun onBackPressed()

    fun addProvider()

    fun getProvider(): ProviderInfo

    fun loadBoxInfo(number: String)
}