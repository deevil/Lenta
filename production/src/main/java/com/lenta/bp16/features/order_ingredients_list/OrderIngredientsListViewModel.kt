package com.lenta.bp16.features.order_ingredients_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.lenta.bp16.model.ingredients.IngredientInfo
import com.lenta.bp16.model.ingredients.OrderIngredientDataInfo
import com.lenta.bp16.model.ingredients.params.GetIngredientDataParams
import com.lenta.bp16.model.ingredients.params.UnblockIngredientsParams
import com.lenta.bp16.model.ingredients.params.WarehouseParam
import com.lenta.bp16.model.ingredients.ui.ItemOrderIngredientUi
import com.lenta.bp16.model.ingredients.results.IngredientsDataListResult
import com.lenta.bp16.model.ingredients.ui.OrderByBarcodeUI
import com.lenta.bp16.model.warehouse.IWarehousePersistStorage
import com.lenta.bp16.platform.extention.getFieldWithSuffix
import com.lenta.bp16.platform.extention.getItemName
import com.lenta.bp16.platform.extention.getModeType
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.bp16.platform.resource.IResourceManager
import com.lenta.bp16.request.GetEanIngredientsNetRequest
import com.lenta.bp16.request.GetIngredientsDataListNetRequest
import com.lenta.bp16.request.UnblockIngredientNetRequest
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.*
import javax.inject.Inject
import kotlin.properties.Delegates

class OrderIngredientsListViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var resourceManager: IResourceManager

    @Inject
    lateinit var unblockIngredientNetRequest: UnblockIngredientNetRequest

    @Inject
    lateinit var warehouseStorage: IWarehousePersistStorage

    @Inject
    lateinit var getIngredientData: GetIngredientsDataListNetRequest

    // выбранное количество
    var weight: String by Delegates.notNull()

    // выбранный ингредиент
    val ingredient by unsafeLazy {
        MutableLiveData<IngredientInfo>()
    }

    private val allOrderIngredients: MutableLiveData<List<OrderIngredientDataInfo>> by unsafeLazy {
        MutableLiveData<List<OrderIngredientDataInfo>>()
    }

    private val allEanIngredients: MutableLiveData<List<OrderByBarcodeUI>> by unsafeLazy {
        MutableLiveData<List<OrderByBarcodeUI>>()
    }

    fun loadOrderIngredientsList() = launchUITryCatch {
        navigator.showProgressLoadingData()

        val code = ingredient.value?.code.orEmpty()
        val mode = ingredient.value?.getModeType().orEmpty()
        val warehouseList = warehouseStorage.getSelectedWarehouses().toList()

        val lgort = when (mode) {
            MODE_5 -> mutableListOf(WarehouseParam(ingredient.value?.lgort.orEmpty()))
            MODE_6 -> mutableListOf(WarehouseParam(ingredient.value?.lgort.orEmpty()))
            else -> {
                val selectedWarehouseList = mutableListOf<WarehouseParam>()
                for (element in warehouseList) {
                    selectedWarehouseList.add(WarehouseParam(element))
                }
                selectedWarehouseList
            }
        }

        val result = getIngredientData(
                params = GetIngredientDataParams(
                        tkMarket = sessionInfo.market.orEmpty(),
                        deviceIP = resourceManager.deviceIp,
                        code = code,
                        mode = mode,
                        weight = weight,
                        warehouse = lgort
                )

        ).also {
            navigator.hideProgress()
        }
        result.either(::handleFailure) { ingredientsDataListResult ->
            allEanIngredients.value = ingredientsDataListResult.orderByBarcode
            allOrderIngredients.value = ingredientsDataListResult.ordersIngredientsDataInfoList
            Unit
        }
    }

    val orderIngredientsList by unsafeLazy {
        allOrderIngredients.switchMap {
            asyncLiveData<List<ItemOrderIngredientUi>> {
                emit(it.mapIndexed { index, orderIngredientDataInfo ->
                    ItemOrderIngredientUi(
                            name = orderIngredientDataInfo.getItemName(),
                            position = (index + 1).toString(),
                            plan = getFieldWithSuffix(orderIngredientDataInfo.plan_qnt.dropZeros(), orderIngredientDataInfo.getSuffix()),
                            fact = getFieldWithSuffix(orderIngredientDataInfo.done_qnt.dropZeros(), orderIngredientDataInfo.getSuffix())
                    )
                })
            }
        }
    }

    fun onBackPressed() = launchUITryCatch {
        navigator.showProgressLoadingData()
        unblockIngredientNetRequest(
                params = UnblockIngredientsParams(
                        code = ingredient.value?.code.orEmpty(),
                        mode = UnblockIngredientsParams.MODE_UNBLOCK_ORDER
                )
        ).also {
            navigator.hideProgress()
            navigator.goBack()
        }.either(fnL = ::handleFailure)
    }

    fun onClickItemPosition(position: Int) {
        ingredient.value?.let { selectedIngredient ->
            allOrderIngredients.value?.getOrNull(position)?.let { orderDataInfo ->
                allEanIngredients.value?.getOrNull(position)?.let { barcode ->
                    navigator.openIngredientDetailsScreen(orderDataInfo, selectedIngredient.text3.orEmpty(), barcode)
                } ?: navigator.showNotFoundedBarcodeForPosition()
            } ?: navigator.showAlertIngredientNotFound()
        }
    }

    companion object {
        const val MODE_5 = "5"
        const val MODE_6 = "6"
    }

}