package com.lenta.bp9.features.reconciliation_mercury

import com.lenta.bp9.R
import com.lenta.bp9.databinding.FragmentReconciliationMercuryBinding
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel

class ReconciliationMercuryFragment : CoreFragment<FragmentReconciliationMercuryBinding, ReconciliationMercuryViewModel>() {

    override fun getLayoutId(): Int = R.layout.fragment_reconciliation_mercury

    override fun getPageNumber(): String = "09/101"

    override fun getViewModel(): ReconciliationMercuryViewModel {
        provideViewModel(ReconciliationMercuryViewModel::class.java).let {vm ->
            getAppComponent()?.inject(vm)
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
        topToolbarUiModel.description.value = getString(R.string.reconciliation_mercury)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.untie)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.next)
    }


}
