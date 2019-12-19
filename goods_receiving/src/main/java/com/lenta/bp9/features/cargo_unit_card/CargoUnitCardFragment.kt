package com.lenta.bp9.features.cargo_unit_card

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import com.lenta.bp9.R
import com.lenta.bp9.databinding.FragmentCargoUnitCardBinding
import com.lenta.bp9.model.task.TaskCargoUnitInfo
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

class CargoUnitCardFragment : CoreFragment<FragmentCargoUnitCardBinding, CargoUnitCardViewModel>(), ToolbarButtonsClickListener {

    companion object {
        fun create(cargoUnitInfo: TaskCargoUnitInfo): CargoUnitCardFragment {
            CargoUnitCardFragment().let {
                it.cargoUnitInfo = cargoUnitInfo
                return it
            }
        }
    }

    private var cargoUnitInfo by state<TaskCargoUnitInfo?>(null)

    override fun getLayoutId(): Int = R.layout.fragment_cargo_unit_card

    override fun getPageNumber(): String = "09/27"

    override fun getViewModel(): CargoUnitCardViewModel {
        provideViewModel(CargoUnitCardViewModel::class.java).let {vm ->
            getAppComponent()?.inject(vm)
            vm.cargoUnitInfo.value = this.cargoUnitInfo
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
        topToolbarUiModel.description.value = "${getString(R.string.reduction_cargo_unit)} ${vm.cargoUnitInfo.value?.cargoUnitNumber}"
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.spinnerStatus?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View?, position: Int, l: Long) {
                vm.onClickPositionSpinStatus(position)
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {
            }
        }
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_5 -> vm.onClickApply()
        }
    }

}
