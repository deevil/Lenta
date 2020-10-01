package com.lenta.bp9.features.label_printing.print_labels_count_copies

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.AdapterView
import com.lenta.bp9.R
import com.lenta.bp9.databinding.FragmentPrintLabelsCountCopiesBinding
import com.lenta.bp9.features.goods_information.marking.uom_st_without_counting_in_boxes.MarkingInfoFragment
import com.lenta.bp9.features.label_printing.LabelPrintingItem
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.DateInputMask
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

class PrintLabelsCountCopiesFragment : CoreFragment<FragmentPrintLabelsCountCopiesBinding, PrintLabelsCountCopiesViewModel>(),
        ToolbarButtonsClickListener {

    companion object {
        fun newInstance(labels: List<LabelPrintingItem>?): PrintLabelsCountCopiesFragment {
            PrintLabelsCountCopiesFragment().let {
                it.labels = labels
                return it
            }
        }
    }

    private var labels: List<LabelPrintingItem>? = null

    override fun getLayoutId(): Int = R.layout.fragment_print_labels_count_copies

    override fun getPageNumber(): String = "09/119"

    override fun getViewModel(): PrintLabelsCountCopiesViewModel {
        provideViewModel(PrintLabelsCountCopiesViewModel::class.java).let { vm ->
            getAppComponent()?.inject(vm)
            this.labels?.let { vm.initLabels(it) }
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
        topToolbarUiModel.description.value = getString(R.string.label_printing)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.confirm)

        connectLiveData(vm.enabledConfirmBtn, bottomToolbarUiModel.uiModelButton5.enabled)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.etCountCopies?.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                val countCopiesLength = vm.countCopies.value?.length ?: 0
                if (countCopiesLength > 0) {
                    vm.onClickConfirm()
                }
                return@OnKeyListener true
            }
            false
        })
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_5 -> vm.onClickConfirm()
        }
    }

}
