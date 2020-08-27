package com.lenta.bp16.features.tech_orders_list

import androidx.lifecycle.MutableLiveData
import com.lenta.bp16.model.ingredients.ui.ItemTechOrderUi
import com.lenta.bp16.platform.extention.getFieldWithSuffix
import com.lenta.bp16.platform.resource.IResourceManager
import com.lenta.bp16.request.GetTechOrdersUseCase
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.unsafeLazy
import javax.inject.Inject

class TechOrdersListViewModel : CoreViewModel() {

    @Inject
    lateinit var getTechOrdersUseCase: GetTechOrdersUseCase

    @Inject
    lateinit var resourceManager: IResourceManager

    // суффикс
    val suffix: String by unsafeLazy {
        resourceManager.kgSuffix()
    }

    val materialIngredientKtsch: MutableLiveData<String> by unsafeLazy {
        MutableLiveData<String>()
    }


    val allTechOrdersList by unsafeLazy {
        MutableLiveData<List<ItemTechOrderUi>>()
    }

    init {
        launchUITryCatch {
            getTechOrdersUseCase(Unit).either { list ->
                allTechOrdersList.value = list.filter { it.ktsch == materialIngredientKtsch.value }.mapIndexed { index, techOrderDataInfo ->
                    val position = (index + 1).toString()
                    ItemTechOrderUi(
                            position = position,
                            text1 = techOrderDataInfo.text1.orEmpty().takeLast(valueNumber),
                            text2 = techOrderDataInfo.text2.orEmpty(),
                            plan = getFieldWithSuffix(techOrderDataInfo.plan_qnt, suffix)
                    )
                }
                Unit
            }
        }
    }

    companion object{
        /**Значимые цифры заказа*/
        const val valueNumber = 12
    }

}