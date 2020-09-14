package com.lenta.bp9.features.transport_marriage.goods_details

import android.os.Bundle
import android.view.View
import com.lenta.bp9.BR
import com.lenta.bp9.R
import com.lenta.bp9.databinding.FragmentTransportMarriageGoodsDetailsBinding
import com.lenta.bp9.databinding.ItemTileGoodsDetailsBinding
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

class TransportMarriageGoodsDetailsFragment : CoreFragment<FragmentTransportMarriageGoodsDetailsBinding, TransportMarriageGoodsDetailsViewModel>() {

    companion object {
        fun create(cargoUnitNumber: String, materialNumber: String, materialName: String): TransportMarriageGoodsDetailsFragment {
            TransportMarriageGoodsDetailsFragment().let {
                it.cargoUnitNumber = cargoUnitNumber
                it.materialNumber = materialNumber
                it.materialName = materialName
                return it
            }
        }
    }

    private var cargoUnitNumber by state<String?>(null)
    private var materialNumber by state<String?>(null)
    private var materialName by state<String?>(null)

    override fun getLayoutId(): Int = R.layout.fragment_transport_marriage_goods_details

    override fun getPageNumber(): String = "09/25"

    override fun getViewModel(): TransportMarriageGoodsDetailsViewModel {
        provideViewModel(TransportMarriageGoodsDetailsViewModel::class.java).let {vm ->
            getAppComponent()?.inject(vm)
            vm.cargoUnitNumber.value = this.cargoUnitNumber
            vm.materialNumber.value = this.materialNumber
            vm.materialName.value = this.materialName
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
        topToolbarUiModel.description.value = getString(R.string.details_quantities_entered)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding?.let { layoutBinding ->
            layoutBinding.rvConfig = initRecycleAdapterDataBinding<ItemTileGoodsDetailsBinding>(
                    layoutId = R.layout.item_tile_goods_details,
                    itemId = BR.item
            )

            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner
        }
    }

}
