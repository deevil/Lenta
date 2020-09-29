package com.lenta.bp12.features.open_task.good_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.lenta.bp12.BR
import com.lenta.bp12.R
import com.lenta.bp12.databinding.*
import com.lenta.bp12.platform.extention.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class GoodListFragment : CoreFragment<FragmentGoodListBinding, GoodListViewModel>(),
        ToolbarButtonsClickListener, ViewPagerSettings, OnScanResultListener {

    override fun getLayoutId(): Int = R.layout.fragment_good_list

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix(SCREEN_NUMBER)

    override fun getViewModel(): GoodListViewModel {
        provideViewModel(GoodListViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = vm.description

        connectLiveData(vm.title, topToolbarUiModel.title)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.delete, enabled = false)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.print, enabled = false)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.save, enabled = false)

        connectLiveData(vm.deleteEnabled, bottomToolbarUiModel.uiModelButton3.enabled)
        connectLiveData(vm.deleteVisible, bottomToolbarUiModel.uiModelButton3.visibility)
        connectLiveData(vm.printEnabled, bottomToolbarUiModel.uiModelButton4.enabled)
        connectLiveData(vm.printVisibility, bottomToolbarUiModel.uiModelButton4.visibility)
        connectLiveData(vm.saveEnabled, bottomToolbarUiModel.uiModelButton5.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_3 -> vm.onClickDelete()
            R.id.b_4 -> vm.onPrint()
            R.id.b_5 -> vm.onClickSave()
        }
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return when (position) {
            TAB_PROCESSING -> initGoodListNotProcessed(container)
            TAB_PROCESSED -> initGoodListProcessed(container)
            TAB_BASKET -> {
                if (vm.manager.isWholesaleTaskType) initGoodListWholesaleBasket(container)
                else initGoodListCommonBasket(container)
            }
            else -> View(context)
        }
    }

    private fun initGoodListNotProcessed(container: ViewGroup): View {
        val layoutBinding = DataBindingUtil.inflate<LayoutGoodListProcessingBinding>(LayoutInflater.from(container.context),
                R.layout.layout_good_list_processing,
                container,
                false)

        val onClickSelectionListener = View.OnClickListener {
            (it.tag as? Int)?.let { position ->
                vm.processingSelectionsHelper.revert(position = position)
                layoutBinding.rv.adapter?.notifyItemChanged(position)
            }
        }

        layoutBinding.rvConfig = initRecycleAdapterDataBinding(
                layoutId = R.layout.item_good_list_processing,
                itemId = BR.item,
                onItemBind = { binding: ItemGoodListProcessingBinding, position: Int ->
                    binding.tvItemNumber.tag = position
                    if (!vm.isTaskStrict) {
                        binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                    }
                    binding.selectedForDelete = vm.processingSelectionsHelper.isSelected(position)
                },
                keyHandlerId = TAB_PROCESSING,
                recyclerView = layoutBinding.rv,
                items = vm.processing,
                onClickHandler = vm::onClickItemPosition
        )

        layoutBinding.vm = vm
        layoutBinding.lifecycleOwner = viewLifecycleOwner

        return layoutBinding.root
    }

    private fun initGoodListProcessed(container: ViewGroup): View {
        val layoutBinding = DataBindingUtil.inflate<LayoutGoodListProcessedBinding>(LayoutInflater.from(container.context),
                R.layout.layout_good_list_processed,
                container,
                false)

        val onClickSelectionListener = View.OnClickListener {
            (it.tag as? Int)?.let { position ->
                vm.processedSelectionsHelper.revert(position = position)
                layoutBinding.rv.adapter?.notifyItemChanged(position)
            }
        }

        layoutBinding.rvConfig = initRecycleAdapterDataBinding(
                layoutId = R.layout.item_good_list_processed,
                itemId = BR.item,
                onItemBind = { binding: ItemGoodListProcessedBinding, position: Int ->
                    binding.tvItemNumber.tag = position
                    binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                    binding.selectedForDelete = vm.processedSelectionsHelper.isSelected(position)
                },
                keyHandlerId = TAB_PROCESSED,
                recyclerView = layoutBinding.rv,
                items = vm.processed,
                onClickHandler = vm::onClickItemPosition
        )

        layoutBinding.vm = vm
        layoutBinding.lifecycleOwner = viewLifecycleOwner

        return layoutBinding.root

    }

    private fun initGoodListWholesaleBasket(container: ViewGroup): View {
        val layoutBinding = DataBindingUtil.inflate<LayoutGoodListWholesaleBasketsBinding>(LayoutInflater.from(container.context),
                R.layout.layout_good_list_wholesale_baskets,
                container,
                false)

        val onClickSelectionListener = View.OnClickListener {
            (it.tag as? Int)?.let { position ->
                vm.basketSelectionsHelper.revert(position = position)
                layoutBinding.rv.adapter?.notifyItemChanged(position)
            }
        }

        layoutBinding.rvConfig = initRecycleAdapterDataBinding(
                layoutId = R.layout.item_wholesale_basket,
                itemId = BR.item,
                onItemBind = { binding: ItemWholesaleBasketBinding, position: Int ->
                    binding.tvItemNumber.tag = position
                    binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                    binding.selectedForDelete = vm.basketSelectionsHelper.isSelected(position)
                },
                keyHandlerId = TAB_BASKET,
                recyclerView = layoutBinding.rv,
                items = vm.wholesaleBaskets,
                onClickHandler = vm::onClickItemPosition
        )

        layoutBinding.vm = vm
        layoutBinding.lifecycleOwner = viewLifecycleOwner

        return layoutBinding.root
    }


    private fun initGoodListCommonBasket(container: ViewGroup): View {
        val layoutBinding = DataBindingUtil.inflate<LayoutGoodListCommonBasketsBinding>(LayoutInflater.from(container.context),
                R.layout.layout_good_list_common_baskets,
                container,
                false)

        val onClickSelectionListener = View.OnClickListener {
            (it.tag as? Int)?.let { position ->
                vm.basketSelectionsHelper.revert(position = position)
                layoutBinding.rv.adapter?.notifyItemChanged(position)
            }
        }

        layoutBinding.rvConfig = initRecycleAdapterDataBinding(
                layoutId = R.layout.item_task_content_common_basket,
                itemId = BR.item,
                onItemBind = { binding: ItemTaskContentCommonBasketBinding, position: Int ->
                    binding.tvItemNumber.tag = position
                    binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                    binding.selectedForDelete = vm.basketSelectionsHelper.isSelected(position)
                },
                keyHandlerId = TAB_BASKET,
                recyclerView = layoutBinding.rv,
                items = vm.commonBaskets,
                onClickHandler = vm::onClickItemPosition
        )

        layoutBinding.vm = vm
        layoutBinding.lifecycleOwner = viewLifecycleOwner

        return layoutBinding.root
    }

    override fun getTextTitle(position: Int): String {
        return when (position) {
            TAB_PROCESSING -> getString(R.string.to_processing)
            TAB_PROCESSED -> getString(R.string.processed)
            TAB_BASKET -> getString(R.string.baskets)
            else -> throw IllegalArgumentException("Wrong pager position!")
        }
    }

    override fun countTab(): Int {
        return vm.getCountTab()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }

    companion object {
        const val SCREEN_NUMBER = "32"

        private const val TAB_PROCESSING = 0
        private const val TAB_PROCESSED = 1
        private const val TAB_BASKET = 2
    }

}
