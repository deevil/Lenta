package com.lenta.bp16.features.ingredients_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.lenta.bp16.features.ingredients_list.IngredientsListFragment.Companion.TAB_BY_MATERIALS
import com.lenta.bp16.features.ingredients_list.IngredientsListFragment.Companion.TAB_BY_ORDER
import com.lenta.bp16.model.SearchStatus
import com.lenta.bp16.model.ingredients.GoodByOrder
import com.lenta.bp16.model.ingredients.IngredientInfo
import com.lenta.bp16.model.ingredients.params.GetIngredientsParams
import com.lenta.bp16.model.ingredients.params.WarehouseParam
import com.lenta.bp16.model.ingredients.ui.ItemIngredientUi
import com.lenta.bp16.model.ingredients.ui.OrderByBarcodeUI
import com.lenta.bp16.model.ingredients.ui.OrderByBarcodeUI.Companion.EAN_NOM
import com.lenta.bp16.model.ingredients.ui.OrderByBarcodeUI.Companion.EAN_UMREN
import com.lenta.bp16.model.ingredients.ui.OrderByBarcodeUI.Companion.EAN_UMREZ
import com.lenta.bp16.model.ingredients.ui.OrderByBarcodeUI.Companion.FAKE_EAN
import com.lenta.bp16.model.ingredients.ui.OrderByBarcodeUI.Companion.FAKE_MATNR
import com.lenta.bp16.model.warehouse.IWarehousePersistStorage
import com.lenta.bp16.platform.extention.getIngredientStatusBlock
import com.lenta.bp16.platform.extention.getIngredientStatusWork
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.bp16.platform.resource.IResourceManager
import com.lenta.bp16.request.GetIngredientsNetRequest
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.asyncLiveData
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.unsafeLazy
import com.lenta.shared.utilities.orIfNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class IngredientsListViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var resourceManager: IResourceManager

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var warehouseStorage: IWarehousePersistStorage

    @Inject
    lateinit var getIngredientsRequest: GetIngredientsNetRequest

    val selectedPage by unsafeLazy { MutableLiveData(0) }
    val numberField by unsafeLazy { MutableLiveData<String>("") }
    val requestFocusToNumberField by unsafeLazy { MutableLiveData(true) }
    val marketNumber by unsafeLazy { sessionInfo.market }
    private var shortSapcode = ""
    private var aufnr = ""
    private var selectedEan = ""

    /**Общий список заказов и материалов*/
    private val allIngredientsInfo: MutableLiveData<List<IngredientInfo>> by unsafeLazy {
        MutableLiveData<List<IngredientInfo>>()
    }

    /**Список ШК*/
    private val allIngredientsEanInfo: MutableLiveData<List<OrderByBarcodeUI>> by unsafeLazy {
        MutableLiveData<List<OrderByBarcodeUI>>()
    }

    /**Список технических заказов*/
    private val goodsByOrderList: MutableLiveData<List<GoodByOrder>> by unsafeLazy {
        MutableLiveData<List<GoodByOrder>>()
    }

    val ingredientsByOrder by unsafeLazy {
        allIngredientsInfo.switchMap {
            asyncLiveData<List<ItemIngredientUi>> {
                emit(filterIngredientsBy(it, IngredientInfo.TYPE_ORDER))
            }
        }
    }

    val ingredientsByMaterial by unsafeLazy {
        allIngredientsInfo.switchMap {
            asyncLiveData<List<ItemIngredientUi>> {
                emit(filterIngredientsBy(it, IngredientInfo.TYPE_MATERIAL))
            }
        }
    }

    fun loadIngredients() {
        navigator.showProgressLoadingData()
        launchUITryCatch {
            getIngredientsRequest(
                    GetIngredientsParams(
                            tkMarket = sessionInfo.market.orEmpty(),
                            deviceIP = resourceManager.deviceIp,
                            workhousesList = warehouseStorage.getSelectedWarehouses().map { WarehouseParam(it) }
                    )
            ).either(::handleFailure) { ingredientListResult ->
                allIngredientsInfo.value = ingredientListResult.ingredientsList
                allIngredientsEanInfo.value = ingredientListResult.goodsEanList
                goodsByOrderList.value = ingredientListResult.goodsListByOrder
                Unit
            }
            navigator.hideProgress()
        }
    }

    private fun filterIngredientsBy(ingredientsList: List<IngredientInfo>, type: String): List<ItemIngredientUi> {
        return ingredientsList.asSequence()
                .filter { it.objType == type }
                .mapIndexedNotNull { index, ingredientInfo ->
                    ingredientInfo.code?.run {
                        val position = (index + 1).toString()
                        ItemIngredientUi(
                                code = ingredientInfo.code,
                                position = position,
                                text1 = ingredientInfo.text1.orEmpty(),
                                text2 = ingredientInfo.text2.orEmpty(),
                                ingredientStatusBlock = ingredientInfo.getIngredientStatusBlock(),
                                ingredientStatusWork = ingredientInfo.getIngredientStatusWork()
                        )
                    }
                }.toList()
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    override fun onOkInSoftKeyboard(): Boolean {
        searchByBarcode()
        return true
    }

    fun searchByBarcode() {
        launchUITryCatch {
            navigator.showProgressLoadingData()
            /**Поиск отсканированного ШК в данных интерфейса ZMP_UTZ_PRO_10_V001*/
            selectedEan = numberField.value.orEmpty()
            val searchStatus = findSearchStatus()
            navigator.hideProgress()
            processSearchStatus(searchStatus)
        }
    }

    /**Результаты поиска по статусу*/
    private suspend fun processSearchStatus(searchStatus: SearchStatus) {
        when (searchStatus) {
            SearchStatus.DUALISM -> navigator.showAlertDualism()
            SearchStatus.FOUND_ORDER -> {
                val selectedIngredient = withContext(Dispatchers.IO) { allIngredientsInfo.value?.find { it.code == aufnr } }
                selectedIngredient?.let {
                    val barcode = withContext(Dispatchers.IO) {
                        allIngredientsEanInfo.value?.find { it.ean == selectedEan }.orIfNull {
                            OrderByBarcodeUI(
                                    matnr = FAKE_MATNR,
                                    ean = FAKE_EAN,
                                    ean_nom = EAN_NOM,
                                    ean_umrez = EAN_UMREZ,
                                    ean_umren = EAN_UMREN
                            )
                        }
                    }
                    navigator.openOrderDetailsScreen(selectedIngredient, barcode)
                }
            }
            SearchStatus.FOUND_MATERIAL -> {
                val selectedIngredient = withContext(Dispatchers.IO) { allIngredientsInfo.value?.find { it.code == shortSapcode } }
                selectedIngredient?.let(navigator::openMaterialRemakesScreen)
            }
            SearchStatus.NOT_FOUND -> navigator.showAlertGoodNotFoundInCurrentShift()
        }
    }

    /**Определение статуса поиска*/
    private suspend fun findSearchStatus() = withContext(Dispatchers.IO) {
        var status = SearchStatus.NOT_FOUND
        allIngredientsEanInfo.value?.find { selectedEan == it.ean }?.let { ean ->
            val longSapcode = ean.matnr
            /**Поиск вхождения в список заказов*/
            goodsByOrderList.value?.find { it.matnr == longSapcode }?.let { order ->
                aufnr = order.aufnr.orEmpty()
                status = SearchStatus.FOUND_ORDER
            }
            shortSapcode = longSapcode.takeLast(6) //Берем последние 6 значащих цифр
            /**Поиск на двойное вхождение*/
            allIngredientsInfo.value?.find { it.code == aufnr && it.code == shortSapcode }?.let {
                status = SearchStatus.DUALISM
            }
            /**В случае, если двойное вхождение не было найдено, то выполнить поиск по MATNR*/
            if (status != SearchStatus.DUALISM) {
                allIngredientsInfo.value?.find { it.code == shortSapcode }?.let {
                    status = SearchStatus.FOUND_MATERIAL
                }
            }
        }
        status
    }

    fun onClickMenu() {
        navigator.goBack()
    }

    fun onClickItemPosition(position: Int) {
        selectedPage.value?.let { page ->
            when (page) {
                TAB_BY_ORDER -> ingredientsByOrder.value?.get(position)
                TAB_BY_MATERIALS -> ingredientsByMaterial.value?.get(position)
                else -> null
            }?.let { ingredientUI ->
                allIngredientsInfo.value?.find { it.code == ingredientUI.code }?.let { selectedIngredient ->
                    val positionInList = ingredientUI.position.toInt()
                    if (selectedIngredient.isByOrder) {
                        val barcode = allIngredientsEanInfo.value?.getOrNull(positionInList)
                                .orIfNull {
                                    OrderByBarcodeUI(
                                            matnr = FAKE_MATNR,
                                            ean = FAKE_EAN,
                                            ean_nom = EAN_NOM,
                                            ean_umrez = EAN_UMREZ,
                                            ean_umren = EAN_UMREN
                                    )
                                }
                        navigator.openOrderDetailsScreen(selectedIngredient, barcode)
                    } else {
                        navigator.openMaterialRemakesScreen(selectedIngredient)
                    }
                }
            }
        }
    }

    fun onRefreshClicked() {
        loadIngredients()
    }

    fun onScanResult(data: String) {
        numberField.value = data
        searchByBarcode()
    }
}