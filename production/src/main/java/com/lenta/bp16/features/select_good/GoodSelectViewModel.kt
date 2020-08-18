package com.lenta.bp16.features.select_good

import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import com.lenta.bp16.model.ingredients.IngredientInfo
import com.lenta.bp16.model.movement.params.ProductInfoParams
import com.lenta.bp16.model.movement.result.ProductInfoResult
import com.lenta.bp16.model.pojo.GoodParams
import com.lenta.bp16.platform.Constants
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.bp16.repository.DatabaseRepository
import com.lenta.bp16.request.ProductInfoNetRequest
import com.lenta.bp16.request.pojo.Ean
import com.lenta.bp16.request.pojo.Product
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.unsafeLazy
import javax.inject.Inject

class GoodSelectViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var productInfoNetRequest: ProductInfoNetRequest

    val deviceIp = MutableLiveData("")

    val enteredEanField = MutableLiveData("")
    val requestFocusEnteredEanField = MutableLiveData(true)

    private val productInfoList: MutableLiveData<List<ProductInfoResult>> by unsafeLazy {
        MutableLiveData<List<ProductInfoResult>>()
    }

    val enabledNextButton = enteredEanField.map { !it.isNullOrBlank() }

    /**
     * Остановился тут, потому что пока не приходят данные
     * */

    private fun searchGood() {
        val ean: MutableList<String> = mutableListOf()
        ean.add(enteredEanField.value.orEmpty())
        val matnr: List<String> = mutableListOf()
        launchUITryCatch {
            productInfoNetRequest(
                    ProductInfoParams(
                            ean = ean.map { Ean(it) },
                            matnr = matnr.map { Product(it) }
                    )
            ).either(::handleFailure, productInfoList::setValue)
        }
    }

/*    private fun searchGood() {
        launchUITryCatch {
            val goodEan = database.getGoodByEan(enteredEanField.value.toString())
            goodEan?.let {
                val ean = it.ean
                val material = it.getFormattedMaterial()
                val name = it.name
                val goodParams = GoodParams(ean = ean, material = material, name = name)
                navigator.openGoodInfoScreen(goodParams)
            } ?: navigator.showAlertGoodNotFound {
                navigator.openSelectGoodScreen()
            }
        }
    }*/

    fun onClickNext() {
        searchGood()
    }

    fun onScanResult(data: String) {
        enteredEanField.value = data
        searchGood()
    }

    fun onClickMenu() {
        navigator.openMainMenuScreen()
    }

}