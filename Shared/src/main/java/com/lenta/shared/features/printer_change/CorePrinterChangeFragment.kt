package com.lenta.shared.features.printer_change

import android.view.View
import com.lenta.shared.R
import com.lenta.shared.databinding.FragmentPrinterChangeBinding
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel

abstract class CorePrinterChangeFragment : CoreFragment<FragmentPrinterChangeBinding, CorePrinterChangeViewModel>(), OnBackPresserListener, ToolbarButtonsClickListener {

    override fun getLayoutId(): Int = R.layout.fragment_printer_change

    override fun onBackPressed(): Boolean {
        vm.onBackPressed()
        return true
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_1 -> vm.onClickBack()
            R.id.b_5 -> vm.onClickApp()
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = resources.getString(R.string.printer_change)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.cleanAll()
        bottomToolbarUiModel.uiModelButton1.let { buttonUiModel -> buttonUiModel.show(ButtonDecorationInfo.back) }
        bottomToolbarUiModel.uiModelButton5.let { buttonUiModel -> buttonUiModel.show(ButtonDecorationInfo.apply) }
    }


}
