package com.lenta.shared.features.weight_equipment_name

import com.lenta.shared.R
import com.lenta.shared.databinding.FragmentWeightEquipmentNameBinding
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel

class WeightEquipmentNameFragment : CoreFragment<FragmentWeightEquipmentNameBinding, WeightEquipmentNameViewModel>() {

    override fun getLayoutId(): Int = R.layout.fragment_weight_equipment_name

    override fun getPageNumber(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getViewModel(): WeightEquipmentNameViewModel {
        provideViewModel(WeightEquipmentNameViewModel::class.java).let {
            coreComponent.inject(it)
            //it.setTxtNotFoundPrinter(getString(R.string.printer_not_found))
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}
