package com.lenta.bp14.features.not_exposed.goods_list

import com.lenta.bp14.R
import com.lenta.bp14.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel
import android.os.Bundle
import android.view.LayoutInflater
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import android.view.ViewGroup
import android.view.View
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.lenta.bp14.BR
import com.lenta.bp14.data.GoodsListTab
import com.lenta.bp14.databinding.*
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.utilities.databinding.DataBindingAdapter
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.databinding.RecyclerViewKeyHandler
import com.lenta.shared.utilities.extentions.connectLiveData

class GoodsListNeFragment : CoreFragment<FragmentGoodsListNeBinding, GoodsListNeViewModel>(),
        ViewPagerSettings, ToolbarButtonsClickListener {

    private var processingRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null
    private var processedRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null
    private var searchRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null

    override fun getLayoutId(): Int = R.layout.fragment_goods_list_ne

    override fun getPageNumber(): String {
        return "14/74"
    }

    override fun getViewModel(): GoodsListNeViewModel {
        provideViewModel(GoodsListNeViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.list_of_goods)

        vm.taskName.observe(this, Observer<String> { name ->
            topToolbarUiModel.title.value = name
        })
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.save)

        viewLifecycleOwner.apply {
            vm.selectedPage.observe(this, Observer {
                if (it == GoodsListTab.SEARCH.position) {
                    bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.filter)
                } else {
                    bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.delete)
                }
            })
        }

        connectLiveData(vm.deleteButtonEnabled, getBottomToolBarUIModel()!!.uiModelButton3.enabled)
        connectLiveData(vm.saveButtonEnabled, getBottomToolBarUIModel()!!.uiModelButton5.enabled)
        connectLiveData(vm.thirdButtonVisibility, getBottomToolBarUIModel()!!.uiModelButton3.visibility)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_3 -> {
                if (vm.selectedPage.value == GoodsListTab.SEARCH.position) {
                    vm.onClickFilter()
                } else vm.onClickDelete()
            }
            R.id.b_5 -> vm.onClickSave()
        }
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        if (position == 0) {
            DataBindingUtil.inflate<LayoutNeGoodsListProcessingBinding>(LayoutInflater.from(container.context),
                    R.layout.layout_ne_goods_list_processing,
                    container,
                    false).let { layoutBinding ->

                layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                        layoutId = R.layout.item_good,
                        itemId = BR.good,
                        realisation = object : DataBindingAdapter<ItemGoodBinding> {
                            override fun onCreate(binding: ItemGoodBinding) {
                            }

                            override fun onBind(binding: ItemGoodBinding, position: Int) {
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

        if (position == 1) {
            DataBindingUtil.inflate<LayoutNeGoodsListProcessedBinding>(LayoutInflater.from(container.context),
                    R.layout.layout_ne_goods_list_processed,
                    container,
                    false).let { layoutBinding ->

                val onClickSelectionListener = View.OnClickListener {
                    (it!!.tag as Int).let { position ->
                        vm.processedSelectionsHelper.revert(position = position)
                        layoutBinding.rv.adapter?.notifyItemChanged(position)
                    }
                }

                layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                        layoutId = R.layout.item_good_quantity_selectable,
                        itemId = BR.good,
                        realisation = object : DataBindingAdapter<ItemGoodQuantitySelectableBinding> {
                            override fun onCreate(binding: ItemGoodQuantitySelectableBinding) {
                            }

                            override fun onBind(binding: ItemGoodQuantitySelectableBinding, position: Int) {
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

        DataBindingUtil.inflate<LayoutNeGoodsListSearchBinding>(LayoutInflater.from(container.context),
                R.layout.layout_ne_goods_list_search,
                container,
                false).let { layoutBinding ->

            layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                    layoutId = R.layout.item_good,
                    itemId = BR.good,
                    realisation = object : DataBindingAdapter<ItemGoodBinding> {
                        override fun onCreate(binding: ItemGoodBinding) {
                        }

                        override fun onBind(binding: ItemGoodBinding, position: Int) {
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
                    items = vm.processingGoods,
                    lifecycleOwner = layoutBinding.lifecycleOwner!!,
                    initPosInfo = searchRecyclerViewKeyHandler?.posInfo?.value
            )

            return layoutBinding.root
        }
    }

    override fun getTextTitle(position: Int): String {
        return getString(
                when (getRealTabPosition(position)) {
                    0 -> R.string.not_processed
                    1 -> R.string.processed
                    else -> R.string.search
                }
        )
    }

    private fun getRealTabPosition(position: Int): Int {
        return if (countTab() < 3) position + 1 else position
    }

    override fun countTab(): Int {
        return 3
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }

}
