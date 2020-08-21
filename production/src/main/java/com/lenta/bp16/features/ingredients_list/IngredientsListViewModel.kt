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
import com.lenta.bp16.model.ingredients.results.IngredientsListResult
import com.lenta.bp16.model.ingredients.ui.ItemIngredientUi
import com.lenta.bp16.model.ingredients.ui.OrderByBarcodeUI
import com.lenta.bp16.model.ingredients.ui.OrderByBarcodeUI.Companion.EAN_NOM
import com.lenta.bp16.model.ingredients.ui.OrderByBarcodeUI.Companion.EAN_UMREN
import com.lenta.bp16.model.ingredients.ui.OrderByBarcodeUI.Companion.EAN_UMREZ
import com.lenta.bp16.model.ingredients.ui.OrderByBarcodeUI.Companion.FAKE_EAN
import com.lenta.bp16.model.ingredients.ui.OrderByBarcodeUI.Companion.FAKE_MATNR
import com.lenta.bp16.model.warehouse.IWarehousePersistStorage
import com.lenta.bp16.platform.extention.getIngredientStatus
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.bp16.platform.resource.IResourceManager
import com.lenta.bp16.request.GetIngredientsNetRequest
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
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
    private val selectedMatnr by unsafeLazy { MutableLiveData<String>("") }

    private val allIngredients: MutableLiveData<IngredientsListResult> by unsafeLazy {
        MutableLiveData<IngredientsListResult>()
    }

    private val allIngredientsInfo: MutableLiveData<List<IngredientInfo>> by unsafeLazy {
        MutableLiveData<List<IngredientInfo>>()
    }

    private val allIngredientsEanInfo: MutableLiveData<List<OrderByBarcodeUI>> by unsafeLazy {
        MutableLiveData<List<OrderByBarcodeUI>>()
    }

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
            ).either(::handleFailure, allIngredients::setValue)
            allIngredients.value?.let { ingredientListResult ->
                allIngredientsInfo.value = ingredientListResult.ingredientsList
                allIngredientsEanInfo.value = ingredientListResult.goodsEanList?.mapNotNull { it.convert() }
                goodsByOrderList.value = ingredientListResult.goodsListByOrder
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
                                ingredientStatus = ingredientInfo.getIngredientStatus()
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
            /**Давай поясню логику
             *
             * Меня чет совсем поплавило на этом месте, поэтому мб получится исправить это позже.
             * Главное записать чем я руководствовался в этот момент.
             *
             * */
            navigator.showProgressLoadingData()
            /**Поиск отсканированного ШК в данных интерфейса ZMP_UTZ_PRO_10_V001*/
            val searchStatus = withContext(Dispatchers.IO) {
                var status = SearchStatus.NOT_FOUND
                allIngredientsEanInfo.value?.find { numberField.value == it.ean }?.let { ean ->
                    selectedMatnr.value = ean.matnr

                    /**Поиск вхождения в список материалов*/
                    allIngredientsInfo.value?.find { it.code == selectedMatnr.value }?.let {
                        status = SearchStatus.FOUND_INGREDIENT
                    }
                    /**Поиск вхождения в список заказов*/
                    goodsByOrderList.value?.find { it.matnr == selectedMatnr.value }?.let {
                        if (status == SearchStatus.FOUND_INGREDIENT)
                            status = SearchStatus.DUALISM
                        status = SearchStatus.FOUND_ORDER
                    }
                }
                return@withContext status
            }

            navigator.hideProgress()

            /*when (searchStatus) {
                SearchStatus.DUALISM -> navigator.showAlertDualism()
                SearchStatus.FOUND_INGREDIENT -> allIngredients.value?.find { it.code == selectedMatnr.value }?.let { selectedIngredient ->
                    navigator.openOrderDetailsScreen(selectedIngredient, barcode)
                }
                SearchStatus.FOUND_ORDER -> allIngredients.value?.find { it.code == selectedMatnr.value }?.let { selectedIngredient ->
                    navigator.openMaterialRemakesScreen(selectedIngredient)
                }
                SearchStatus.NOT_FOUND -> navigator.showAlertGoodNotFoundInCurrentShift()
            }*/

        }
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