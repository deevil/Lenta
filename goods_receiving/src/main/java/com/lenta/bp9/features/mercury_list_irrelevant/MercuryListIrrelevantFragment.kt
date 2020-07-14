package com.lenta.bp9.features.mercury_list_irrelevant

import android.os.Bundle
import android.view.View
import com.lenta.bp9.BR
import com.lenta.bp9.R
import com.lenta.bp9.databinding.FragmentMercuryListIrrelevantBinding
import com.lenta.bp9.databinding.ItemTileMercuryListIrrelevantBinding
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.databinding.DataBindingAdapter
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

class MercuryListIrrelevantFragment : CoreFragment<FragmentMercuryListIrrelevantBinding, MercuryListIrrelevantViewModel>(), ToolbarButtonsClickListener {

    companion object {
        fun create(netRestNumber: Int): MercuryListIrrelevantFragment {
            MercuryListIrrelevantFragment().let {
                it.netRestNumber = netRestNumber
                return it
            }
        }
    }

    private var netRestNumber by state<Int?>(null)

    override fun getLayoutId(): Int = R.layout.fragment_mercury_list_irrelevant

    override fun getPageNumber(): String = "09/106"

    override fun getViewModel(): MercuryListIrrelevantViewModel {
        provideViewModel(MercuryListIrrelevantViewModel::class.java).let {vm ->
            getAppComponent()?.inject(vm)
            vm.netRestNumber.value = this.netRestNumber
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
        topToolbarUiModel.description.value = getString(R.string.mercury_list_irrelevant)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.untie)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.temporary)
    }

    override fun onResume() {
        super.onResume()
        vm.onResume()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initRvConfig()
    }

    private fun initRvConfig() {
        binding?.let { layoutBinding ->
            layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                    layoutId = R.layout.item_tile_mercury_list_irrelevant,
                    itemId = BR.item,
                    realisation = object : DataBindingAdapter<ItemTileMercuryListIrrelevantBinding> {
                        override fun onCreate(binding: ItemTileMercuryListIrrelevantBinding) {
                        }

                        override fun onBind(binding: ItemTileMercuryListIrrelevantBinding, position: Int) {
                        }

                    }
            )

            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner
        }
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_3 -> vm.onClickUntied()
            R.id.b_5 -> vm.onClickTemporary()
        }
    }

}
