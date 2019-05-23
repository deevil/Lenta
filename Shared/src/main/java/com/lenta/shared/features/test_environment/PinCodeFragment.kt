package com.lenta.shared.features.test_environment

import android.view.View
import com.lenta.shared.R
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel

class PinCodeFragment : CoreFragment<com.lenta.shared.databinding.FragmentPinCodeBinding, PinCodeViewModel>(), ToolbarButtonsClickListener {

    private var requestCode: Int? = null
    private var message: String? = null

    override fun getLayoutId(): Int = R.layout.fragment_pin_code


    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_5 -> vm.onClickGoOver()
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = resources.getString(R.string.test_environment)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.cleanAll()
        bottomToolbarUiModel.uiModelButton1.let { buttonUiModel -> buttonUiModel.show(ButtonDecorationInfo.back) }
        bottomToolbarUiModel.uiModelButton5.let { buttonUiModel -> buttonUiModel.show(ButtonDecorationInfo.goOver) }
    }

    override fun getPageNumber(): String = "10/56"

    override fun getViewModel(): PinCodeViewModel {
        provideViewModel(PinCodeViewModel::class.java).let { viewModel ->
            coreComponent.inject(viewModel)

            requestCode?.let {
                viewModel.requestCode = it
            }

            message?.let {
                viewModel.message.value = it
            }

            return viewModel
        }
    }

    companion object {
        fun create(requestCode: Int, message: String): PinCodeFragment {
            return PinCodeFragment().apply {
                this.requestCode = requestCode
                this.message = message
            }
        }
    }
}