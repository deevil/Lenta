package com.lenta.bp9.features.supply_results

import android.os.Bundle
import android.view.View
import com.lenta.bp9.R
import com.lenta.bp9.databinding.FragmentSupplyResultsBinding
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

class SupplyResultsFragment : CoreFragment<FragmentSupplyResultsBinding, SupplyResultsViewModel>(),
        ToolbarButtonsClickListener {

    companion object {
        fun create(pageNumber: String, numberSupply: String, isAutomaticWriteOff: Boolean): SupplyResultsFragment {
            SupplyResultsFragment().let {
                it.pageNumber = pageNumber
                it.numberSupply = numberSupply
                it.isAutomaticWriteOff = isAutomaticWriteOff
                return it
            }
        }
    }

    private var pageNumber: String = ""
    private var numberSupply by state<String?>(null)
    private var isAutomaticWriteOff by state<Boolean?>(null)

    override fun getLayoutId(): Int = R.layout.fragment_supply_results

    override fun getPageNumber(): String = "09/${this.pageNumber}"

    override fun getViewModel(): SupplyResultsViewModel {
        provideViewModel(SupplyResultsViewModel::class.java).let { vm ->
            getAppComponent()?.inject(vm)
            vm.numberSupply.value = this.numberSupply
            vm.isAutomaticWriteOff.value = this.isAutomaticWriteOff
            return vm
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.message.value = if (this.isAutomaticWriteOff == false)
            context?.getString(R.string.supply_results_success_dialog, this.numberSupply)
        else
            context?.getString(R.string.supply_results_automatic_charge_success, this.numberSupply)
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
        topToolbarUiModel.description.value = getString(R.string.supply_results)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.docs)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.next)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_4 -> vm.onClickDocs()
            R.id.b_5 -> vm.onClickNext()
        }
    }

}
