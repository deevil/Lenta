package com.lenta.bp16.features.ingredients_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.lenta.bp16.features.ingredients_list.IngredientsListFragment.Companion.TAB_BY_MATERIALS
import com.lenta.bp16.features.ingredients_list.IngredientsListFragment.Companion.TAB_BY_ORDER
import com.lenta.bp16.model.ingredients.IngredientInfo
import com.lenta.bp16.model.ingredients.params.GetIngredientsParams
import com.lenta.bp16.model.ingredients.params.WarehouseParam
import com.lenta.bp16.model.ingredients.ui.ItemIngredientUi
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

    private val allIngredients: MutableLiveData<List<IngredientInfo>> by unsafeLazy {
        MutableLiveData<List<IngredientInfo>>()
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

    fun loadIngredients() {
        launchUITryCatch {
            navigator.showProgressLoadingData()
            getIngredientsRequest(
                    GetIngredientsParams(
                            tkMarket = sessionInfo.market.orEmpty(),
                            deviceIP = resourceManager.deviceIp,
                            workhousesList = warehouseStorage.getSelectedWarehouses().map { WarehouseParam(it) }
                    )
            ).also {
                navigator.hideProgress()
            }.either(::handleFailure, allIngredients::setValue)
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

        return true
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
}