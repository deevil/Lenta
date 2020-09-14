package com.lenta.bp14.features.price_check.goods_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.lenta.bp14.BR
import com.lenta.bp14.R
import com.lenta.bp14.databinding.*
import com.lenta.bp14.di.CheckPriceComponent
import com.lenta.bp14.features.common_ui_model.SimpleProductUi
import com.lenta.shared.di.CoreInjectHelper
import com.lenta.shared.platform.fragment.KeyDownCoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.databinding.RecyclerViewKeyHandler
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class GoodsListPcFragment : KeyDownCoreFragment<FragmentGoodsListPcBinding, GoodsListPcViewModel>(),
        ViewPagerSettings, ToolbarButtonsClickListener, OnScanResultListener {

    private var processingRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null
    private var processedRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null
    private var searchRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null

    override fun getLayoutId(): Int = R.layout.fragment_goods_list_pc

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix(SCREEN_NUMBER)

    override fun getViewModel(): GoodsListPcViewModel {
        provideViewModel(GoodsListPcViewModel::class.java).let {
            CoreInjectHelper.getComponent(CheckPriceComponent::class.java)!!.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.goods_list)

        topToolbarUiModel.title.value = vm.taskName
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.video)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.delete)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.print)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.save)

        connectLiveData(vm.videoButtonEnabled, getBottomToolBarUIModel()!!.uiModelButton2.enabled)
        connectLiveData(vm.videoButtonVisibility, getBottomToolBarUIModel()!!.uiModelButton2.visibility)
        connectLiveData(vm.deleteButtonEnabled, getBottomToolBarUIModel()!!.uiModelButton3.enabled)
        connectLiveData(vm.deleteButtonVisibility, getBottomToolBarUIModel()!!.uiModelButton3.visibility)
        connectLiveData(vm.printButtonEnabled, getBottomToolBarUIModel()!!.uiModelButton4.enabled)
        connectLiveData(vm.printButtonVisibility, getBottomToolBarUIModel()!!.uiModelButton4.visibility)
        connectLiveData(vm.saveButtonEnabled, getBottomToolBarUIModel()!!.uiModelButton5.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_2 -> if (isGoogleServicesAvailable()) vm.onClickVideo() else vm.showVideoErrorMessage()
            R.id.b_3 -> vm.onClickDelete()
            R.id.b_4 -> vm.onClickPrint()
            R.id.b_5 -> vm.onClickSave()
        }
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return when (vm.getCorrectedPagePosition(position)) {
            TAB_PROCESSING -> initProcessingGoodList(container)
            TAB_PROCESSED -> initProcessedGoodList(container)
            TAB_SEARCH -> initSearchGoodList(container)
            else -> View(context)
        }
    }

    private fun initProcessingGoodList(container: ViewGroup): View {
        DataBindingUtil.inflate<LayoutPcGoodsListProcessingBinding>(LayoutInflater.from(container.context),
                R.layout.layout_pc_goods_list_processing,
                container,
                false).let { layoutBinding ->

            layoutBinding.rvConfig = initRecycleAdapterDataBinding<SimpleProductUi, ItemSimpleGoodBinding>(
                    layoutId = R.layout.item_simple_good,
                    itemId = BR.vm,
                    keyHandlerId = TAB_PROCESSING,
                    recyclerView = layoutBinding.rv,
                    items = vm.processingGoods,
                    onClickHandler = vm::onClickItemPosition
            )

            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner

            return layoutBinding.root
        }
    }

    private fun initProcessedGoodList(container: ViewGroup): View {
        DataBindingUtil.inflate<LayoutPcGoodsListProcessedBinding>(LayoutInflater.from(container.context),
                R.layout.layout_pc_goods_list_processed,
                container,
                false).let { layoutBinding ->

            val onClickSelectionListener = View.OnClickListener {
                (it!!.tag as Int).let { position ->
                    vm.processedSelectionsHelper.revert(position = position)
                    layoutBinding.rv.adapter?.notifyItemChanged(position)
                }
            }

            layoutBinding.rvConfig = initRecycleAdapterDataBinding(
                    layoutId = R.layout.item_good_check_selectable,
                    itemId = BR.vm,
                    onItemBind = { binding: ItemGoodQuantitySelectableBinding, position: Int ->
                        binding.tvItemNumber.tag = position
                        binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                        binding.selectedForDelete = vm.processedSelectionsHelper.isSelected(position)
                    },
                    keyHandlerId = TAB_PROCESSED,
                    recyclerView = layoutBinding.rv,
                    items = vm.processedGoods,
                    onClickHandler = vm::onClickItemPosition
            )

            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner

            return layoutBinding.root
        }
    }

    private fun initSearchGoodList(container: ViewGroup): View {
        DataBindingUtil.inflate<LayoutPcGoodsListSearchBinding>(LayoutInflater.from(container.context),
                R.layout.layout_pc_goods_list_search,
                container,
                false).let { layoutBinding ->

            val onClickSelectionListener = View.OnClickListener {
                (it!!.tag as Int).let { position ->
                    vm.searchSelectionsHelper.revert(position = position)
                    layoutBinding.rv.adapter?.notifyItemChanged(position)
                }
            }

            layoutBinding.rvConfig = initRecycleAdapterDataBinding(
                    layoutId = R.layout.item_good_check_selectable,
                    itemId = BR.vm,
                    onItemBind = { binding: ItemGoodCheckSelectableBinding, position: Int ->
                        binding.tvItemNumber.tag = position
                        binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                        binding.selectedForDelete = vm.searchSelectionsHelper.isSelected(position)
                    },
                    keyHandlerId = TAB_SEARCH,
                    recyclerView = layoutBinding.rv,
                    items = vm.searchGoods,
                    onClickHandler = vm::onClickItemPosition
            )

            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner

            return layoutBinding.root
        }
    }

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }


    override fun getTextTitle(position: Int): String {
        return when (vm.getCorrectedPagePosition(position)) {
            TAB_PROCESSING -> getString(R.string.processing)
            TAB_PROCESSED -> getString(R.string.processed)
            TAB_SEARCH -> getString(R.string.search)
            else -> throw IllegalArgumentException("Wrong pager position!")
        }
    }

    override fun countTab(): Int {
        return vm.getPagesCount()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }

    private fun isGoogleServicesAvailable(): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val status = googleApiAvailability.isGooglePlayServicesAvailable(context)
        return status == ConnectionResult.SUCCESS
    }

    companion object {
        const val SCREEN_NUMBER = "41"

        private const val TAB_PROCESSING = 0
        private const val TAB_PROCESSED = 1
        private const val TAB_SEARCH = 2
    }

}
