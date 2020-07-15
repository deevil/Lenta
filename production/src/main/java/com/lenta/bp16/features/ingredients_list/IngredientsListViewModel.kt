package com.lenta.bp16.features.ingredients_list

import androidx.lifecycle.MutableLiveData
import com.lenta.bp16.model.IngredientStatus
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.unsafeLazy
import javax.inject.Inject

class IngredientsListViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var navigator: IScreenNavigator

    val selectedPage by unsafeLazy { MutableLiveData(0) }
    val numberField by unsafeLazy { MutableLiveData<String>("") }
    val requestFocusToNumberField by unsafeLazy { MutableLiveData(true) }

    val ingredientsByOrder by unsafeLazy { mutableListOf<ItemIngredientUi>() }
    val ingredientsByMaterial by unsafeLazy { mutableListOf<ItemIngredientUi>() }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    override fun onOkInSoftKeyboard(): Boolean {

        return true
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

data class ItemIngredientUi(
        val position: String,
        val text1: String,
        val text2: String,
        val ingredientStatus: IngredientStatus
)