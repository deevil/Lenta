package com.lenta.bp9.features.goods_details.marking_goods_details

import com.lenta.bp9.R
import com.lenta.bp9.platform.extentions.getAppComponent
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
import com.lenta.bp9.BR
import com.lenta.bp9.databinding.*
import com.lenta.bp9.features.goods_details.GoodsDetailsFragment
import com.lenta.bp9.features.goods_list.GoodsListViewPages
import com.lenta.bp9.model.task.TaskMarkingGoodsProperties
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.model.task.TaskType
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.DataBindingAdapter
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.databinding.RecyclerViewKeyHandler
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.state.state

class MarkingGoodsDetailsFragment : CoreFragment<FragmentMarkingGoodsDetailsBinding, MarkingGoodsDetailsViewModel>(),
        ViewPagerSettings,
        ToolbarButtonsClickListener {

    private var productInfo by state<TaskProductInfo?>(null)

    override fun getLayoutId(): Int = R.layout.fragment_marking_goods_details

    override fun getPageNumber(): String = PAGE_NUMBER

    override fun getViewModel(): MarkingGoodsDetailsViewModel {
        provideViewModel(MarkingGoodsDetailsViewModel::class.java).let {vm ->
            getAppComponent()?.inject(vm)
            vm.productInfo.value = this.productInfo
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
        topToolbarUiModel.description.value = getString(R.string.details_quantities_entered)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.delete, visible = vm.visibilityDelButton.value ?: false, enabled = vm.enabledDelBtn.value ?: false)

        connectLiveData(vm.visibilityDelButton, bottomToolbarUiModel.uiModelButton3.visibility)
        connectLiveData(vm.enabledDelBtn, bottomToolbarUiModel.uiModelButton3.enabled)
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return if (vm.markingGoodsProperties.value.isNullOrEmpty()) {
            getPageCategories(container)
        } else {
            when(position) {
                PAGE_PROPERTIES -> getPageProperties(container)
                PAGE_CATEGORIES -> getPageCategories(container)
                else -> View(context)
            }
        }
    }

    private fun getPageProperties(container: ViewGroup) : View {
        DataBindingUtil
                .inflate<LayoutMarkingGoodsDetailsPropertiesBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_marking_goods_details_properties,
                        container,
                        false).let { layoutBinding ->

                    layoutBinding.rvConfig = initRecycleAdapterDataBinding(
                            layoutId = R.layout.item_tile_marking_goods_details_properties,
                            itemId = BR.item,
                            onAdapterItemBind = { binding: ItemTileMarkingGoodsDetailsPropertiesBinding, position: Int ->
                                onAdapterBindHandler(binding, position)
                            }
                    )

                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner
                    return layoutBinding.root
                }
    }

    private fun getPageCategories(container: ViewGroup) : View {
        DataBindingUtil
                .inflate<LayoutMarkingGoodsDetailsCategoriesBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_marking_goods_details_categories,
                        container,
                        false).let { layoutBinding ->

                    val onClickSelectionListener = View.OnClickListener {
                        (it!!.tag as Int).let { position ->
                            vm.categoriesSelectionsHelper.revert(position = position)
                            layoutBinding.rv.adapter?.notifyItemChanged(position)
                        }
                    }

                    layoutBinding.rvConfig = initRecycleAdapterDataBinding(
                            layoutId = R.layout.item_tile_goods_details_del,
                            itemId = BR.item,
                            onAdapterItemBind = { binding: ItemTileGoodsDetailsDelBinding, position: Int ->
                                binding.tvItemNumber.tag = position
                                binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                                binding.selectedForDelete = vm.categoriesSelectionsHelper.isSelected(position)
                                onAdapterBindHandler(binding, position)
                            }
                    )

                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner
                    return layoutBinding.root
                }
    }

    override fun getTextTitle(position: Int): String {
        return if (vm.markingGoodsProperties.value.isNullOrEmpty()) {
            getString(R.string.categories)
        } else {
            getString(if (position == PAGE_PROPERTIES) R.string.properties else R.string.categories)
        }
    }

    override fun countTab(): Int {
        return if (vm.markingGoodsProperties.value.isNullOrEmpty()) {
            1
        } else {
            2
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_3 -> vm.onClickDelete()
        }
    }

    companion object {
        private const val PAGE_NUMBER = "09/25"
        private const val PAGE_PROPERTIES = 0
        private const val PAGE_CATEGORIES = 1
        fun newInstance(productInfo: TaskProductInfo): MarkingGoodsDetailsFragment {
            MarkingGoodsDetailsFragment().let {
                it.productInfo = productInfo
                return it
            }
        }
    }

}
