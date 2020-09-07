package com.lenta.bp9.features.label_printing

import android.os.Bundle
import android.view.View
import com.lenta.bp9.BR
import com.lenta.bp9.R
import com.lenta.bp9.databinding.FragmentLabelPrintingBinding
import com.lenta.bp9.databinding.ItemTileAlcoholBatchBinding
import com.lenta.bp9.databinding.ItemTileGoodsDetailsDelBinding
import com.lenta.bp9.databinding.ItemTileLabelPrintingBinding
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.provideViewModel

class LabelPrintingFragment : CoreFragment<FragmentLabelPrintingBinding, LabelPrintingViewModel>(),
        ToolbarButtonsClickListener {

    override fun getLayoutId(): Int = R.layout.fragment_label_printing

    override fun getPageNumber(): String = "09/118"

    override fun getViewModel(): LabelPrintingViewModel {
        provideViewModel(LabelPrintingViewModel::class.java).let { vm ->
            getAppComponent()?.inject(vm)
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
        topToolbarUiModel.description.value = getString(R.string.label_printing)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.print)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.nextAlternate)

        connectLiveData(vm.enabledNextBtn, bottomToolbarUiModel.uiModelButton5.enabled)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRvConfig()
    }

    private fun initRvConfig() {
        binding?.let { layoutBinding ->
            val onClickSelectionListener = View.OnClickListener {
                (it!!.tag as Int).let { position ->
                    vm.labelSelectionsHelper.revert(position = position)
                    layoutBinding.rv.adapter?.notifyItemChanged(position)
                }
            }

            layoutBinding.rvConfig = initRecycleAdapterDataBinding(
                    layoutId = R.layout.item_tile_label_printing,
                    itemId = BR.item,
                    onAdapterItemBind = { binding: ItemTileLabelPrintingBinding, position: Int ->
                        binding.tvItemNumber.tag = position
                        binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                        binding.selectedForDelete = vm.labelSelectionsHelper.isSelected(position)
                    }
            )

            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner
        }
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_4 -> vm.onClickPrint()
            R.id.b_5 -> vm.onClickNext()
        }
    }

    override fun onFragmentResult(arguments: Bundle) {
        super.onFragmentResult(arguments)
        vm.markPrintedLabels(arguments)
    }


}
