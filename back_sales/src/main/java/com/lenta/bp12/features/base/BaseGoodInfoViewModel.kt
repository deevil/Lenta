package com.lenta.bp12.features.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.lenta.bp12.managers.interfaces.ITaskManager
import com.lenta.bp12.model.Taskable
import com.lenta.bp12.model.pojo.Basket
import com.lenta.bp12.model.pojo.Good
import com.lenta.bp12.model.pojo.extentions.getQuantityOfGood
import com.lenta.bp12.platform.FIRST_BASKET
import com.lenta.bp12.platform.FIRST_POSITION
import com.lenta.bp12.platform.ZERO_QUANTITY
import com.lenta.bp12.platform.extention.isWholesaleType
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.platform.resource.IResourceManager
import com.lenta.bp12.repository.IDatabaseRepository
import com.lenta.bp12.request.GoodInfoNetRequest
import com.lenta.bp12.request.ScanInfoNetRequest
import com.lenta.bp12.request.ScanInfoResult
import com.lenta.bp12.request.pojo.ProviderInfo
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.*
import com.lenta.shared.utilities.orIfNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Карточка товара
 * Дети:
 * @see com.lenta.bp12.features.open_task.base.BaseGoodInfoOpenViewModel
 * @see com.lenta.bp12.features.open_task.marked_good_info.MarkedGoodInfoOpenViewModel
 * @see com.lenta.bp12.features.open_task.good_info.GoodInfoOpenViewModel
 *
 * @see com.lenta.bp12.features.create_task.base.BaseGoodInfoCreateViewModel
 * @see com.lenta.bp12.features.create_task.good_info.GoodInfoCreateViewModel
 * @see com.lenta.bp12.features.create_task.marked_good_info.MarkedGoodInfoCreateViewModel
 * */
