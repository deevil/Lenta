package com.lenta.bp12.features.base

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.managers.interfaces.IMarkManager
import com.lenta.bp12.managers.interfaces.ITaskManager
import com.lenta.bp12.model.Taskable
import com.lenta.bp12.model.actionByNumber
import com.lenta.bp12.model.pojo.Good
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.platform.resource.IResourceManager
import com.lenta.bp12.repository.IDatabaseRepository
import com.lenta.bp12.request.GoodInfoNetRequest
import com.lenta.bp12.request.PrintPalletListNetRequest
import com.lenta.bp12.request.pojo.good_info.GoodInfoParams
import com.lenta.bp12.request.pojo.good_info.GoodInfoResult
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.device_info.DeviceInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.unsafeLazy
import com.lenta.shared.utilities.orIfNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    /**
     * Метод проверяет длину отсканированного/введенного кода
     * */
    fun checkSearchNumber(number: String) {
        manager.ean = number
        actionByNumber(
                number = number,
                funcForEan = ::getGoodByEan,
                funcForMaterial = ::getGoodByMaterial,
                funcForSapOrBar = navigator::showTwelveCharactersEntered,
                funcForMark = ::checkMark,
                funcForNotValidBarFormat = navigator::showIncorrectEanFormat
        )
        numberField.value = ""
    }

    /**
     * Метод ищет есть ли уже товар в задании по EAN,
     * если есть то отправляет на его карточку
     * если нет то создает товар
     * */
    private fun getGoodByEan(ean: String) {
        launchUITryCatch {
            navigator.showProgressLoadingData()
            val foundGood = withContext(Dispatchers.IO) { manager.findGoodByEan(ean) }
            navigator.hideProgress()
            foundGood?.let(::setFoundGood).orIfNull {
                actionWhenGoodNotFoundByEan(ean)
            }
        }
    }

    suspend fun loadGoodInfoByEan(ean: String) {
        navigator.showProgressLoadingData(::handleFailure)
        goodInfoNetRequest(
                GoodInfoParams(
                        tkNumber = sessionInfo.market.orEmpty(),
                        ean = ean,
                        taskType = task.value?.type?.code.orEmpty()
                )
        ).also {
            navigator.hideProgress()
        }.either(
                fnL = ::handleFailure,
                fnR = ::handleLoadGoodInfoResult
        )
    }

    /**
     * Метод ищет есть ли уже товар в задании по Sap коду,
     * если есть то отправляет на его карточку
     * если нет то создает товар
     * */
    private fun getGoodByMaterial(material: String) {
        launchUITryCatch {
            manager.clearEan()
            navigator.showProgressLoadingData()
            val foundGood = withContext(Dispatchers.IO) { manager.findGoodByMaterial(material) }
            navigator.hideProgress()
            foundGood?.let(::setFoundGood).orIfNull {
                actionWhenGoodNotFoundByMaterial(material)
            }
        }
    }

    abstract fun setFoundGood(foundGood: Good)
    abstract fun checkMark(number: String)
    abstract suspend fun actionWhenGoodNotFoundByEan(ean: String)
    abstract suspend fun actionWhenGoodNotFoundByMaterial(material: String)
    abstract fun handleLoadGoodInfoResult(result: GoodInfoResult)

    fun checkThatNoneOfGoodAreMarkType(goodTitle: String) {
        if (task.value?.goods?.none { it.isMarked() } == true) {
            navigator.showForGoodNeedScanFirstMark(goodTitle)
        }
    }

    fun onScanResult(data: String) {
        checkSearchNumber(data)
    }
}