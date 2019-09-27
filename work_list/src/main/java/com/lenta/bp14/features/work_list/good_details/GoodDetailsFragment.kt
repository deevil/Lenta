package com.lenta.bp14.features.work_list.good_details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.lenta.bp14.BR
import com.lenta.bp14.R
import com.lenta.bp14.databinding.*
import com.lenta.bp14.platform.extentions.getAppComponent
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

class GoodDetailsFragment : CoreFragment<FragmentGoodDetailsBinding, GoodDetailsViewModel>(),
        ToolbarButtonsClickListener, ViewPagerSettings {

    private var shelfLifeRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null
    private var commentsRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null

    override fun getLayoutId(): Int = R.layout.fragment_good_details

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix("13")

    override fun getViewModel(): GoodDetailsViewModel {
        provideViewModel(GoodDetailsViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.details_of_goods)

        connectLiveData(vm.title, topToolbarUiModel.title)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.delete)

        connectLiveData(vm.deleteButtonEnabled, bottomToolbarUiModel.uiModelButton3.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_3 -> vm.onClickDelete()
        }
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        if (position == 0) {
            DataBindingUtil
                    .inflate<LayoutGoodDetailsShelfLifeBinding>(LayoutInflater.from(container.context),
                            R.layout.layout_good_details_shelf_life,
                            container,
                            false).let { layoutBinding ->

                        val onClickSelectionListener = View.OnClickListener {
                            (it!!.tag as Int).let { position ->
                                vm.shelfLifeSelectionsHelper.revert(position = position)
                                layoutBinding.rv.adapter?.notifyItemChanged(position)
                            }
                        }

                        layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                                layoutId = R.layout.item_wl_shelf_life_quantity_selectable,
                                itemId = BR.shelfLife,
                                realisation = object : DataBindingAdapter<ItemWlShelfLifeQuantitySelectableBinding> {
                                    override fun onCreate(binding: ItemWlShelfLifeQuantitySelectableBinding) {
                                    }

                                    override fun onBind(binding: ItemWlShelfLifeQuantitySelectableBinding, position: Int) {
                                        binding.tvItemNumber.tag = position
                                        binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                                        binding.selectedForDelete = vm.shelfLifeSelectionsHelper.isSelected(position)
                                        shelfLifeRecyclerViewKeyHandler?.let {
                                            binding.root.isSelected = it.isSelected(position)
                                        }
                                    }
                                }
                        )

                        layoutBinding.vm = vm
                        layoutBinding.lifecycleOwner = viewLifecycleOwner
                        return layoutBinding.root
                    }
        }

        DataBindingUtil
                .inflate<LayoutGoodDetailsCommentsBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_good_details_comments,
                        container,
                        false).let { layoutBinding ->

                    val onClickSelectionListener = View.OnClickListener {
                        (it!!.tag as Int).let { position ->
                            vm.commentSelectionsHelper.revert(position = position)
                            layoutBinding.rv.adapter?.notifyItemChanged(position)
                        }
                    }

                    layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                            layoutId = R.layout.item_wl_comment_quantity_selectable,
                            itemId = BR.comment,
                            realisation = object : DataBindingAdapter<ItemWlCommentQuantitySelectableBinding> {
                                override fun onCreate(binding: ItemWlCommentQuantitySelectableBinding) {
                                }

                                override fun onBind(binding: ItemWlCommentQuantitySelectableBinding, position: Int) {
                                    binding.tvItemNumber.tag = position
                                    binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                                    binding.selectedForDelete = vm.commentSelectionsHelper.isSelected(position)
                                    commentsRecyclerViewKeyHandler?.let {
                                        binding.root.isSelected = it.isSelected(position)
                                    }
                                }
                            }
                    )

                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner
                    return layoutBinding.root
                }
    }

    override fun getTextTitle(position: Int): String {
        return when (position) {
            0 -> getString(R.string.expiration_dates)
            1 -> getString(R.string.comments)
            else -> throw IllegalArgumentException("Wrong pager position!")
        }
    }

    override fun countTab() = 2

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }

}
