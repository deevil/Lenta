package com.lenta.bp16.features.material_remake_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.lenta.bp16.model.ProducerDataInfo
import com.lenta.bp16.model.ZPartDataInfo
import com.lenta.bp16.model.data_storage.IIngredientDataPersistStorage
import com.lenta.bp16.model.ingredients.IngredientInfo
import com.lenta.bp16.model.ingredients.MaterialIngredientDataInfo
import com.lenta.bp16.model.ingredients.MercuryPartDataInfo
import com.lenta.bp16.model.ingredients.params.GetIngredientDataParams
import com.lenta.bp16.model.ingredients.params.UnblockIngredientsParams
import com.lenta.bp16.model.ingredients.params.WarehouseParam
import com.lenta.bp16.model.ingredients.ui.ItemMaterialIngredientUi
import com.lenta.bp16.model.ingredients.ui.OrderByBarcodeUI
import com.lenta.bp16.model.warehouse.IWarehousePersistStorage
import com.lenta.bp16.platform.extention.getFieldWithSuffix
import com.lenta.bp16.platform.extention.getModeType
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.bp16.platform.resource.IResourceManager
import com.lenta.bp16.request.GetIngredientsDataListNetRequest
import com.lenta.bp16.request.UnblockIngredientNetRequest
import com.lenta.bp16.request.ingredients_use_case.set_data.SetMercuryPartDataInfoUseCase
import com.lenta.bp16.request.ingredients_use_case.set_data.SetProducerDataInfoUseCase
import com.lenta.bp16.request.ingredients_use_case.set_data.SetWarehouseForSelectedItemUseCase
import com.lenta.bp16.request.ingredients_use_case.set_data.SetZPartDataInfoUseCase
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
    lateinit var getIngredientDataList: GetIngredientsDataListNetRequest

    @Inject
    lateinit var unblockIngredientNetRequest: UnblockIngredientNetRequest

    @Inject
    lateinit var warehouseStorage: IWarehousePersistStorage

    @Inject
    lateinit var setMercuryPartDataInfoUseCase: SetMercuryPartDataInfoUseCase

    @Inject
    lateinit var setProducerDataInfoUseCase: SetProducerDataInfoUseCase

    @Inject
    lateinit var setZPartDataInfoUseCase: SetZPartDataInfoUseCase

    @Inject
    lateinit var setWarehouseForSelectedItemUseCase:SetWarehouseForSelectedItemUseCase


    // выбранный ингредиент
    val ingredient by unsafeLazy {
        MutableLiveData<IngredientInfo>()
    }

    private val allEanMaterialIngredients: MutableLiveData<List<OrderByBarcodeUI>> by unsafeLazy {
        MutableLiveData<List<OrderByBarcodeUI>>()
    }

    private val allMaterialIngredients: MutableLiveData<List<MaterialIngredientDataInfo>> by unsafeLazy {
        MutableLiveData<List<MaterialIngredientDataInfo>>()
    }

    private val allProducersList: MutableLiveData<List<ProducerDataInfo>> by unsafeLazy {
        MutableLiveData<List<ProducerDataInfo>>()
    }

    private val allMercuryPartDataInfoList: MutableLiveData<List<MercuryPartDataInfo>> by unsafeLazy {
        MutableLiveData<List<MercuryPartDataInfo>>()
    }

    private val zPartDataInfoList: MutableLiveData<List<ZPartDataInfo>> by unsafeLazy {
        MutableLiveData<List<ZPartDataInfo>>()
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

        val selectedWarehouseList = when (mode) {
            MODE_5, MODE_6 -> mutableListOf(WarehouseParam(ingredient.value?.lgort.orEmpty()))
            else -> warehouseList.mapTo(mutableListOf()) { WarehouseParam(it) }
        }

        val result = getIngredientDataList(
                params = GetIngredientDataParams(
                        tkMarket = sessionInfo.market.orEmpty(),
                        deviceIP = resourceManager.deviceIp,
                        code = code,
                        mode = mode,
                        weight = "",
                        warehouse = selectedWarehouseList
                )
        ).also {
            navigator.hideProgress()
        }
        result.either(::handleFailure) { ingredientsDataListResult ->
            allMaterialIngredients.value = ingredientsDataListResult.materialsIngredientsDataInfoList
            allEanMaterialIngredients.value = ingredientsDataListResult.orderByBarcode
            allProducersList.value = ingredientsDataListResult.producerDataInfoList
            allMercuryPartDataInfoList.value = ingredientsDataListResult.mercuryPartDataInfoList
            zPartDataInfoList.value = ingredientsDataListResult.zPartDataInfoList
            Unit
        }
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
        }.either(::handleFailure)
    }

    /** Сохранение списков в IngredientDataPersistStorage */
    private fun saveDataInStorage() {
        val producerDataInfoList = allProducersList.value
        val zPartDataInfoList = zPartDataInfoList.value
        val mercuryPartDataInfoList = allMercuryPartDataInfoList.value
        launchUITryCatch {
            setZPartDataInfoUseCase(zPartDataInfoList.orEmpty())
            setMercuryPartDataInfoUseCase(mercuryPartDataInfoList.orEmpty())
            setProducerDataInfoUseCase(producerDataInfoList.orEmpty())
        }
    }

    fun onClickItemPosition(position: Int) {

        saveDataInStorage()

        allMaterialIngredients.value?.getOrNull(position)?.let { selectedMaterial ->
            val code = ingredient.value?.getFormattedCode().orEmpty()
            val name = ingredient.value?.nameMatnrOsn.orEmpty()
            val warehouse = ingredient.value?.lgort.orEmpty()
            launchUITryCatch {
                setWarehouseForSelectedItemUseCase(listOf(warehouse))
            }
            allEanMaterialIngredients.value?.getOrNull(position)?.let { barcode ->
                navigator.openMaterialRemakeDetailsScreen(selectedMaterial, code, name, barcode)
            }
        } ?: navigator.showAlertPartNotFound()
    }

    companion object {
        const val MODE_5 = "5"
        const val MODE_6 = "6"
    }
}