package com.lenta.bp9.features.goods_information.excise_alco_box_acc.excise_alco_box_list

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
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.utilities.databinding.*
import com.lenta.shared.utilities.state.state

class ExciseAlcoBoxListFragment : CoreFragment<FragmentExciseAlcoBoxListBinding, ExciseAlcoBoxListViewModel>(),
        ViewPagerSettings,
        PageSelectionListener {

    companion object {
        fun create(productInfo: TaskProductInfo): ExciseAlcoBoxListFragment {
            ExciseAlcoBoxListFragment().let {
                it.productInfo = productInfo
                return it
            }
        }
    }

    private var productInfo by state<TaskProductInfo?>(null)

    private var notProcessedRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null

    override fun getLayoutId(): Int = R.layout.fragment_excise_alco_box_list

    override fun getPageNumber(): String = "09/42"

    override fun getViewModel(): ExciseAlcoBoxListViewModel {
        provideViewModel(ExciseAlcoBoxListViewModel::class.java).let {vm ->
            getAppComponent()?.inject(vm)
            vm.productInfo.value = productInfo
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = "${vm.productInfo.value?.getMaterialLastSix()} ${vm.productInfo.value?.description}"
        topToolbarUiModel.description.value = getString(R.string.goods_info)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.boxes)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.details)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.add)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply)
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        if (position == 0) {
            DataBindingUtil
                    .inflate<LayoutExciseAlcoBoxListNotProcessedBinding>(LayoutInflater.from(container.context),
                            R.layout.layout_excise_alco_box_list_not_processed,
                            container,
                            false).let { layoutBinding ->

                        layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                                layoutId = R.layout.item_tile_excis_alco_box_list_not_processed,
                                itemId = BR.vm,
                                realisation = object : DataBindingAdapter<ItemTileExcisAlcoBoxListNotProcessedBinding> {
                                    override fun onCreate(binding: ItemTileExcisAlcoBoxListNotProcessedBinding) {
                                    }

                                    override fun onBind(binding: ItemTileExcisAlcoBoxListNotProcessedBinding, position: Int) {
                                        notProcessedRecyclerViewKeyHandler?.let {
                                            binding.root.isSelected = it.isSelected(position)
                                        }
                                    }

                                },
                                onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                                    notProcessedRecyclerViewKeyHandler?.let {
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
                        notProcessedRecyclerViewKeyHandler = RecyclerViewKeyHandler(
                                rv = layoutBinding.rv,
                                items = vm.countNotProcessed,
                                lifecycleOwner = layoutBinding.lifecycleOwner!!,
                                initPosInfo = notProcessedRecyclerViewKeyHandler?.posInfo?.value
                        )
                        return layoutBinding.root
                    }
        }

        DataBindingUtil
                .inflate<LayoutExciseAlcoBoxListProcessedBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_excise_alco_box_list_processed,
                        container,
                        false).let { layoutBinding ->

                    val onClickSelectionListener = View.OnClickListener {
                        (it!!.tag as Int).let { position ->
                            vm.processedSelectionsHelper.revert(position = position)
                            layoutBinding.rv.adapter?.notifyItemChanged(position)
                        }
                    }

                    layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                            layoutId = R.layout.item_tile_excise_alco_box_list_processed,
                            itemId = BR.vm,
                            realisation = object : DataBindingAdapter<ItemTileExciseAlcoBoxListProcessedBinding> {
                                override fun onCreate(binding: ItemTileExciseAlcoBoxListProcessedBinding) {
                                }

                                override fun onBind(binding: ItemTileExciseAlcoBoxListProcessedBinding, position: Int) {
                                    binding.tvCounter.tag = position
                                    binding.tvCounter.setOnClickListener(onClickSelectionListener)
                                    binding.selectedForDelete = vm.processedSelectionsHelper.isSelected(position)
                                }

                            }
                    )

                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner
                    return layoutBinding.root
                }
    }

    override fun getTextTitle(position: Int): String = getString(if (position == 0) R.string.not_processed else R.string.processed)

    override fun countTab(): Int = 2

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }

    override fun onPageSelected(position: Int) {
        vm.onPageSelected(position)
    }

}
