package com.lenta.bp16.features.material_remake_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.lenta.bp16.features.order_ingredients_list.OrderIngredientsListViewModel
import com.lenta.bp16.model.ingredients.IngredientInfo
import com.lenta.bp16.model.ingredients.MaterialIngredientDataInfo
import com.lenta.bp16.model.ingredients.params.GetIngredientDataParams
import com.lenta.bp16.model.ingredients.params.UnblockIngredientsParams
import com.lenta.bp16.model.ingredients.params.WarehouseParam
import com.lenta.bp16.model.ingredients.ui.ItemMaterialIngredientUi
import com.lenta.bp16.model.ingredients.ui.OrderByBarcode
import com.lenta.bp16.model.warehouse.IWarehousePersistStorage
import com.lenta.bp16.platform.extention.getFieldWithSuffix
import com.lenta.bp16.platform.extention.getModeType
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.bp16.platform.resource.IResourceManager
import com.lenta.bp16.request.GetEanIngredientsNetRequest
import com.lenta.bp16.request.GetMaterialIngredientsDataNetRequest
import com.lenta.bp16.request.UnblockIngredientNetRequest
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.asyncLiveData
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.unsafeLazy
import javax.inject.Inject

class MaterialRemakesListViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var resourceManager: IResourceManager

    @Inject
    lateinit var getIngredientData: GetMaterialIngredientsDataNetRequest

    @Inject
    lateinit var unblockIngredientNetRequest: UnblockIngredientNetRequest

    @Inject
    lateinit var getEanIngredientData: GetEanIngredientsNetRequest

    @Inject
    lateinit var warehouseStorage: IWarehousePersistStorage

    // выбранный ингредиент
    val ingredient by unsafeLazy {
        MutableLiveData<IngredientInfo>()
    }

    private val allEanMaterialIngredients: MutableLiveData<List<OrderByBarcode>> by unsafeLazy {
        MutableLiveData<List<OrderByBarcode>>()
    }

    private val allMaterialIngredients: MutableLiveData<List<MaterialIngredientDataInfo>> by unsafeLazy {
        MutableLiveData<List<MaterialIngredientDataInfo>>()
    }

    // суффикс
    val suffix: String by unsafeLazy {
        resourceManager.kgSuffix()
    }

    fun loadMaterialIngredients() = launchUITryCatch {
        navigator.showProgressLoadingData()

        val code = ingredient.value?.code.orEmpty()
        val mode = ingredient.value?.getModeType().orEmpty()
        val warehouseList = warehouseStorage.getSelectedWarehouses().toList()

        val lgort = when(mode){
            OrderIngredientsListViewModel.MODE_5 -> mutableListOf(WarehouseParam(ingredient.value?.lgort.orEmpty()))
            OrderIngredientsListViewModel.MODE_6 -> mutableListOf(WarehouseParam(ingredient.value?.lgort.orEmpty()))
            else -> {val selectedWarehouseList = mutableListOf<WarehouseParam>()
                for (element in warehouseList){
                    selectedWarehouseList.add(WarehouseParam(element))
                }
                selectedWarehouseList
            }
        }

        val eanResult = getEanIngredientData(
                params = GetIngredientDataParams(
                        tkMarket = sessionInfo.market.orEmpty(),
                        deviceIP = resourceManager.deviceIp,
                        code = code,
                        mode = mode,
                        weight = "",
                        warehouse = lgort
                )
        )

        val result = getIngredientData(
                params = GetIngredientDataParams(
                        tkMarket = sessionInfo.market.orEmpty(),
                        deviceIP = resourceManager.deviceIp,
                        code = code,
                        mode = mode,
                        weight = "",
                        warehouse = lgort
                )
        ).also {
            navigator.hideProgress()
        }
        result.either(::handleFailure, allMaterialIngredients::setValue)
        eanResult.either(::handleFailure, allEanMaterialIngredients::setValue)
    }

    val materialIngredients by unsafeLazy {
        allMaterialIngredients.switchMap {
            asyncLiveData<List<ItemMaterialIngredientUi>> {
                emit(it.mapIndexed { index, materialIngredientDataInfo ->
                    ItemMaterialIngredientUi(
                            lgort = materialIngredientDataInfo.lgort.orEmpty(),
                            desc = materialIngredientDataInfo.ltxa1.orEmpty(),
                            position = (index + 1).toString(),
                            plan = getFieldWithSuffix(materialIngredientDataInfo.plan_qnt, suffix),
                            fact = getFieldWithSuffix(materialIngredientDataInfo.done_qnt, suffix)
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
                        mode = UnblockIngredientsParams.MODE_UNBLOCK_MATERIAL
                )
        ).also {
            navigator.hideProgress()
            navigator.goBack()
        }.either(fnL = ::handleFailure)
    }

    fun onClickItemPosition(position: Int) {
        allMaterialIngredients.value?.getOrNull(position)?.let { selectedMaterial ->
            val code = ingredient.value?.getFormattedCode().orEmpty()
            val name = ingredient.value?.nameMatnrOsn.orEmpty()
            allEanMaterialIngredients.value?.getOrNull(position)?.let { barcode ->
                navigator.openMaterialRemakeDetailsScreen(selectedMaterial, code, name, barcode)
            }
        } ?: navigator.showAlertPartNotFound()
    }
}