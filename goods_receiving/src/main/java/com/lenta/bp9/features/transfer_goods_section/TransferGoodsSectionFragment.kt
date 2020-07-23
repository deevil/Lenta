package com.lenta.bp9.features.transfer_goods_section

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.lenta.bp9.BR
import com.lenta.bp9.R
import com.lenta.bp9.databinding.FragmentTransferGoodsSectionBinding
import com.lenta.bp9.databinding.ItemTileTransferGoodsSectionBinding
import com.lenta.bp9.databinding.LayoutTransferGoodsSectionBinding
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.databinding.DataBindingAdapter
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.provideViewModel

class TransferGoodsSectionFragment : CoreFragment<FragmentTransferGoodsSectionBinding, TransferGoodsSectionViewModel>(),
        ViewPagerSettings,
        PageSelectionListener,
        ToolbarButtonsClickListener,
        OnBackPresserListener {

    override fun getLayoutId(): Int = R.layout.fragment_transfer_goods_section

    override fun getPageNumber(): String = "09/36"

    override fun getViewModel(): TransferGoodsSectionViewModel {
        provideViewModel(TransferGoodsSectionViewModel::class.java).let {vm ->
            getAppComponent()?.inject(vm)
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
        topToolbarUiModel.description.value = getString(R.string.transfer_goods_section)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.save)
        connectLiveData(vm.enabledBtnSave, bottomToolbarUiModel.uiModelButton5.enabled)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_5 -> vm.onClickSave()
        }
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        DataBindingUtil
                .inflate<LayoutTransferGoodsSectionBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_transfer_goods_section,
                        container,
                        false).let { layoutBinding ->

                    val onClickConditionTitle = View.OnClickListener {
                        (it!!.tag as Int).let { position ->
                            vm.onClickItemPosition(position)
                        }
                    }

                    layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                            layoutId = R.layout.item_tile_transfer_goods_section,
                            itemId = BR.item,
                            realisation = object : DataBindingAdapter<ItemTileTransferGoodsSectionBinding> {
                                override fun onCreate(binding: ItemTileTransferGoodsSectionBinding) {
                                }

                                override fun onBind(binding: ItemTileTransferGoodsSectionBinding, position: Int) {
                                    binding.tvCondition.tag = position
                                    binding.tvCondition.setOnClickListener(onClickConditionTitle)
                                }

                            }
                    )

                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner
                    return layoutBinding.root
                }
    }

    override fun getTextTitle(position: Int): String = getString(if (position == 0) R.string.transmitted else R.string.transferred)

    override fun countTab(): Int {
        return 2
    }

    override fun onPageSelected(position: Int) {
        vm.onPageSelected(position)
    }

    override fun onBackPressed(): Boolean {
        vm.onBackPressed()
        return false
    }

    override fun onResume() {
        super.onResume()
        vm.onResume()
    }

}
