package com.lenta.bp12.features.create_task.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.lenta.bp12.features.create_task.base.interfaces.IBaseGoodInfoCreateViewModel
import com.lenta.bp12.model.pojo.Basket
import com.lenta.bp12.model.pojo.Good
import com.lenta.bp12.model.pojo.extentions.getQuantityOfGood
import com.lenta.bp12.platform.FIRST_POSITION
import com.lenta.bp12.platform.ZERO_QUANTITY
import com.lenta.bp12.platform.extention.isWholesaleType
import com.lenta.bp12.request.pojo.ProviderInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.*
import com.lenta.shared.utilities.orIfNull

/**
 * Класс содержащий общую логику для
 * @see com.lenta.bp12.features.create_task.good_info.GoodInfoCreateViewModel
 * @see com.lenta.bp12.features.create_task.marked_good_info.MarkedGoodInfoCreateViewModel
 * */
abstract class BaseGoodInfoCreateViewModel : CoreViewModel(), IBaseGoodInfoCreateViewModel {
    val task by unsafeLazy {
        manager.currentTask
    }

    val good by unsafeLazy {
        manager.currentGood
    }

    val title by unsafeLazy {
        good.map { good ->
            good?.getNameWithMaterial() ?: task.value?.getFormattedName()
        }
    }

    override val isWholesaleTaskType by lazy {
        task.mapSkipNulls {
            it.type.isWholesaleType()
        }
    }

    /**
    Ввод количества
     */

    override val quantityFieldEnabled by unsafeLazy {
        MutableLiveData(false)
    }

    /**
    Количество товара итого
     */

    override val totalTitle by unsafeLazy {
        good.map { good ->
            resource.totalWithConvertingInfo(good?.getConvertingInfo().orEmpty())
        }
    }

    override val totalQuantity by unsafeLazy {
        good.combineLatest(quantity).map {
            it?.let {
                val total = it.first.getTotalQuantity()
                val current = it.second

                total.sumWith(current)
            }
        }
    }

    override val totalWithUnits by unsafeLazy {
        totalQuantity.map { quantity ->
            "${quantity.dropZeros()} ${good.value?.commonUnits?.name}"
        }
    }

    /**
     * Корзины
     * */
    override val basketNumber by unsafeLazy {
        good.combineLatest(quantity)
                .combineLatest(isProviderSelected)
                .switchMap {
                    asyncTryCatchLiveData {
                        val good = it.first.first
                        val isProviderSelected = it.second

                        good.takeIf { isProviderSelected }
                                ?.run { getBasket(this)?.index?.toString() }
                                .orIfNull {
                                    task.value?.baskets?.size?.plus(1)?.toString()
                                }.orEmpty()
                    }
                }
    }

    override val basketQuantity by unsafeLazy {
        good.combineLatest(quantity)
                .combineLatest(isProviderSelected)
                .switchMap {
                    asyncTryCatchLiveData {
                        val good = it.first.first
                        val enteredQuantity = it.first.second
                        val isProviderSelected = it.second

                        good.takeIf { isProviderSelected }?.run {
                            getBasket(this)?.run {
                                getQuantityOfGood(good).sumWith(enteredQuantity)
                            }.orIfNull {
                                enteredQuantity
                            }
                        }.orIfNull { ZERO_QUANTITY }
                    }
                }
    }

    override val basketQuantityWithUnits by unsafeLazy {
        good.combineLatest(basketQuantity).mapSkipNulls {
            val (good, quantity) = it
            "${quantity.dropZeros()} ${good.commonUnits.name}"
        }
    }

    /**
    Список поставщиков
     */

    final override val sourceProviders = MutableLiveData(mutableListOf<ProviderInfo>())

    override val providers = sourceProviders.mapSkipNulls {
        val list = it.toMutableList()
        if (list.size > 1) {
            list.add(0,
                    ProviderInfo(
                            name = resource.chooseProvider()
                    ))
        }

        list.toList()
    }

    override val providerList by unsafeLazy {
        providers.map { list ->
            list?.map { it.name }
        }
    }

    override val providerEnabled by unsafeLazy {
        providerList.map { providers ->
            providers?.size ?: 0 > 1
        }
    }

    override val providerPosition = MutableLiveData(FIRST_POSITION)

    override val isProviderSelected by unsafeLazy {
        providerEnabled.combineLatest(providerPosition).map {
            val isEnabled = it?.first ?: false
            val position = it?.second ?: FIRST_POSITION

            isProviderEnabledAndPositionChanged(isEnabled, position) or
                    isProviderNotEnabledAndPositionDidntChanged(isEnabled, position)
        }
    }

    private fun isProviderEnabledAndPositionChanged(isEnabled: Boolean, position: Int) = isEnabled && position > FIRST_POSITION
    private fun isProviderNotEnabledAndPositionDidntChanged(isEnabled: Boolean, position: Int) = !isEnabled && position == FIRST_POSITION

    /**
    Количество товара по корзинам
     */

    override val basketTitle by unsafeLazy {
        MutableLiveData(resource.byBasket())
    }

    override val rollbackVisibility = MutableLiveData(true)

    override val closeVisibility by unsafeLazy {
        task.map { task ->
            task?.type?.isWholesaleType() ?: false
        }
    }

    override val closeEnabled by unsafeLazy {
        basketQuantity.map { basketQuantityValue ->
            basketQuantityValue?.let { it > 0 }
        }
    }

    override fun updateProviders(providers: MutableList<ProviderInfo>) {
        sourceProviders.value = providers
    }

    override fun onScanResult(number: String) {
        launchUITryCatch {
            if (isProviderSelected.value == true) {
                checkSearchNumber(number)
            } else {
                navigator.showChooseProviderFirst()
            }
        }
    }

    override suspend fun getBasket(good: Good): Basket? {
        return manager.getBasket(ProviderInfo.getEmptyCode(), good, false)
    }

    override fun updateData() {
        val good = good.value
        if (manager.isWasAddedProvider && good != null) {
            updateProviders(good.providers)
            providerPosition.value = 1
            manager.isWasAddedProvider = false
        }
    }

    override fun onClickDetails() {
        manager.updateCurrentGood(good.value)
        navigator.openGoodDetailsCreateScreen()
    }

    override fun addProvider() {
        navigator.openAddProviderScreen()
    }

    override fun getProvider(): ProviderInfo {
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

    override fun onClickClose() {
        navigator.showCloseBasketDialog(::handleYesOnClickCloseCallback)
    }

    private fun handleYesOnClickCloseCallback() {
        manager.isBasketsNeedsToBeClosed = true
        saveChangesAndExit()
    }
}

