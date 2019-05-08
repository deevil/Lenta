package com.lenta.bp10.features.goods_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.lenta.bp10.BR
import com.lenta.bp10.R
import com.lenta.bp10.databinding.FragmentGoodsListBinding
import com.lenta.bp10.databinding.LayoutGoodsCountedBinding
import com.lenta.bp10.databinding.LayoutGoodsFilterBinding
import com.lenta.bp10.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.provideViewModel

class GoodsListFragment :
        CoreFragment<FragmentGoodsListBinding, GoodsListViewModel>(),
        ViewPagerSettings,
        PageSelectionListener {

    override fun getLayoutId(): Int = R.layout.fragment_goods_list

    override fun getPageNumber() = "10/06"

    override fun getViewModel(): GoodsListViewModel {
        provideViewModel(GoodsListViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.list_of_goods)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.delete, enabled = false)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.print, enabled = false)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.save, enabled = false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.let {
            it.viewPagerSettings = this

        }

    }

    override fun onResume() {
        super.onResume()
        vm.onResume()
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        if (position ==0) {
            DataBindingUtil
                    .inflate<LayoutGoodsCountedBinding>(LayoutInflater.from(container.context),
                            R.layout.layout_goods_counted,
                            container,
                            false).let {
                        it.lifecycleOwner = viewLifecycleOwner
                        it.rvConfig = DataBindingRecyclerViewConfig(layoutId = R.layout.item_tile_goods, itemId = BR.vm)
                        it.vm = vm
                        return it.root
                    }
        }

        DataBindingUtil
                .inflate<LayoutGoodsFilterBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_goods_filter,
                        container,
                        false).let {
                    it.lifecycleOwner = viewLifecycleOwner
                    it.rvConfig = DataBindingRecyclerViewConfig(layoutId = R.layout.item_tile_goods, itemId = BR.vm)
                    it.vm = vm
                    return it.root
                }


    }

    override fun getTextTitle(position: Int): String = getString(if (position == 0) R.string.counted else R.string.filter)

    override fun onPageSelected(position: Int) {
        Logg.d { "onPageSelected $position" }
    }

    override fun countTab(): Int = 2


}
