package com.lenta.bp16.features.warehouse_selection

import com.lenta.bp16.R
import com.lenta.bp16.platform.extention.getAppComponent
import com.lenta.shared.databinding.FragmentWeightEquipmentNameBinding
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.getAppInfo
import com.lenta.shared.utilities.extentions.provideViewModel

class WarehouseSelectionFragment : CoreFragment<FragmentWeightEquipmentNameBinding, WarehouseSelectionViewModel>() {
    override fun getLayoutId(): Int {
        return R.layout.fragment_select_warehouse
    }

    override fun getPageNumber(): String? {
        return SCREEN_NUMBER
    }

    override fun getViewModel(): WarehouseSelectionViewModel {
        provideViewModel(WarehouseSelectionViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = context?.getAppInfo()
        topToolbarUiModel.description.value = getString(R.string.main_menu)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.next)
    }

    companion object {
        private const val SCREEN_NUMBER = "16/81"
    }
}