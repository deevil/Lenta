package com.lenta.bp16.features.order_ingredients_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.lenta.bp16.model.ingredients.IngredientInfo
import com.lenta.bp16.model.ingredients.OrderIngredientDataInfo
import com.lenta.bp16.model.ingredients.params.GetIngredientDataParams
import com.lenta.bp16.model.ingredients.params.UnblockIngredientsParams
import com.lenta.bp16.model.ingredients.ui.ItemOrderIngredientUi
import com.lenta.bp16.model.ingredients.ui.OrderByBarcode
import com.lenta.bp16.platform.extention.getFieldWithSuffix
import com.lenta.bp16.platform.extention.getItemName
import com.lenta.bp16.platform.extention.getModeType
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.bp16.platform.resource.IResourceManager
import com.lenta.bp16.request.GetEanIngredientsNetRequest
import com.lenta.bp16.request.GetOrderIngredientsDataNetRequest
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

    // выбранное количество
    var weight: String by Delegates.notNull()

    // выбранный ингредиент
    val ingredient by unsafeLazy {
        MutableLiveData<IngredientInfo>()
    }

    @Inject
    lateinit var getIngredientData: GetOrderIngredientsDataNetRequest

    @Inject
    lateinit var getEanIngredientData: GetEanIngredientsNetRequest

    private val allOrderIngredients: MutableLiveData<List<OrderIngredientDataInfo>> by unsafeLazy {
        MutableLiveData<List<OrderIngredientDataInfo>>()
    }

    private val allEanIngredients: MutableLiveData<List<OrderByBarcode>> by unsafeLazy {
        MutableLiveData<List<OrderByBarcode>>()
    }

    fun loadOrderIngredientsList() = launchUITryCatch {
        navigator.showProgressLoadingData()

        val code = ingredient.value?.code.orEmpty()
        val mode = ingredient.value?.getModeType().orEmpty()

        val result = getIngredientData(
                params = GetIngredientDataParams(
                        tkMarket = sessionInfo.market.orEmpty(),
                        deviceIP = resourceManager.deviceIp,
                        code = code,
                        mode = mode,
                        weight = weight
                )
        )
        val eanResult = getEanIngredientData(
                params = GetIngredientDataParams(
                        tkMarket = sessionInfo.market.orEmpty(),
                        deviceIP = resourceManager.deviceIp,
                        code = code,
                        mode = mode,
                        weight = weight
                )
        ).also {
            navigator.hideProgress()
        }
        result.either(::handleFailure, allOrderIngredients::setValue)
        eanResult.either(::handleFailure, allEanIngredients::setValue)
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
                        mode = UnblockIngredientsParams.MODE_UNBLOCK_VP
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
}