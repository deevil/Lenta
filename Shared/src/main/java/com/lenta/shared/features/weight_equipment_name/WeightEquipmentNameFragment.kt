package com.lenta.shared.features.weight_equipment_name

import android.view.View
import com.lenta.shared.R
import com.lenta.shared.databinding.FragmentWeightEquipmentNameBinding
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class WeightEquipmentNameFragment : CoreFragment<FragmentWeightEquipmentNameBinding, WeightEquipmentNameViewModel>(),
        ToolbarButtonsClickListener {

    override fun getLayoutId(): Int = R.layout.fragment_weight_equipment_name

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix("53")

    override fun getViewModel(): WeightEquipmentNameViewModel {
        provideViewModel(WeightEquipmentNameViewModel::class.java).let {
            coreComponent.inject(it)
            //it.setTxtNotFoundPrinter(getString(R.string.printer_not_found))
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.change_weight_equipment)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_5 -> vm.onClickApply()
        }
    }

}
