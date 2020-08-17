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
import com.lenta.bp16.model.ingredients.ui.OrderByBarcode
import com.lenta.bp16.model.warehouse.IWarehousePersistStorage
import com.lenta.bp16.platform.extention.getIngredientStatus
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.bp16.platform.resource.IResourceManager
import com.lenta.bp16.request.GetGoodsByOrderNetRequest
import com.lenta.bp16.request.GetIngredientsEanInfoNetRequest
import com.lenta.bp16.request.GetIngredientsNetRequest
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.asyncLiveData
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.unsafeLazy
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

    @Inject
    lateinit var getIngredientsEanInfoNetRequest: GetIngredientsEanInfoNetRequest

    @Inject
    lateinit var getGoodsByOrderNetRequest: GetGoodsByOrderNetRequest

    val selectedPage by unsafeLazy { MutableLiveData(0) }
    val numberField by unsafeLazy { MutableLiveData<String>("") }
    val requestFocusToNumberField by unsafeLazy { MutableLiveData(true) }
    val marketNumber by unsafeLazy { sessionInfo.market }
    private val selectedMatrn by unsafeLazy { MutableLiveData<String>("") }

    private val allIngredients: MutableLiveData<List<IngredientInfo>> by unsafeLazy {
        MutableLiveData<List<IngredientInfo>>()
    }

    private val allIngredientsEanInfo: MutableLiveData<List<OrderByBarcode>> by unsafeLazy {
        MutableLiveData<List<OrderByBarcode>>()
    }

    private val goodsByOrderList: MutableLiveData<List<GoodByOrder>> by unsafeLazy {
        MutableLiveData<List<GoodByOrder>>()
    }

    val ingredientsByOrder by unsafeLazy {
        allIngredients.switchMap {
            asyncLiveData<List<ItemIngredientUi>> {
                emit(filterIngredientsBy(it, IngredientInfo.TYPE_ORDER))
            }
        }
    }

    val ingredientsByMaterial by unsafeLazy {
        allIngredients.switchMap {
            asyncLiveData<List<ItemIngredientUi>> {
                emit(filterIngredientsBy(it, IngredientInfo.TYPE_MATERIAL))
            }
        }
    }

    /**
     *
     * О боже, зачем я это сделал?
     *
     * */
    fun loadIngredients() {
        navigator.showProgressLoadingData()
        loadIngredientsInfo()
        loadEanInfoIngredients()
        loadGoodsByOrder()
        navigator.hideProgress()
    }

    /**
     * То что ты сейчас прочитаешь выглядит мягко скажу некрасиво, я чуток запутался в коде и сделал не ок.
     * Надо будет сделать все через один запрос(изначально думал разбиение выглядит ок, но чет хз даже)
     *
     */

    fun loadIngredientsInfo() {
        launchUITryCatch {
            getIngredientsRequest(
                    GetIngredientsParams(
                            tkMarket = sessionInfo.market.orEmpty(),
                            deviceIP = resourceManager.deviceIp,
                            workhousesList = warehouseStorage.getSelectedWarehouses().map { WarehouseParam(it) }
                    )
            ).either(::handleFailure, allIngredients::setValue)
        }
    }
    /**Another one*/

    fun loadEanInfoIngredients() {
        launchUITryCatch {
            getIngredientsEanInfoNetRequest(
                    GetIngredientsParams(
                            tkMarket = sessionInfo.market.orEmpty(),
                            deviceIP = resourceManager.deviceIp,
                            workhousesList = warehouseStorage.getSelectedWarehouses().map { WarehouseParam(it) }
                    )
            ).either(::handleFailure, allIngredientsEanInfo::setValue)
        }
    }
    /**And Another one*/

    fun loadGoodsByOrder() {
        launchUITryCatch {
            getGoodsByOrderNetRequest(
                    GetIngredientsParams(
                            tkMarket = sessionInfo.market.orEmpty(),
                            deviceIP = resourceManager.deviceIp,
                            workhousesList = warehouseStorage.getSelectedWarehouses().map { WarehouseParam(it) }
                    )
            ).either(::handleFailure, goodsByOrderList::setValue)
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

            /**Поиск отсканированного ШК в данных интерфейса ZMP_UTZ_PRO_10_V001*/
            allIngredientsEanInfo.value?.find { numberField.value == it.ean }?.let {
                selectedMatrn.value = it.matnr
            }
            var searchStatus = SearchStatus.NOT_FOUND
            /**Поиск вхождения в список материалов*/
            allIngredients.value?.find { it.code == selectedMatrn.value }?.let {
                searchStatus = SearchStatus.FOUND_INGREDIENT
            }
            /**Поиск вхождения в список заказов*/
            goodsByOrderList.value?.find { it.matnr == selectedMatrn.value }?.let {
                if (searchStatus == SearchStatus.FOUND_ORDER)
                    searchStatus = SearchStatus.DUALISM
                searchStatus = SearchStatus.FOUND_INGREDIENT
            }

            when (searchStatus) {
                SearchStatus.DUALISM -> navigator.showAlertDualism {
                    navigator.openIngredientsListScreen()
                }
                SearchStatus.FOUND_INGREDIENT -> allIngredients.value?.find { it.code == selectedMatrn.value }?.let { selectedIngredient ->
                    navigator.openOrderDetailsScreen(selectedIngredient)
                }
                SearchStatus.FOUND_ORDER -> allIngredients.value?.find { it.code == selectedMatrn.value }?.let { selectedIngredient ->
                    navigator.openMaterialRemakesScreen(selectedIngredient)
                }
                SearchStatus.NOT_FOUND -> navigator.showAlertGoodNotFoundInCurrentShift { navigator.openIngredientsListScreen() }
            }

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
                allIngredients.value?.find { it.code == ingredientUI.code }?.let { selectedIngredient ->
                    if (selectedIngredient.isByOrder) {
                        navigator.openOrderDetailsScreen(selectedIngredient)
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
    }

}