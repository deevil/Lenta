package com.lenta.shared.features.test_environment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import com.lenta.shared.R
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumber
import com.lenta.shared.utilities.extentions.getScreenPrefix
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

class PinCodeFragment : CoreFragment<com.lenta.shared.databinding.FragmentPinCodeBinding, PinCodeViewModel>(), ToolbarButtonsClickListener {

    private var requestCode by state<Int?>(null)
    private var message by state<String?>(null)

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
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.goOver, enabled = false)

        viewLifecycleOwner.let {
            connectLiveData(vm.enabledGoOverBtn, bottomToolbarUiModel.uiModelButton5.enabled)
        }
    }

    override fun getPageNumber(): String = generateScreenNumber()

    override fun getViewModel(): PinCodeViewModel {
        provideViewModel(PinCodeViewModel::class.java).let { viewModel ->
            coreComponent.inject(viewModel)

            viewModel.setPrefixScreen(getScreenPrefix())

            viewModel.setMsgIncorrectPinCode(getString(R.string.msg_incorrect_pin_code))

            requestCode?.let {
                viewModel.requestCode = it
            }

            message?.let {
                viewModel.message.value = it
            }

            return viewModel
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.let { it.etPin1.addTextChangedListener(EnterCodeTextWatcher(binding?.etPin2)) }
        binding?.let { it.etPin2.addTextChangedListener(EnterCodeTextWatcher(binding?.etPin3)) }
        binding?.let { it.etPin3.addTextChangedListener(EnterCodeTextWatcher(binding?.etPin4)) }
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

class EnterCodeTextWatcher (private var nextFocus: EditText?) : TextWatcher {
    override fun afterTextChanged(s: Editable?) {
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        nextFocus?.let { it.requestFocus() }
    }

}