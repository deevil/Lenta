package com.lenta.shared.features.printer_change

import android.view.View
import com.lenta.shared.R
import com.lenta.shared.databinding.FragmentPrinterChangeBinding
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.generateScreenNumber
import com.lenta.shared.utilities.extentions.provideViewModel

class PrinterChangeFragment : CoreFragment<FragmentPrinterChangeBinding, PrinterChangeViewModel>(),
        ToolbarButtonsClickListener {

    override fun getPageNumber(): String = generateScreenNumber()

    override fun getViewModel(): PrinterChangeViewModel {
        provideViewModel(PrinterChangeViewModel::class.java).let {
            coreComponent.inject(it)
            it.setTxtNotFoundPrinter(getString(R.string.printer_not_found))
            return it
        }
    }

    override fun getLayoutId(): Int = R.layout.fragment_printer_change


    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_5 -> vm.onClickApply()
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = resources.getString(R.string.printer_change)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.cleanAll()
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply)
    }

}
