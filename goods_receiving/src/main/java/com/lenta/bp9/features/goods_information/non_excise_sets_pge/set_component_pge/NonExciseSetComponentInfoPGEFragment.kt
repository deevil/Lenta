package com.lenta.bp9.features.goods_information.non_excise_sets_pge.set_component_pge

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import com.lenta.bp9.R
import com.lenta.bp9.databinding.FragmentNonExciseSetComponentInfoPgeBinding
import com.lenta.bp9.features.goods_information.non_excise_sets_pge.NonExciseSetsPGEFragment
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.model.task.TaskSetsInfo
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.DateInputMask
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

class NonExciseSetComponentInfoPGEFragment : CoreFragment<FragmentNonExciseSetComponentInfoPgeBinding, NonExciseSetComponentInfoPGEViewModel>() {

    companion object {
        fun create(setInfo: TaskSetsInfo): NonExciseSetComponentInfoPGEFragment {
            NonExciseSetComponentInfoPGEFragment().let {
                it.setInfo = setInfo
                return it
            }
        }
    }

    private var setInfo by state<TaskSetsInfo?>(null)

    override fun getLayoutId(): Int = R.layout.fragment_non_excise_set_component_info_pge

    override fun getPageNumber(): String = "09/40"

    override fun getViewModel(): NonExciseSetComponentInfoPGEViewModel {
        provideViewModel(NonExciseSetComponentInfoPGEViewModel::class.java).let {vm ->
            getAppComponent()?.inject(vm)
            vm.setInfo.value = this.setInfo
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
        topToolbarUiModel.description.value = getString(R.string.set_component)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.reset)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.spinnerQuality?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View?, position: Int, l: Long) {
                vm.onClickPositionSpinQuality(position)
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {
            }
        }

        binding?.spinnerManufacturers?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View?, position: Int, l: Long) {
                vm.onClickPositionSpinManufacturers(position)
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {
            }
        }

        binding?.spinnerBottlingDate?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View?, position: Int, l: Long) {
                vm.onClickPositionBottlingDate(position)
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {
            }
        }
    }

}
