package com.lenta.bp9.features.reconciliation_mercury

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.lifecycle.Observer
import com.lenta.bp9.R
import com.lenta.bp9.databinding.FragmentReconciliationMercuryBinding
import com.lenta.bp9.model.task.revise.ProductVetDocumentRevise
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

class ReconciliationMercuryFragment : CoreFragment<FragmentReconciliationMercuryBinding, ReconciliationMercuryViewModel>(),
        ToolbarButtonsClickListener,
        OnBackPresserListener {

    companion object {
        fun create(productVetDoc: ProductVetDocumentRevise): ReconciliationMercuryFragment {
            ReconciliationMercuryFragment().let {
                it.productVetDoc = productVetDoc
                return it
            }
        }
    }

    private var productVetDoc by state<ProductVetDocumentRevise?>(null)

    override fun getLayoutId(): Int = R.layout.fragment_reconciliation_mercury

    override fun getPageNumber(): String = "09/101"

    override fun getViewModel(): ReconciliationMercuryViewModel {
        provideViewModel(ReconciliationMercuryViewModel::class.java).let {vm ->
            getAppComponent()?.inject(vm)
            vm.productVetDoc.value = this.productVetDoc
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
        topToolbarUiModel.description.value = getString(R.string.reconciliation_mercury)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.next)
        viewLifecycleOwner.apply {
            if (vm.productVetDoc.value!!.isAttached) {
                bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.untie)
            } else {
                bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.tied)
            }
            connectLiveData(vm.enabledReconciliationCheck, bottomToolbarUiModel.uiModelButton2.enabled)
        }
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_2 -> vm.onClickTiedUntied()
            R.id.b_5 -> vm.onClickNext()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.cbChecked?.setOnClickListener {
            if (it is CheckBox) {
                vm.onClickReconciliationCheck(it.isChecked)
            }
        }
    }

    override fun onBackPressed(): Boolean {
        vm.onBackPressed()
        return false
    }

}
