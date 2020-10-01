package com.lenta.bp15.features.good_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.lenta.bp15.BR
import com.lenta.bp15.R
import com.lenta.bp15.databinding.*
import com.lenta.bp15.platform.extention.getAppComponent
import com.lenta.shared.platform.fragment.KeyDownCoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class GoodListFragment : KeyDownCoreFragment<FragmentGoodListBinding, GoodListViewModel>(),
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
        topToolbarUiModel.description.value = getString(R.string.good_list)

        connectLiveData(vm.title, topToolbarUiModel.title)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.complete)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_5 -> vm.onClickComplete()
        }
    }

    override fun onResume() {
        super.onResume()
        vm.requestFocusToNumberField.value = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return when (position) {
            TAB_PROCESSING -> initProcessingList(container)
            TAB_PROCESSED -> initProcessedList(container)
            else -> View(context)
        }
    }

    private fun initProcessingList(container: ViewGroup): View {
        val layoutBinding = DataBindingUtil.inflate<LayoutGoodListProcessingBinding>(LayoutInflater.from(container.context),
                R.layout.layout_good_list_processing,
                container,
                false)

        layoutBinding.rvConfig = initRecycleAdapterDataBinding<ItemGoodUi, ItemGoodListBinding>(
                layoutId = R.layout.item_good_list,
                itemId = BR.item,
                keyHandlerId = TAB_PROCESSING,
                recyclerView = layoutBinding.rv,
                items = vm.processingList,
                onClickHandler = vm::onClickItemProcessingPosition
        )

        layoutBinding.vm = vm
        layoutBinding.lifecycleOwner = viewLifecycleOwner

        return layoutBinding.root
    }

    private fun initProcessedList(container: ViewGroup): View {
        val layoutBinding = DataBindingUtil.inflate<LayoutGoodListProcessedBinding>(LayoutInflater.from(container.context),
                R.layout.layout_good_list_processed,
                container,
                false)

        layoutBinding.rvConfig = initRecycleAdapterDataBinding<ItemGoodUi, ItemGoodListBinding>(
                layoutId = R.layout.item_good_list,
                itemId = BR.item,
                keyHandlerId = TAB_PROCESSED,
                recyclerView = layoutBinding.rv,
                items = vm.processedList,
                onClickHandler = vm::onClickItemProcessedPosition
        )

        layoutBinding.vm = vm
        layoutBinding.lifecycleOwner = viewLifecycleOwner

        return layoutBinding.root
    }


    override fun getTextTitle(position: Int): String {
        return when (position) {
            TAB_PROCESSING -> getString(R.string.to_processing)
            TAB_PROCESSED -> getString(R.string.processed)
            else -> throw IllegalArgumentException("Wrong pager position!")
        }
    }

    override fun countTab(): Int {
        return TABS
    }

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }

    companion object {
        const val SCREEN_NUMBER = "10"

        private const val TABS = 2
        private const val TAB_PROCESSING = 0
        private const val TAB_PROCESSED = 1
    }

}