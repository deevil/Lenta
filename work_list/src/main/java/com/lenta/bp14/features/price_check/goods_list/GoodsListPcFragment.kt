package com.lenta.bp14.features.price_check.goods_list

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
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.lenta.bp14.BR
import com.lenta.bp14.databinding.*
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix

class GoodsListPcFragment : CoreFragment<FragmentGoodsListPcBinding, GoodsListPcViewModel>(), ViewPagerSettings {

    override fun getLayoutId(): Int = R.layout.fragment_goods_list_pc

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix("41")

    override fun getViewModel(): GoodsListPcViewModel {
        provideViewModel(GoodsListPcViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.goods_list)

        vm.taskName.observe(this, Observer<String> { name ->
            topToolbarUiModel.title.value = name
        })
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.delete)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.print)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.save)
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        if (position == 0) {
            DataBindingUtil.inflate<LayoutPcGoodsListProcessingBinding>(LayoutInflater.from(container.context),
                    R.layout.layout_pc_goods_list_processing,
                    container,
                    false).let { layoutBinding ->

                layoutBinding.rvConfig = DataBindingRecyclerViewConfig<ItemGoodBinding>(
                        layoutId = R.layout.item_good,
                        itemId = BR.good)

                layoutBinding.vm = vm
                layoutBinding.lifecycleOwner = viewLifecycleOwner
                return layoutBinding.root
            }
        }

        if (position == 2) {
            DataBindingUtil.inflate<LayoutPcGoodsListSearchBinding>(LayoutInflater.from(container.context),
                    R.layout.layout_pc_goods_list_search,
                    container,
                    false).let { layoutBinding ->

                layoutBinding.rvConfig = DataBindingRecyclerViewConfig<ItemGoodCheckSelectableBinding>(
                        layoutId = R.layout.item_good_check_selectable,
                        itemId = BR.good)

                layoutBinding.vm = vm
                layoutBinding.lifecycleOwner = viewLifecycleOwner
                return layoutBinding.root
            }
        }

        DataBindingUtil.inflate<LayoutPcGoodsListProcessedBinding>(LayoutInflater.from(container.context),
                R.layout.layout_pc_goods_list_processed,
                container,
                false).let { layoutBinding ->

            layoutBinding.rvConfig = DataBindingRecyclerViewConfig<ItemGoodCheckSelectableBinding>(
                    layoutId = R.layout.item_good_check_selectable,
                    itemId = BR.good)

            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner
            return layoutBinding.root
        }
    }

    override fun getTextTitle(position: Int): String {
        return when (position) {
            0 -> getString(R.string.processing)
            1 -> getString(R.string.processed)
            2 -> getString(R.string.search)
            else -> {
                Logg.d { "Wrong pager position!" }
                "Error"
            }
        }
    }

    override fun countTab(): Int {
        return 3
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }

}
