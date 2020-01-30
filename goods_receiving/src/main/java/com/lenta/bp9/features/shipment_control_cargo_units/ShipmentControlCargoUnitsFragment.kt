package com.lenta.bp9.features.shipment_control_cargo_units

import com.lenta.bp9.R
import com.lenta.bp9.databinding.FragmentShipmentControlCargoUnitsBinding
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel
import android.os.Bundle
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import android.view.ViewGroup
import android.view.View
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo

class ShipmentControlCargoUnitsFragment : CoreFragment<FragmentShipmentControlCargoUnitsBinding, ShipmentControlCargoUnitsViewModel>(), ViewPagerSettings {

    override fun getLayoutId(): Int = R.layout.fragment_shipment_control_cargo_units

    override fun getPageNumber(): String = "09/118"

    override fun getViewModel(): ShipmentControlCargoUnitsViewModel {
        provideViewModel(ShipmentControlCargoUnitsViewModel::class.java).let {vm ->
            getAppComponent()?.inject(vm)
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.taskCaption
        topToolbarUiModel.description.value = getString(R.string.shipment_control_cargo_units)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
                bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.missing)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.nextAlternate)
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return View(context)
    }

    override fun getTextTitle(position: Int): String {
        return "Title: $position"
    }

    override fun countTab(): Int {
        return 2
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }


}
