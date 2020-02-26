package com.lenta.bp12.features.good_details

import com.lenta.bp12.R
import com.lenta.bp12.platform.extention.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel
import android.os.Bundle
import android.view.LayoutInflater
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import android.view.ViewGroup
import android.view.View
import androidx.databinding.DataBindingUtil
import com.lenta.bp12.BR
import com.lenta.bp12.databinding.*
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.utilities.databinding.DataBindingAdapter
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.databinding.RecyclerViewKeyHandler
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix

class GoodDetailsFragment : CoreFragment<FragmentGoodDetailsBinding, GoodDetailsViewModel>(), ViewPagerSettings,
        ToolbarButtonsClickListener {

    private var basketRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null
    private var categoryRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null

    override fun getLayoutId(): Int = R.layout.fragment_good_details

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix("96")

    override fun getViewModel(): GoodDetailsViewModel {
        provideViewModel(GoodDetailsViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.title
        topToolbarUiModel.description.value = getString(R.string.good_details)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.delete, enabled = false)

        connectLiveData(vm.deleteEnabled, getBottomToolBarUIModel()!!.uiModelButton3.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_3 -> vm.onClickDelete()
        }
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return when (position) {
            0 -> initGoodDetailsBaskets(container)
            1 -> initGoodDetailsCategories(container)
            else -> View(context)
        }
    }

    private fun initGoodDetailsBaskets(container: ViewGroup): View {
        DataBindingUtil.inflate<LayoutGoodDetailsBasketsBinding>(LayoutInflater.from(container.context),
                R.layout.layout_good_details_baskets,
                container,
                false).let { layoutBinding ->

            val onClickSelectionListener = View.OnClickListener {
                (it!!.tag as Int).let { position ->
                    vm.basketSelectionsHelper.revert(position = position)
                    layoutBinding.rv.adapter?.notifyItemChanged(position)
                }
            }

            layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                    layoutId = R.layout.item_good_details_basket,
                    itemId = BR.item,
                    realisation = object : DataBindingAdapter<ItemGoodDetailsBasketBinding> {
                        override fun onCreate(binding: ItemGoodDetailsBasketBinding) {
                        }

                        override fun onBind(binding: ItemGoodDetailsBasketBinding, position: Int) {
                            binding.tvItemNumber.tag = position
                            binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                            binding.selectedForDelete = vm.basketSelectionsHelper.isSelected(position)
                            basketRecyclerViewKeyHandler?.let {
                                binding.root.isSelected = it.isSelected(position)
                            }
                        }
                    }
            )

            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner
            basketRecyclerViewKeyHandler = RecyclerViewKeyHandler(
                    rv = layoutBinding.rv,
                    items = vm.baskets,
                    lifecycleOwner = layoutBinding.lifecycleOwner!!,
                    initPosInfo = basketRecyclerViewKeyHandler?.posInfo?.value
            )

            return layoutBinding.root
        }
    }

    private fun initGoodDetailsCategories(container: ViewGroup): View {
        DataBindingUtil.inflate<LayoutGoodDetailsCategoriesBinding>(LayoutInflater.from(container.context),
                R.layout.layout_good_details_categories,
                container,
                false).let { layoutBinding ->

            val onClickSelectionListener = View.OnClickListener {
                (it!!.tag as Int).let { position ->
                    vm.categorySelectionsHelper.revert(position = position)
                    layoutBinding.rv.adapter?.notifyItemChanged(position)
                }
            }

            layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                    layoutId = R.layout.item_good_details_category,
                    itemId = BR.item,
                    realisation = object : DataBindingAdapter<ItemGoodDetailsCategoryBinding> {
                        override fun onCreate(binding: ItemGoodDetailsCategoryBinding) {
                        }

                        override fun onBind(binding: ItemGoodDetailsCategoryBinding, position: Int) {
                            binding.tvItemNumber.tag = position
                            binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                            binding.selectedForDelete = vm.categorySelectionsHelper.isSelected(position)
                            categoryRecyclerViewKeyHandler?.let {
                                binding.root.isSelected = it.isSelected(position)
                            }
                        }
                    }
            )

            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner
            categoryRecyclerViewKeyHandler = RecyclerViewKeyHandler(
                    rv = layoutBinding.rv,
                    items = vm.categories,
                    lifecycleOwner = layoutBinding.lifecycleOwner!!,
                    initPosInfo = categoryRecyclerViewKeyHandler?.posInfo?.value
            )

            return layoutBinding.root
        }
    }

    override fun getTextTitle(position: Int): String {
        return when (position) {
            0 -> getString(R.string.baskets)
            1 -> getString(R.string.categories)
            else -> throw IllegalArgumentException("Wrong pager position!")
        }
    }

    override fun countTab(): Int {
        return 2
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }


}
