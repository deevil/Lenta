package com.lenta.bp9.features.goods_information.excise_alco.task_ppp.alco_boxed.box_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.lenta.bp9.BR
import com.lenta.bp9.R
import com.lenta.bp9.databinding.*
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.KeyDownCoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

class ExciseAlcoBoxListFragment : KeyDownCoreFragment<FragmentExciseAlcoBoxListBinding, ExciseAlcoBoxListViewModel>(),
        ViewPagerSettings,
        PageSelectionListener,
        ToolbarButtonsClickListener,
        OnScanResultListener {

    private var productInfo by state<TaskProductInfo?>(null)
    private var selectQualityCode by state<String?>(null)
    private var selectReasonRejectionCode by state<String?>(null)
    private var initialCount by state<String?>(null)

    override fun getLayoutId(): Int = R.layout.fragment_excise_alco_box_list

    override fun getPageNumber(): String = SCREEN_NUMBER

    override fun getViewModel(): ExciseAlcoBoxListViewModel {
        provideViewModel(ExciseAlcoBoxListViewModel::class.java).let { vm ->
            getAppComponent()?.inject(vm)
            vm.productInfo.value = productInfo
            vm.selectQualityCode.value = this.selectQualityCode
            vm.initialCount.value = this.initialCount
            selectReasonRejectionCode?.let {
                vm.selectReasonRejectionCode.value = it
            }
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = "${vm.productInfo.value?.getMaterialLastSix()} ${vm.productInfo.value?.description}"
        topToolbarUiModel.description.value = vm.getDescription() //"Список коробов"
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.clean, visible = vm.visibilityCleanButton.value
                ?: false, enabled = vm.enabledCleanButton.value ?: false)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply)

        connectLiveData(vm.visibilityCleanButton, bottomToolbarUiModel.uiModelButton3.visibility)
        connectLiveData(vm.enabledCleanButton, bottomToolbarUiModel.uiModelButton3.enabled)
        connectLiveData(vm.enabledHandleGoodsButton, bottomToolbarUiModel.uiModelButton4.enabled)

        viewLifecycleOwner.apply {
            vm.selectedPage.observe(this, Observer {
                if (it == 0) {
                    bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.selectAll, enabled = selectQualityCode != "1")
                    bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.handleGoods, enabled = selectQualityCode != "1" && vm.enabledHandleGoodsButton.value == true)
                } else {
                    bottomToolbarUiModel.uiModelButton2.clean()
                    bottomToolbarUiModel.uiModelButton4.clean()
                }
            })
        }

    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        if (position == TAB_NOT_PROCESSED) {
            DataBindingUtil
                    .inflate<LayoutExciseAlcoBoxListNotProcessedBinding>(LayoutInflater.from(container.context),
                            R.layout.layout_excise_alco_box_list_not_processed,
                            container,
                            false)
                    .let { layoutBinding ->
                        val onClickSelectionListener = View.OnClickListener {
                            (it!!.tag as Int).let { position ->
                                vm.notProcessedSelectionsHelper.revert(position = position)
                                layoutBinding.rv.adapter?.notifyItemChanged(position)
                            }
                        }

                        layoutBinding.rvConfig = initRecycleAdapterDataBinding(
                                layoutId = R.layout.item_tile_excis_alco_box_list_not_processed,
                                itemId = BR.item,
                                onItemBind = { binding: ItemTileExcisAlcoBoxListNotProcessedBinding, position: Int ->
                                    binding.tvItemNumber.tag = position
                                    binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                                    binding.selectedItem = if (vm.isSelectAll.value == true) true else vm.notProcessedSelectionsHelper.isSelected(position)
                                },
                                keyHandlerId = TAB_NOT_PROCESSED,
                                recyclerView = layoutBinding.rv,
                                items = vm.countNotProcessed,
                                onClickHandler = vm::onClickItemPosition
                        )

                        layoutBinding.vm = vm
                        layoutBinding.lifecycleOwner = viewLifecycleOwner

                        return layoutBinding.root
                    }
        }

        DataBindingUtil
                .inflate<LayoutExciseAlcoBoxListProcessedBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_excise_alco_box_list_processed,
                        container,
                        false)
                .let { layoutBinding ->
                    val onClickSelectionListener = View.OnClickListener {
                        (it!!.tag as Int).let { position ->
                            vm.processedSelectionsHelper.revert(position = position)
                            layoutBinding.rv.adapter?.notifyItemChanged(position)
                        }
                    }

                    layoutBinding.rvConfig = initRecycleAdapterDataBinding(
                            layoutId = R.layout.item_tile_excise_alco_box_list_processed,
                            itemId = BR.item,
                            onItemBind = { binding: ItemTileExciseAlcoBoxListProcessedBinding, position: Int ->
                                binding.tvItemNumber.tag = position
                                binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                                binding.selectedForDelete = vm.processedSelectionsHelper.isSelected(position)
                            },
                            keyHandlerId = TAB_PROCESSED,
                            recyclerView = layoutBinding.rv,
                            items = vm.countNotProcessed,
                            onClickHandler = vm::onClickItemPosition
                    )

                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner

                    return layoutBinding.root
                }
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_2 -> vm.onClickSelectAll()
            R.id.b_3 -> vm.onClickClean()
            R.id.b_4 -> vm.onClickHandleGoods()
            R.id.b_5 -> vm.onClickApply()
        }
    }

    override fun getTextTitle(position: Int): String = getString(if (position == TAB_NOT_PROCESSED) R.string.to_processing else R.string.processed)

    override fun countTab(): Int = TABS

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }

    override fun onPageSelected(position: Int) {
        vm.onPageSelected(position)
    }

    override fun onScanResult(data: String) {
        vm.isScan.value = true
        vm.onScanResult(data)
    }

    override fun onResume() {
        super.onResume()
        vm.onResume()
    }

    companion object {
        fun create(productInfo: TaskProductInfo, selectQualityCode: String, selectReasonRejectionCode: String?, initialCount: String): ExciseAlcoBoxListFragment {
            ExciseAlcoBoxListFragment().let {
                it.productInfo = productInfo
                it.selectQualityCode = selectQualityCode
                it.selectReasonRejectionCode = selectReasonRejectionCode
                it.initialCount = initialCount
                return it
            }
        }

        const val SCREEN_NUMBER = "09/42"

        private const val TABS = 2
        private const val TAB_NOT_PROCESSED = 0
        private const val TAB_PROCESSED = 1
    }

}
