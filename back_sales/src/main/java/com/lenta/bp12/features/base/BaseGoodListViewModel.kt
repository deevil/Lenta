package com.lenta.bp12.features.base

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.managers.interfaces.IMarkManager
import com.lenta.bp12.managers.interfaces.ITaskManager
import com.lenta.bp12.model.Taskable
import com.lenta.bp12.model.pojo.Basket
import com.lenta.bp12.model.pojo.Good
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.platform.resource.IResourceManager
import com.lenta.bp12.repository.IDatabaseRepository
import com.lenta.bp12.request.GoodInfoNetRequest
import com.lenta.bp12.request.PrintPalletListNetRequest
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.device_info.DeviceInfo
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.extentions.unsafeLazy
import javax.inject.Inject

abstract class BaseGoodListViewModel<R : Taskable, T : ITaskManager<R>> : CoreViewModel() {

    val numberField: MutableLiveData<String> = MutableLiveData("")

    val basketSelectionsHelper = SelectionItemsHelper()

    val requestFocusToNumberField by lazy {
        MutableLiveData(true)
    }

    @Inject
    lateinit var navigator: IScreenNavigator

    abstract var manager: T

    @Inject
    lateinit var markManager: IMarkManager

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var deviceInfo: DeviceInfo

    @Inject
    lateinit var resource: IResourceManager

    @Inject
    lateinit var printPalletListNetRequest: PrintPalletListNetRequest

    /** "ZMP_UTZ_BKS_05_V001"
     * Получение данных товара по ШК / SAP-коду
     */
    @Inject
    lateinit var goodInfoNetRequest: GoodInfoNetRequest

    @Inject
    lateinit var database: IDatabaseRepository

    protected val task by unsafeLazy {
        manager.currentTask
    }

    abstract fun checkSearchNumber(number: String)
    abstract fun getGoodByEan(ean: String)
    abstract fun setFoundGood(foundGood: Good)

    fun onClickProperties() {
        navigator.openBasketPropertiesScreen()
    }

    fun checkThatNoneOfGoodAreMarkType(goodTitle: String) {
        if (task.value?.goods?.none { it.isMarked() } == true) {
            navigator.showForGoodNeedScanFirstMark(goodTitle)
        }
    }

    fun onScanResult(data: String) {
        checkSearchNumber(data)
    }

    fun getMrc(good: Good, task: R): String {
        val mrc = good.maxRetailPrice

        return mrc.takeIf { isDivByMrcAndItsNotZero(mrc, task) }
                ?.let { resource.mrcDashCostRub(it) }
                .orEmpty()
    }

    private fun isDivByMrcAndItsNotZero(mrc: String, task: R): Boolean {
        val isDivByMinimalPrice = task.type?.isDivByMinimalPrice == true
        return isDivByMinimalPrice && (mrc.isEmpty().not() && mrc != ZERO_MRC_STRING)
    }

    inline fun <reified T : CoreFragment<*, *>, reified S: CoreFragment<*, *>> lockAndUpdateBasketAndTask(isNeedLock: Boolean, task: R, basket: Basket) {
        basket.isLocked = isNeedLock
        task.updateBasket(basket)
        with(manager) {
            updateCurrentBasket(basket)
            updateCurrentTask(task)
        }
        if (isNeedLock) {
            navigator.goBackTo(T::class.simpleName)
        } else {
            navigator.goBackTo(S::class.simpleName)
        }

    }

    companion object {
        private const val ZERO_MRC_STRING = "0"
    }
}