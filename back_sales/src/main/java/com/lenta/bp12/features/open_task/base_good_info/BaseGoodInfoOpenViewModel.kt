package com.lenta.bp12.features.open_task.base_good_info
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.lenta.bp12.model.pojo.Basket
import com.lenta.bp12.model.pojo.extentions.getQuantityOfGood
import com.lenta.bp12.platform.FIRST_POSITION
import com.lenta.bp12.platform.ZERO_QUANTITY
import com.lenta.bp12.platform.extention.isWholesaleType
import com.lenta.bp12.request.pojo.ProviderInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.*
import com.lenta.shared.utilities.orIfNull

/** Класс содержащий общую логику для:
* @see com.lenta.bp12.features.open_task.marked_good_info.MarkedGoodInfoOpenViewModel
* @see com.lenta.bp12.features.open_task.good_info.GoodInfoOpenViewModel
 * */
abstract class BaseGoodInfoOpenViewModel : CoreViewModel(), IBaseGoodInfoOpenViewModel {

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
            it.type?.isWholesaleType() == true
        }
    }

    val isWholesale by lazy {
        manager.isWholesaleTaskType
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

    override val totalWithUnits by lazy {
        totalQuantity.map { quantity ->
            good.value?.let {
                buildString {
                    append(quantity.dropZeros())
                    if (isPlannedQuantityMoreThanZero) {
                        append(" $FROM_STRING ${it.planQuantity.dropZeros()}")
                    }
                    append(" ")
                    append(it.commonUnits.name)
                }
            }
        }
    }

    /**
     * Корзины
     * */
    override val basketNumber by unsafeLazy {
        good.combineLatest(quantity).combineLatest(isProviderSelected).switchMap {
            asyncTryCatchLiveData {
                val isProviderSelected = it.second

                if (isProviderSelected) {
                    task.value?.let { task ->
                        getBasket()?.let { basket ->
                            "${basket.index}"
                        } ?: "${task.baskets.size + 1}"
                    }.orEmpty()
                } else ""
            }
        }
    }

    override val basketQuantity by unsafeLazy {
        good.combineLatest(quantity).combineLatest(isProviderSelected).switchMap {
            asyncTryCatchLiveData {
                val good = it.first.first
                val enteredQuantity = it.first.second
                val isProviderSelected = it.second

                val result = if (isProviderSelected) {
                    getBasket()?.getQuantityOfGood(good)?.sumWith(enteredQuantity)
                            ?: enteredQuantity
                } else ZERO_QUANTITY

                result
            }
        }
    }

    override val basketQuantityWithUnits by unsafeLazy {
        good.combineLatest(basketQuantity).map {
            it?.let {
                val (good, quantity) = it
                "${quantity.dropZeros()} ${good.commonUnits.name}"
            }
        }
    }

    /**
    Список поставщиков
     */

    override val sourceProviders = MutableLiveData(mutableListOf<ProviderInfo>())

    override val providers = sourceProviders.map {
        it?.let { providers ->
            val list = providers.toMutableList()
            if (list.size > 1) {
                list.add(0,
                        ProviderInfo(
                                name = resource.chooseProvider()
                        ))
            }

            list.toList()
        }
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

    override val providerPosition = MutableLiveData(0)

    override val isProviderSelected = providerEnabled.combineLatest(providerPosition).map {
        val isEnabled = it?.first ?: false
        val position = it?.second ?: FIRST_POSITION

        isProviderEnabledAndPositionChanged(isEnabled, position)  or
                isProviderNotEnabledAndPositionDidntChanged(isEnabled, position)
    }

    private fun isProviderEnabledAndPositionChanged(isEnabled: Boolean, position: Int)
            = isEnabled && position > FIRST_POSITION
    private fun isProviderNotEnabledAndPositionDidntChanged(isEnabled: Boolean, position: Int)
            = !isEnabled && position == FIRST_POSITION

    /**
    Количество товара по корзинам
     */

    override val basketTitle by unsafeLazy {
        MutableLiveData(resource.byBasket())
    }

    override val rollbackVisibility = MutableLiveData(true)

    override val closeVisibility by unsafeLazy {
        task.map { task ->
            task?.type?.isWholesaleType()
        }
    }

    override val closeEnabled by unsafeLazy {
        applyEnabled.map { it }
    }

    /**
     * Плановое количество
     * */

    val plannedQuantity by unsafeLazy {
        good.value?.planQuantity ?: ZERO_QUANTITY
    }

    val isPlannedQuantityMoreThanZero by unsafeLazy {
        good.value?.planQuantity?.let {
            it > 0
        } ?: false
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

    override suspend fun getBasket(): Basket? {
        val good = good.value
        return good?.let {
            manager.getBasket(it.provider.code.orEmpty(), it)
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

    override fun isPlannedQuantityActual(): Boolean {
        val quantityValue = quantity.value ?: -1.0
        return isPlannedQuantityMoreThanZero && (quantityValue > plannedQuantity)
    }

    companion object {
        private const val FROM_STRING = "из"
    }
}
