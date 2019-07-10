package com.lenta.bp7.features.code

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import com.lenta.bp7.R
import com.lenta.bp7.databinding.FragmentCodeBinding
import com.lenta.bp7.platform.extentions.getAppComponent
import com.lenta.bp7.platform.extentions.getAppTitle
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class CodeFragment : CoreFragment<FragmentCodeBinding, CodeViewModel>(), ToolbarButtonsClickListener {

    override fun getLayoutId(): Int = R.layout.fragment_code

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix("06")

    override fun getViewModel(): CodeViewModel {
        provideViewModel(CodeViewModel::class.java).let {
            getAppComponent()?.inject(it)

            it.setIncorrectCodeMessage(getString(R.string.incorrect_code_message))
            it.setTextForCheckType(getString(R.string.self_control_tc), getString(R.string.external_audit))

            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = getAppTitle()
        topToolbarUiModel.description.value = getString(R.string.select_mode)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.cleanAll()
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.goOver, enabled = false)

        viewLifecycleOwner.connectLiveData(vm.enabledGoOverBtn, bottomToolbarUiModel.uiModelButton5.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_5 -> vm.onClickGoOver()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            etNumber1.addTextChangedListener(EnterCodeTextWatcher(binding?.etNumber2))
            etNumber2.addTextChangedListener(EnterCodeTextWatcher(binding?.etNumber3))
            etNumber3.addTextChangedListener(EnterCodeTextWatcher(binding?.etNumber4))
        }
    }
}

class EnterCodeTextWatcher(private var nextFocus: EditText?) : TextWatcher {
    override fun afterTextChanged(s: Editable?) {
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        nextFocus?.let { it.requestFocus() }
    }
}
