package com.lenta.bp9.features.mercury_exception_integration

import android.view.View
import com.lenta.bp9.R
import com.lenta.bp9.databinding.FragmentMercuryExceptionIntegrationBinding
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel

class MercuryExceptionIntegrationFragment : CoreFragment<FragmentMercuryExceptionIntegrationBinding, MercuryExceptionIntegrationViewModel>(), ToolbarButtonsClickListener {

    override fun getLayoutId(): Int = R.layout.fragment_mercury_exception_integration

    override fun getPageNumber(): String = "09/104"

    override fun getViewModel(): MercuryExceptionIntegrationViewModel {
        provideViewModel(MercuryExceptionIntegrationViewModel::class.java).let {vm ->
            getAppComponent()?.inject(vm)
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
        topToolbarUiModel.description.value = getString(R.string.mercury_exception_integration_des)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.next)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_5 -> vm.onClickNext()
        }
    }

}
