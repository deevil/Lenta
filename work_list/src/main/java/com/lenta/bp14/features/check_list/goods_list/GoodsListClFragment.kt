package com.lenta.bp14.features.check_list.goods_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.lenta.bp14.BR
import com.lenta.bp14.R
import com.lenta.bp14.databinding.FragmentGoodsListClBinding
import com.lenta.bp14.databinding.ItemTileGoodsClBinding
import com.lenta.bp14.databinding.LayoutGoodsListClBinding
import com.lenta.bp14.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.provideViewModel

class GoodsListClFragment : CoreFragment<FragmentGoodsListClBinding, GoodsListClViewModel>(), ToolbarButtonsClickListener, ViewPagerSettings {

    override fun getLayoutId(): Int = R.layout.fragment_goods_list_cl

    override fun getPageNumber(): String = "14/21"

    override fun getViewModel(): GoodsListClViewModel {
        provideViewModel(GoodsListClViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
        topToolbarUiModel.description.value = getString(R.string.list_of_goods)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.delete)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.save)
        connectLiveData(vm.deleteButtonEnabled, bottomToolbarUiModel.uiModelButton3.enabled)
        connectLiveData(vm.saveButtonEnabled, bottomToolbarUiModel.uiModelButton5.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_3 -> vm.onClickDelete()
            R.id.b_5 -> vm.onClickSave()
        }
    }


    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        DataBindingUtil
                .inflate<LayoutGoodsListClBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_goods_list_cl,
                        container,
                        false).let { layoutBinding ->

                    layoutBinding.rvConfig = DataBindingRecyclerViewConfig<ItemTileGoodsClBinding>(
                            layoutId = R.layout.item_tile_goods_cl,
                            itemId = BR.vm
                    )

                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner
                    return layoutBinding.root
                }
    }


    override fun getTextTitle(position: Int): String {
        return getString(R.string.goods)
    }

    override fun countTab() = 1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this

    }


}
