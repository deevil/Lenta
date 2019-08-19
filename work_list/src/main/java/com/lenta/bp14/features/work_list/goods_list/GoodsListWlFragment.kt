package com.lenta.bp14.features.work_list.goods_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.lenta.bp14.BR
import com.lenta.bp14.R
import com.lenta.bp14.databinding.FragmentGoodsListWlBinding
import com.lenta.bp14.databinding.ItemGoodBinding
import com.lenta.bp14.databinding.ItemGoodSimpleBinding
import com.lenta.bp14.databinding.LayoutGoodsListWlBinding
import com.lenta.bp14.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class GoodsListWlFragment : CoreFragment<FragmentGoodsListWlBinding, GoodsListWlViewModel>(), ViewPagerSettings {

    override fun getLayoutId(): Int = R.layout.fragment_goods_list_wl

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix("14")

    override fun getViewModel(): GoodsListWlViewModel {
        provideViewModel(GoodsListWlViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.product_information)

        vm.workListName.observe(this, Observer<String> { name ->
            topToolbarUiModel.title.value = name
        })
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.delete)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.save)
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
            DataBindingUtil.inflate<LayoutGoodsListWlBinding>(LayoutInflater.from(container.context),
                    R.layout.layout_goods_list_wl,
                    container,
                    false).let { layoutBinding ->

                if (position == 0) {
                    layoutBinding.rvConfig = DataBindingRecyclerViewConfig<ItemGoodBinding>(
                            layoutId = R.layout.item_good,
                            itemId = BR.good)
                } else {
                    layoutBinding.rvConfig = DataBindingRecyclerViewConfig<ItemGoodSimpleBinding>(
                            layoutId = R.layout.item_good_simple,
                            itemId = BR.good)
                }

                layoutBinding.vm = vm
                layoutBinding.lifecycleOwner = viewLifecycleOwner
                return layoutBinding.root
            }
    }

    override fun getTextTitle(position: Int): String {
        return when (position) {
            0 -> getString(R.string.processed)
            1 -> getString(R.string.search)
            else -> {
                Logg.d { "Wrong pager position!" }
                "Error"
            }
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
