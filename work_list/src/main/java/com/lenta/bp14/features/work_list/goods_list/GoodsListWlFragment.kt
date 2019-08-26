package com.lenta.bp14.features.work_list.goods_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.lenta.bp14.BR
import com.lenta.bp14.R
import com.lenta.bp14.databinding.*
import com.lenta.bp14.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class GoodsListWlFragment : CoreFragment<FragmentGoodsListWlBinding, GoodsListWlViewModel>(),
        ViewPagerSettings, ToolbarButtonsClickListener {

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

        vm.taskName.observe(this, Observer<String> { name ->
            topToolbarUiModel.title.value = name
        })
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.save)

        vm.thirdButtonDecoration.observe(this, Observer<ButtonDecorationInfo> { decoration ->
            bottomToolbarUiModel.uiModelButton3.show(decoration)
        })

        connectLiveData(vm.deleteButtonEnabled, getBottomToolBarUIModel()!!.uiModelButton3.enabled)
        connectLiveData(vm.saveButtonEnabled, getBottomToolBarUIModel()!!.uiModelButton5.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_3 -> {
                if (vm.thirdButtonDecoration.value == ButtonDecorationInfo.delete) {
                    vm.onClickDelete()
                } else {
                    vm.onClickFilter()
                }
            }
            R.id.b_5 -> vm.onClickSave()
        }
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        if (position == 1) {
            vm.thirdButtonDecoration.value = ButtonDecorationInfo.delete

            DataBindingUtil.inflate<LayoutWlGoodsListBinding>(LayoutInflater.from(container.context),
                    R.layout.layout_wl_goods_list,
                    container,
                    false).let { layoutBinding ->

                layoutBinding.rvConfig = DataBindingRecyclerViewConfig<ItemGoodSelectableBinding>(
                        layoutId = R.layout.item_good_selectable,
                        itemId = BR.good)

                layoutBinding.vm = vm
                layoutBinding.lifecycleOwner = viewLifecycleOwner
                return layoutBinding.root
            }
        }

        if (position == 2) {
            vm.thirdButtonDecoration.value = ButtonDecorationInfo.filter

            DataBindingUtil.inflate<LayoutWlGoodsListBinding>(LayoutInflater.from(container.context),
                    R.layout.layout_wl_goods_list,
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

        DataBindingUtil.inflate<LayoutWlGoodsListBinding>(LayoutInflater.from(container.context),
                R.layout.layout_wl_goods_list,
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
