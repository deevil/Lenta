package com.lenta.bp9.features.goods_list

import com.lenta.bp9.R
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import com.lenta.bp9.BR
import com.lenta.bp9.databinding.*
import com.lenta.shared.keys.KeyCode
import com.lenta.shared.keys.OnKeyDownListener
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.databinding.*
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.getFragmentResultCode

class GoodsListFragment : CoreFragment<FragmentGoodsListBinding, GoodsListViewModel>(),
        ViewPagerSettings,
        OnScanResultListener,
        PageSelectionListener,
        ToolbarButtonsClickListener,
        OnBackPresserListener {

    private var countedRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null

    override fun getLayoutId(): Int = R.layout.fragment_goods_list

    override fun getPageNumber(): String = "9/15"

    override fun getViewModel(): GoodsListViewModel {
        provideViewModel(GoodsListViewModel::class.java).let {vm ->
            getAppComponent()?.inject(vm)
            vm.titleProgressScreen.value = getString(R.string.data_loading)
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
        topToolbarUiModel.description.value = getString(R.string.list_of_goods)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.refusal)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.clean)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.batches)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.save)

        connectLiveData(vm.visibilityCleanButton, bottomToolbarUiModel.uiModelButton3.visibility)
        connectLiveData(vm.enabledCleanButton, bottomToolbarUiModel.uiModelButton3.enabled)
        connectLiveData(vm.visibilityBatchesButton, bottomToolbarUiModel.uiModelButton4.visibility)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_2 -> vm.onClickRefusal()
            R.id.b_3 -> vm.onClickClean()
            R.id.b_4 -> vm.onClickBatches()
            R.id.b_5 -> vm.onClickSave()
        }
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        if (position == 0) {
            DataBindingUtil
                    .inflate<LayoutGoodsListCountedBinding>(LayoutInflater.from(container.context),
                            R.layout.layout_goods_list_counted,
                            container,
                            false).let { layoutBinding ->

                        val onClickSelectionListener = View.OnClickListener {
                            (it!!.tag as Int).let { position ->
                                vm.countedSelectionsHelper.revert(position = position)
                                layoutBinding.rv.adapter?.notifyItemChanged(position)
                            }
                        }

                        layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                                layoutId = R.layout.item_tile_goods_list_counted,
                                itemId = BR.vm,
                                realisation = object : DataBindingAdapter<ItemTileGoodsListCountedBinding> {
                                    override fun onCreate(binding: ItemTileGoodsListCountedBinding) {
                                    }

                                    override fun onBind(binding: ItemTileGoodsListCountedBinding, position: Int) {
                                        binding.tvCounter.tag = position
                                        binding.tvCounter.setOnClickListener(onClickSelectionListener)
                                        binding.selectedForDelete = vm.countedSelectionsHelper.isSelected(position)
                                        countedRecyclerViewKeyHandler?.let {
                                            binding.root.isSelected = it.isSelected(position)
                                        }
                                    }

                                },
                                onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                                    countedRecyclerViewKeyHandler?.let {
                                        if (it.isSelected(position)) {
                                            vm.onClickItemPosition(position)
                                        } else {
                                            it.selectPosition(position)
                                        }
                                    }

                                }
                        )

                        layoutBinding.vm = vm
                        layoutBinding.lifecycleOwner = viewLifecycleOwner
                        countedRecyclerViewKeyHandler = RecyclerViewKeyHandler(
                                rv = layoutBinding.rv,
                                items = vm.listCounted,
                                lifecycleOwner = layoutBinding.lifecycleOwner!!,
                                initPosInfo = countedRecyclerViewKeyHandler?.posInfo?.value
                        )
                        return layoutBinding.root
                    }
        }

        DataBindingUtil
                .inflate<LayoutGoodsListWithoutBarcodeBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_goods_list_without_barcode,
                        container,
                        false).let { layoutBinding ->

                    val onClickGoodsTitle = View.OnClickListener {
                        (it!!.tag as Int).let { position ->
                            vm.onClickItemPosition(position)
                        }
                    }

                    layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                            layoutId = R.layout.item_tile_goods_list_without_barcode,
                            itemId = BR.vm,
                            realisation = object : DataBindingAdapter<ItemTileGoodsListWithoutBarcodeBinding> {
                                override fun onCreate(binding: ItemTileGoodsListWithoutBarcodeBinding) {
                                }

                                override fun onBind(binding: ItemTileGoodsListWithoutBarcodeBinding, position: Int) {
                                    binding.tvCounter.tag = position
                                    binding.tvCounter.setOnClickListener(onClickGoodsTitle)
                                    binding.tvGoodsTitle.tag = position
                                    binding.tvGoodsTitle.setOnClickListener(onClickGoodsTitle)
                                }

                            }
                    )

                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner
                    return layoutBinding.root
                }
    }

    override fun getTextTitle(position: Int): String = getString(if (position == 0) R.string.counted else R.string.without_barcode)

    override fun onPageSelected(position: Int) {
        vm.onPageSelected(position)
    }

    override fun countTab(): Int {
        return 2
    }

    override fun onResume() {
        super.onResume()
        vm.onResume()
    }

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }

    override fun onFragmentResult(arguments: Bundle) {
        super.onFragmentResult(arguments)
        vm.onResult(arguments.getFragmentResultCode())
    }

    override fun onBackPressed(): Boolean {
        vm.onBackPressed()
        return false
    }

}
