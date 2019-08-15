package com.lenta.bp14.features.work_list.details_of_goods

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.lenta.bp14.BR
import com.lenta.bp14.R
import com.lenta.bp14.databinding.FragmentDetailsOfGoodsBinding
import com.lenta.bp14.databinding.ItemTileExpirationBinding
import com.lenta.bp14.databinding.LayoutExpirationDatesListBinding
import com.lenta.bp14.databinding.LayoutTaskListBinding
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

class DetailsOfGoodsFragment : CoreFragment<FragmentDetailsOfGoodsBinding, DetailsOfGoodsViewModel>(),
        ToolbarButtonsClickListener,
        ViewPagerSettings {

    override fun getLayoutId(): Int = R.layout.fragment_details_of_goods

    override fun getPageNumber(): String {
        return "14/13"
    }

    override fun getViewModel(): DetailsOfGoodsViewModel {
        provideViewModel(DetailsOfGoodsViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
        topToolbarUiModel.description.value = getString(R.string.goods_of_details)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.delete)
        connectLiveData(vm.enabledDeleteButton, bottomToolbarUiModel.uiModelButton3.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_3 -> vm.onClickDelete()
        }
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        if (position == 0) {
            DataBindingUtil
                    .inflate<LayoutExpirationDatesListBinding>(LayoutInflater.from(container.context),
                            R.layout.layout_expiration_dates_list,
                            container,
                            false).let { layoutBinding ->

                        layoutBinding.rvConfig = DataBindingRecyclerViewConfig<ItemTileExpirationBinding>(
                                layoutId = R.layout.item_tile_expiration,
                                itemId = BR.vm
                        )

                        layoutBinding.vm = vm
                        layoutBinding.lifecycleOwner = viewLifecycleOwner
                        return layoutBinding.root
                    }
        }

        return View(context)

    }

    override fun getTextTitle(position: Int): String {
        return "position: $position"
    }

    override fun countTab() = 2

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }

}
