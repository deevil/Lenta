package com.lenta.bp9.features.label_printing.print_labels_count_copies

import com.lenta.bp9.R
import com.lenta.bp9.databinding.FragmentPrintLabelsCountCopiesBinding
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel

class PrintLabelsCountCopiesFragment : CoreFragment<FragmentPrintLabelsCountCopiesBinding, PrintLabelsCountCopiesViewModel>() {

    override fun getLayoutId(): Int = R.layout.fragment_print_labels_count_copies

    override fun getPageNumber(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getViewModel(): PrintLabelsCountCopiesViewModel {
        provideViewModel(PrintLabelsCountCopiesViewModel::class.java).let { vm ->
            getAppComponent()?.inject(vm)
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}
