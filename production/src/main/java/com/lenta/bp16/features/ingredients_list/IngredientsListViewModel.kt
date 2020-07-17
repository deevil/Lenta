package com.lenta.bp16.features.ingredients_list

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.lenta.bp16.model.IngredientStatus
import com.lenta.bp16.model.ingredients.GetIngredientsParams
import com.lenta.bp16.model.ingredients.ItemIngredientUi
import com.lenta.bp16.model.warehouse.IWarehousePersistStorage
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.bp16.request.GetIngredientsNetRequest
import com.lenta.bp16.request.pojo.IngredientInfo
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.asyncLiveData
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.unsafeLazy
import javax.inject.Inject

class IngredientsListViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var warehouseStorage: IWarehousePersistStorage

    @Inject
    lateinit var getIngredientsRequest: GetIngredientsNetRequest

    @Inject
    lateinit var context: Context

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

    init {
        loadIngredients()
    }

    private fun loadIngredients() {
        launchUITryCatch {
            navigator.showProgressLoadingData()
            getIngredientsRequest(
                    GetIngredientsParams(
                            tkMarket = sessionInfo.market.orEmpty(),
                            deviceIP = context.getDeviceIp(),
                            workhousesList = warehouseStorage.getSelectedWarehouses().toList()
                    )
            ).also {
                navigator.hideProgress()
            }.either(::handleFailure, allIngredients::setValue)
        }
    }

    private fun filterIngredientsBy(ingredientsList: List<IngredientInfo>, type: String): List<ItemIngredientUi> {
        return ingredientsList.asSequence()
                .filter { it.objType == type }
                .mapIndexed { index, ingredientInfo ->
                    ItemIngredientUi(
                            position = index.toString(),
                            text1 = ingredientInfo.text1.orEmpty(),
                            text2 = ingredientInfo.text2.orEmpty(),
                            ingredientStatus = IngredientStatus.LOCK
                    )
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
                //0 -> processing.value?.get(position)?.material
                //1 -> processed.value?.get(position)?.material
                else -> null
            }?.let { material ->
                // openGoodByMaterial(material)
            }
        }
    }
}