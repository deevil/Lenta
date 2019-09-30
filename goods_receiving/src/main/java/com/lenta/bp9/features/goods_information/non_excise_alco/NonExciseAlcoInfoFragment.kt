package com.lenta.bp9.features.goods_information.non_excise_alco

import com.lenta.bp9.R
import com.lenta.bp9.databinding.FragmentNonExciseAlcoInfoBinding
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel

class NonExciseAlcoInfoFragment : CoreFragment<FragmentNonExciseAlcoInfoBinding, NonExciseAlcoInfoViewModel>() {

    override fun getLayoutId(): Int = R.layout.fragment_non_excise_alco_info

    override fun getPageNumber(): String = "09/19"

    override fun getViewModel(): NonExciseAlcoInfoViewModel {
        provideViewModel(NonExciseAlcoInfoViewModel::class.java).let {vm ->
            getAppComponent()?.inject(vm)
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.goods_info)
        topToolbarUiModel.title.value = "${vm.productInfo.value?.getMaterialLastSix()} ${vm.productInfo.value?.description}"
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.details)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.add)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply)
    }


}
