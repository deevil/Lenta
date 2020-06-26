package com.lenta.bp12.features.open_task.good_details

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
import com.lenta.shared.utilities.databinding.DataBindingAdapter
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.databinding.RecyclerViewKeyHandler
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class GoodDetailsOpenFragment : CoreFragment<FragmentGoodDetailsOpenBinding, GoodDetailsOpenViewModel>(),
        ViewPagerSettings, ToolbarButtonsClickListener {

    private var recyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null

    override fun getLayoutId(): Int = R.layout.fragment_good_details_open

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix("56")

    override fun getViewModel(): GoodDetailsOpenViewModel {
        provideViewModel(GoodDetailsOpenViewModel::class.java).let {
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

        connectLiveData(vm.deleteEnabled, getBottomToolBarUIModel()!!.uiModelButton3.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_3 -> vm.onClickDelete()
        }
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return when (position) {
            0 -> initGoodDetailsCategories(container)
            else -> View(context)
        }
    }

    private fun initGoodDetailsCategories(container: ViewGroup): View {
        DataBindingUtil.inflate<LayoutGoodDetailsCategoryOpenBinding>(LayoutInflater.from(container.context),
                R.layout.layout_good_details_category_open,
                container,
                false).let { layoutBinding ->

            val onClickSelectionListener = View.OnClickListener {
                (it!!.tag as Int).let { position ->
                    vm.selectionsHelper.revert(position = position)
                    layoutBinding.rv.adapter?.notifyItemChanged(position)
                }
            }

            layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                    layoutId = R.layout.item_category,
                    itemId = BR.item,
                    realisation = object : DataBindingAdapter<ItemCategoryBinding> {
                        override fun onCreate(binding: ItemCategoryBinding) {
                        }

                        override fun onBind(binding: ItemCategoryBinding, position: Int) {
                            binding.tvItemNumber.tag = position
                            binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                            binding.selectedForDelete = vm.selectionsHelper.isSelected(position)
                            recyclerViewKeyHandler?.let {
                                binding.root.isSelected = it.isSelected(position)
                            }
                        }
                    }
            )

            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner
            recyclerViewKeyHandler = RecyclerViewKeyHandler(
                    rv = layoutBinding.rv,
                    items = vm.categories,
                    lifecycleOwner = layoutBinding.lifecycleOwner!!,
                    initPosInfo = recyclerViewKeyHandler?.posInfo?.value
            )

            return layoutBinding.root
        }
    }

    override fun getTextTitle(position: Int): String {
        return when (position) {
            0 -> getString(R.string.categories)
            else -> throw IllegalArgumentException("Wrong pager position!")
        }
    }

    override fun countTab(): Int {
        return 1
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }

}