abstract class BaseGoodInfoViewModel<R : Taskable, T : ITaskManager<R>> : CoreViewModel(),
        OnOkInSoftKeyboardListener {

    /** "ZMP_UTZ_BKS_05_V001"
     * Получение данных товара по ШК / SAP-коду
     */
    @Inject
    lateinit var goodInfoNetRequest: GoodInfoNetRequest

    /** "ZMP_UTZ_100_V001"
     * Получение данных по акцизному товару  */
    @Inject
    lateinit var scanInfoNetRequest: ScanInfoNetRequest

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var database: IDatabaseRepository

    @Inject
    lateinit var resource: IResourceManager

    /**
    Количество товара итого
     */

    val totalQuantity by unsafeLazy {
        good.combineLatest(quantity).mapSkipNulls {
            val total = it.first.getTotalQuantity()
            val current = it.second

            total.sumWith(current)
        }
    }

    /**
     * Корзины
     *
     * Номер корзины, если есть подходящая под товар корзина, то берем ее индекс,
     * если нет то берем следующий номер от последней, если последний нет то номер будет 1
     * */

    val basketNumber by unsafeLazy {
        isProviderSelected.switchMap { isProviderSelected ->
            task.switchMap { task ->
                asyncTryCatchLiveData {
                    if (isProviderSelected) {
                        getBasket()?.index?.toString()
                                .orIfNull {
                                    task.baskets.lastOrNull()?.index?.plus(1)?.toString()
                                            .orIfNull { FIRST_BASKET }
                                }
                    } else ""
                }
            }
        }
    }

    /**
     * Количество по корзине
     * */
    val basketQuantity by unsafeLazy {
        good.switchMap { good ->
            quantity.switchMap { enteredQuantity ->
                isProviderSelected.switchMap { isProviderSelected ->
                    asyncTryCatchLiveData {
                        good.takeIf { isProviderSelected }?.run {
                            getBasket()?.getQuantityOfGood(this)?.sumWith(enteredQuantity)
                                    .orIfNull { enteredQuantity }
                        }.orIfNull { ZERO_QUANTITY }
                    }
                }
            }
        }
    }

    val basketQuantityWithUnits by unsafeLazy {
        good.combineLatest(basketQuantity).mapSkipNulls {
            val (good, quantity) = it
            "${quantity.dropZeros()} ${good.commonUnits.name}"
        }
    }

    /**
    Список поставщиков
     */

    val sourceProviders = MutableLiveData(mutableListOf<ProviderInfo>())

    open val quantityFieldEnabled by unsafeLazy {
        MutableLiveData(false)
    }

    val task by unsafeLazy {
        manager.currentTask
    }

    val good by unsafeLazy {
        manager.currentGood
    }

    val isWholesaleTaskType by lazy {
        task.mapSkipNulls {
            it.type?.isWholesaleType() == true
        }
    }

    val title by unsafeLazy {
        good.map { good ->
            good?.getNameWithMaterial() ?: task.value?.getFormattedName()
        }
    }

    val closeVisibility by unsafeLazy {
        task.map { task ->
            task?.type?.isWholesaleType() == true
        }
    }

    open val rollbackVisibility = MutableLiveData(true)

    /**
    Количество товара по корзинам
     */
    val basketTitle by unsafeLazy {
        MutableLiveData(resource.byBasket())
    }

    val totalTitle by unsafeLazy {
        good.mapSkipNulls { good ->
            resource.totalWithConvertingInfo(good.getConvertingInfo())
        }
    }

    val providers = sourceProviders.mapSkipNulls {
        val list = it.toMutableList()
        if (list.size > 1) {
            list.add(0,
                    ProviderInfo(
                            name = resource.chooseProvider()
                    ))
        }

        list.toList()
    }

    val providerList by unsafeLazy {
        providers.mapSkipNulls { list ->
            list.map { it.name }
        }
    }

    val providerEnabled by unsafeLazy {
        providerList.map { providers ->
            providers?.size ?: 0 > 1
        }
    }

    val providerPosition = MutableLiveData(FIRST_POSITION)

    val isProviderSelected = providerEnabled.combineLatest(providerPosition).map {
        val isEnabled = it?.first ?: false
        val position = it?.second ?: FIRST_POSITION

        isProviderEnabledAndPositionChanged(isEnabled, position) or
                isProviderNotEnabledAndPositionDidntChanged(isEnabled, position)
    }

    abstract var manager: T
    abstract val quantity: MutableLiveData<Double>
    abstract val applyEnabled: LiveData<Boolean>
    abstract val totalWithUnits: MutableLiveData<String>
    abstract val closeEnabled: MutableLiveData<Boolean>

    private fun isProviderEnabledAndPositionChanged(isEnabled: Boolean, position: Int) = isEnabled && position > FIRST_POSITION
    private fun isProviderNotEnabledAndPositionDidntChanged(isEnabled: Boolean, position: Int) = !isEnabled && position == FIRST_POSITION

    protected suspend fun findGoodByMaterial(material: String): Good? {
        navigator.showProgressLoadingData()
        val foundGood = withContext(Dispatchers.IO) { manager.findGoodByMaterial(material) }
        navigator.hideProgress()
        return foundGood
    }

    fun onClickClose() {
        navigator.showCloseBasketDialog(::handleYesOnClickCloseCallback)
    }

    private fun handleYesOnClickCloseCallback() {
        manager.isBasketsNeedsToBeClosed = true
        saveChangesAndExit()
    }

    fun getProvider(): ProviderInfo {
        var provider = ProviderInfo.getEmptyProvider()
        if (isProviderSelected.value == true) {
            providers.value?.let { providers ->
                providerPosition.value?.let { position ->
                    provider = providers.getOrNull(position).orIfNull { ProviderInfo.getEmptyProvider() }
                }
            }
        }

        return provider
    }

    open fun onScanResult(number: String) {
        launchUITryCatch {
            if (isProviderSelected.value == true) {
                checkSearchNumber(number)
            } else {
                navigator.showChooseProviderFirst()
            }
        }
    }

    fun updateProviders(providers: MutableList<ProviderInfo>) {
        sourceProviders.value = providers
    }

    fun addProvider() {
        navigator.openAddProviderScreen()
    }

    fun onClickDetails() {
        manager.updateCurrentGood(good.value)
        navigator.openGoodDetailsCreateScreen()
    }

    override fun onOkInSoftKeyboard(): Boolean {
        return if (applyEnabled.value == true) {
            onClickApply()
            true
        } else {
            false
        }
    }

    suspend fun getBasket(): Basket? {
        val good = good.value
        return good?.let {
            manager.getBasket(it.provider.code.orEmpty(), it, false)
        }
    }

    abstract fun checkSearchNumber(number: String)
    abstract fun onClickApply()
    abstract fun saveChangesAndExit(result: ScanInfoResult? = null)
    abstract suspend fun saveChanges(result: ScanInfoResult? = null)
    abstract fun onClickRollback()
    abstract fun onBackPressed()
    abstract fun loadBoxInfo(number: String)
}