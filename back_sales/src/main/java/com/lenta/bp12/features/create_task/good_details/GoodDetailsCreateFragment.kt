package com.lenta.bp12.features.create_task.good_details

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
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class GoodDetailsCreateFragment : CoreFragment<FragmentGoodDetailsCreateBinding, GoodDetailsCreateViewModel>(),
        ViewPagerSettings, ToolbarButtonsClickListener {

    override fun getLayoutId(): Int = R.layout.fragment_good_details_create

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix(SCREEN_NUMBER)

    override fun getViewModel(): GoodDetailsCreateViewModel {
        provideViewModel(GoodDetailsCreateViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.good_details)

        connectLiveData(vm.title, topToolbarUiModel.title)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.delete, enabled = false)

        connectLiveData(vm.deleteEnabled, bottomToolbarUiModel.uiModelButton3.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_3 -> vm.onClickDelete()
        }
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return when (position) {
            TAB_BASKETS -> initGoodDetailsBaskets(container)
            TAB_CATEGORIES -> initGoodDetailsCategories(container)
            else -> View(context)
        }
    }

    private fun initGoodDetailsBaskets(container: ViewGroup): View {
        DataBindingUtil.inflate<LayoutGoodDetailsBasketCreateBinding>(LayoutInflater.from(container.context),
                R.layout.layout_good_details_basket_create,
                container,
                false).let { layoutBinding ->

            val onClickSelectionListener = View.OnClickListener {
                (it.tag as? Int)?.let { position ->
                    vm.basketSelectionsHelper.revert(position = position)
                    layoutBinding.rv.adapter?.notifyItemChanged(position)
                }
            }

            layoutBinding.rvConfig = initRecycleAdapterDataBinding(
                    layoutId = R.layout.item_basket_details,
                    itemId = BR.item,
                    onItemBind = { binding: ItemBasketDetailsBinding, position: Int ->
                        binding.tvItemNumber.tag = position
                        binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                        binding.selectedForDelete = vm.basketSelectionsHelper.isSelected(position)
                    },
                    keyHandlerId = TAB_BASKETS,
                    recyclerView = layoutBinding.rv,
                    items = vm.baskets
            )

            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner

            return layoutBinding.root
        }
    }

    private fun initGoodDetailsCategories(container: ViewGroup): View {
        DataBindingUtil.inflate<LayoutGoodDetailsCategoryCreateBinding>(LayoutInflater.from(container.context),
                R.layout.layout_good_details_category_create,
                container,
                false).let { layoutBinding ->

            val onClickSelectionListener = View.OnClickListener {
                (it.tag as? Int)?.let { position ->
                    vm.categorySelectionsHelper.revert(position = position)
                    layoutBinding.rv.adapter?.notifyItemChanged(position)
                }
            }

            layoutBinding.rvConfig = initRecycleAdapterDataBinding(
                    layoutId = R.layout.item_category,
                    itemId = BR.item,
                    onItemBind = { binding: ItemCategoryBinding, position: Int ->
                        binding.tvItemNumber.tag = position
                        binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                        binding.selectedForDelete = vm.categorySelectionsHelper.isSelected(position)
                    },
                    keyHandlerId = TAB_CATEGORIES,
                    recyclerView = layoutBinding.rv,
                    items = vm.categories
            )

            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner

            return layoutBinding.root
        }
    }

    override fun getTextTitle(position: Int): String {
        return when (position) {
            TAB_BASKETS -> getString(R.string.baskets)
            TAB_CATEGORIES -> getString(R.string.categories)
            else -> throw IllegalArgumentException("Wrong pager position!")
        }
    }

    override fun countTab(): Int {
        return vm.countTab()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }

    companion object {
        const val SCREEN_NUMBER = "96"

        private const val TAB_BASKETS = 0
        private const val TAB_CATEGORIES = 1
    }

}
