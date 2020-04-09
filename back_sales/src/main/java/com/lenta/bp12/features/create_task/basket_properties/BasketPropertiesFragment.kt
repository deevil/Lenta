package com.lenta.bp12.features.create_task.basket_properties

import com.lenta.bp12.R
import com.lenta.bp12.databinding.FragmentBasketPropertiesBinding
import com.lenta.bp12.platform.extention.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class BasketPropertiesFragment : CoreFragment<FragmentBasketPropertiesBinding, BasketPropertiesViewModel>() {

    override fun getLayoutId(): Int = R.layout.fragment_basket_properties

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix("16")

    override fun getViewModel(): BasketPropertiesViewModel {
        provideViewModel(BasketPropertiesViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.basket_properties)

        connectLiveData(vm.title, topToolbarUiModel.title)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
    }

}
