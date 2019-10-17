package com.lenta.bp14.features.work_list.goods_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import com.lenta.bp14.BR
import com.lenta.bp14.R
import com.lenta.bp14.databinding.*
import com.lenta.bp14.di.WorkListComponent
import com.lenta.shared.di.CoreInjectHelper
import com.lenta.shared.keys.KeyCode
import com.lenta.shared.keys.OnKeyDownListener
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.databinding.DataBindingAdapter
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.databinding.RecyclerViewKeyHandler
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class GoodsListWlFragment : CoreFragment<FragmentGoodsListWlBinding, GoodsListWlViewModel>(),
        ViewPagerSettings, ToolbarButtonsClickListener, OnKeyDownListener, OnScanResultListener, OnBackPresserListener {

    private var processingRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null
    private var processedRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null
    private var searchRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null

    override fun getLayoutId(): Int = R.layout.fragment_goods_list_wl

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix("14")

    override fun getViewModel(): GoodsListWlViewModel {
        provideViewModel(GoodsListWlViewModel::class.java).let {
            CoreInjectHelper.getComponent(WorkListComponent::class.java)!!.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.goods_list)

        connectLiveData(vm.taskName, topToolbarUiModel.title)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.delete)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.filter)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.save)

        connectLiveData(vm.deleteButtonVisibility, getBottomToolBarUIModel()!!.uiModelButton3.visibility)
        connectLiveData(vm.deleteButtonEnabled, getBottomToolBarUIModel()!!.uiModelButton3.enabled)
        connectLiveData(vm.filterButtonVisibility, getBottomToolBarUIModel()!!.uiModelButton3.visibility)
        connectLiveData(vm.filterButtonEnabled, getBottomToolBarUIModel()!!.uiModelButton3.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_3 -> vm.onClickDelete()
            R.id.b_4 -> vm.onClickFilter()
            R.id.b_5 -> vm.onClickSave()
        }
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        val correctedPosition = vm.getCorrectedPagePosition(position)

        if (correctedPosition == 0) {
            DataBindingUtil.inflate<LayoutWlGoodsListProcessingBinding>(LayoutInflater.from(container.context),
                    R.layout.layout_wl_goods_list_processing,
                    container,
                    false).let { layoutBinding ->

                layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                        layoutId = R.layout.item_wl_processing_list,
                        itemId = BR.good,
                        realisation = object : DataBindingAdapter<ItemWlProcessingListBinding> {
                            override fun onCreate(binding: ItemWlProcessingListBinding) {
                            }

                            override fun onBind(binding: ItemWlProcessingListBinding, position: Int) {
                                processingRecyclerViewKeyHandler?.let {
                                    binding.root.isSelected = it.isSelected(position)
                                }
                            }
                        },
                        onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                            processingRecyclerViewKeyHandler?.let {
                                if (it.isSelected(position)) {
                                    vm.onClickItemPosition(position)
                                } else {
                                    it.selectPosition(position)
                                }
                            }
                        })

                layoutBinding.vm = vm
                layoutBinding.lifecycleOwner = viewLifecycleOwner
                processingRecyclerViewKeyHandler = RecyclerViewKeyHandler(
                        rv = layoutBinding.rv,
                        items = vm.processingGoods,
                        lifecycleOwner = layoutBinding.lifecycleOwner!!,
                        initPosInfo = processingRecyclerViewKeyHandler?.posInfo?.value
                )

                return layoutBinding.root
            }
        }

        if (correctedPosition == 1) {
            DataBindingUtil.inflate<LayoutWlGoodsListProcessedBinding>(LayoutInflater.from(container.context),
                    R.layout.layout_wl_goods_list_processed,
                    container,
                    false).let { layoutBinding ->

                val onClickSelectionListener = View.OnClickListener {
                    (it!!.tag as Int).let { position ->
                        vm.processedSelectionsHelper.revert(position = position)
                        layoutBinding.rv.adapter?.notifyItemChanged(position)
                    }
                }

                layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                        layoutId = R.layout.item_wl_processed_list,
                        itemId = BR.good,
                        realisation = object : DataBindingAdapter<ItemWlProcessedListBinding> {
                            override fun onCreate(binding: ItemWlProcessedListBinding) {
                            }

                            override fun onBind(binding: ItemWlProcessedListBinding, position: Int) {
                                binding.tvItemNumber.tag = position
                                binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                                binding.selectedForDelete = vm.processedSelectionsHelper.isSelected(position)
                                processedRecyclerViewKeyHandler?.let {
                                    binding.root.isSelected = it.isSelected(position)
                                }
                            }
                        },
                        onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                            processedRecyclerViewKeyHandler?.let {
                                if (it.isSelected(position)) {
                                    vm.onClickItemPosition(position)
                                } else {
                                    it.selectPosition(position)
                                }
                            }
                        })

                layoutBinding.vm = vm
                layoutBinding.lifecycleOwner = viewLifecycleOwner
                processedRecyclerViewKeyHandler = RecyclerViewKeyHandler(
                        rv = layoutBinding.rv,
                        items = vm.processedGoods,
                        lifecycleOwner = layoutBinding.lifecycleOwner!!,
                        initPosInfo = processedRecyclerViewKeyHandler?.posInfo?.value
                )

                return layoutBinding.root
            }
        }

        DataBindingUtil.inflate<LayoutWlGoodsListSearchBinding>(LayoutInflater.from(container.context),
                R.layout.layout_wl_goods_list_search,
                container,
                false).let { layoutBinding ->

            val onClickSelectionListener = View.OnClickListener {
                (it!!.tag as Int).let { position ->
                    vm.searchSelectionsHelper.revert(position = position)
                    layoutBinding.rv.adapter?.notifyItemChanged(position)
                }
            }

            layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                    layoutId = R.layout.item_wl_search_list,
                    itemId = BR.good,
                    realisation = object : DataBindingAdapter<ItemWlSearchListBinding> {
                        override fun onCreate(binding: ItemWlSearchListBinding) {
                        }

                        override fun onBind(binding: ItemWlSearchListBinding, position: Int) {
                            binding.tvItemNumber.tag = position
                            binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                            binding.selectedForDelete = vm.searchSelectionsHelper.isSelected(position)
                            searchRecyclerViewKeyHandler?.let {
                                binding.root.isSelected = it.isSelected(position)
                            }
                        }
                    },
                    onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                        searchRecyclerViewKeyHandler?.let {
                            if (it.isSelected(position)) {
                                vm.onClickItemPosition(position)
                            } else {
                                it.selectPosition(position)
                            }
                        }
                    })

            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner
            searchRecyclerViewKeyHandler = RecyclerViewKeyHandler(
                    rv = layoutBinding.rv,
                    items = vm.searchGoods,
                    lifecycleOwner = layoutBinding.lifecycleOwner!!,
                    initPosInfo = searchRecyclerViewKeyHandler?.posInfo?.value
            )

            return layoutBinding.root
        }
    }

    override fun getTextTitle(position: Int): String {
        return when (vm.getCorrectedPagePosition(position)) {
            0 -> getString(R.string.processing)
            1 -> getString(R.string.processed)
            2 -> getString(R.string.search)
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

    override fun onKeyDown(keyCode: KeyCode): Boolean {
        when (vm.correctedSelectedPage.value) {
            1 -> processedRecyclerViewKeyHandler
            2 -> searchRecyclerViewKeyHandler
            else -> null
        }?.let {
            if (!it.onKeyDown(keyCode)) {
                keyCode.digit?.let { digit ->
                    vm.onDigitPressed(digit)
                    return true
                }
                return false
            }
            return true
        }
        return false
    }

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }

    override fun onBackPressed(): Boolean {
        vm.onClickBack()
        return true
    }

}
