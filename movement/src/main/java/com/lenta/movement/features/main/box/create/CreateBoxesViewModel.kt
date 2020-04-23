package com.lenta.movement.features.main.box.create

import androidx.lifecycle.MutableLiveData
import com.lenta.movement.platform.navigation.IScreenNavigator
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.databinding.PageSelectionListener

class CreateBoxesViewModel: CoreViewModel(), PageSelectionListener {

    lateinit var screenNavigator: IScreenNavigator

    var productInfo: MutableLiveData<ProductInfo> = MutableLiveData()

    val selectedPagePosition = MutableLiveData(0)
    val selectedPage = MutableLiveData(CreateBoxesPage.FILLING)

    override fun onPageSelected(position: Int) {
        selectedPagePosition.value = position
        selectedPage.value = CreateBoxesPage.values()[position]
    }

    fun getTitle(): String {
        return productInfo.value?.let {
            "${it.materialNumber.takeLast(6)} ${it.description}"
        }.orEmpty()
    }

}