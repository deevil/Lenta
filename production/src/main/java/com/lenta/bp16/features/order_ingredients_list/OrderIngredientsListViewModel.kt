package com.lenta.bp16.features.order_ingredients_list

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.lenta.bp16.model.ingredients.IngredientInfo
import com.lenta.bp16.model.ingredients.OrderIngredientDataInfo
import com.lenta.bp16.model.ingredients.params.GetIngredientDataParams
import com.lenta.bp16.model.ingredients.params.UnblockIngredientsParams
import com.lenta.bp16.model.ingredients.ui.ItemOrderIngredientUi
import com.lenta.bp16.platform.extention.getDoneCount
import com.lenta.bp16.platform.extention.getItemName
import com.lenta.bp16.platform.extention.getModeType
import com.lenta.bp16.platform.extention.getPlanCount
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.bp16.request.GetOrderIngredientsDataNetRequest
import com.lenta.bp16.request.UnblockIngredientNetRequest
import com.lenta.bp16.request.UnblockTaskParams
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.*
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.properties.Delegates

class OrderIngredientsListViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var context: Context

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

    private val allOrderIngredients: MutableLiveData<List<OrderIngredientDataInfo>> by unsafeLazy {
        MutableLiveData<List<OrderIngredientDataInfo>>()
    }

    init {
        launchUITryCatch {
            navigator.showProgressLoadingData()

            val code = ingredient.value?.code.orEmpty()
            val mode =  ingredient.value?.getModeType().orEmpty()

            val result  = getIngredientData(
                    params = GetIngredientDataParams(
                            tkMarket = sessionInfo.market.orEmpty(),
                            deviceIP = context.getDeviceIp(),
                            code = code,
                            mode = mode,
                            weight = weight
                    )
            ).also {
                navigator.hideProgress()
            }
            result.either(::handleFailure, fnR = {
                allOrderIngredients.value = it
                it
            })
        }
    }

    val orderIngredientsList by unsafeLazy {
        allOrderIngredients.switchMap {
            asyncLiveData<List<ItemOrderIngredientUi>> {
                emit(it.mapIndexed { index, orderIngredientDataInfo ->
                    ItemOrderIngredientUi(
                            name = orderIngredientDataInfo.getItemName(),
                            position = (index + 1).toString(),
                            plan = orderIngredientDataInfo.getPlanCount(),
                            fact = orderIngredientDataInfo.getDoneCount()
                    )
                })
            }
        }
    }

    fun onBackPressed() = launchAsyncTryCatch {
        unblockIngredientNetRequest(
                params = UnblockIngredientsParams(
                        code = ingredient.value?.code.orEmpty(),
                        mode = UnblockIngredientsParams.MODE_UNBLOCK_VP
                )
        ).either(fnL = ::handleFailure, fnR = {
            navigator.goBack()
        })
    }

    fun onClickItemPosition(position: Int) {
        allOrderIngredients.value?.getOrNull(position)?.let {
            navigator.openIngredientDetailsScreen(it)
        }
    }
}