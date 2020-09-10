package com.lenta.bp9.features.revise

import android.os.Bundle
import android.view.View
import com.lenta.bp9.BR
import com.lenta.bp9.R
import com.lenta.bp9.databinding.FragmentAlcoholBatchSelectBinding
import com.lenta.bp9.databinding.ItemTileAlcoholBatchBinding
import com.lenta.bp9.model.task.revise.ProductDocumentType
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

class AlcoholBatchSelectFragment : CoreFragment<FragmentAlcoholBatchSelectBinding, AlcoholBatchSelectViewModel>() {

    private var matnr by state("")
    private var type by state(ProductDocumentType.None)

    override fun getLayoutId(): Int = R.layout.fragment_alcohol_batch_select

    override fun getPageNumber() = "09/11"

    override fun getViewModel(): AlcoholBatchSelectViewModel {
        provideViewModel(AlcoholBatchSelectViewModel::class.java).let {
            getAppComponent()?.inject(it)
            it.matnr = matnr
            it.type = type
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.taskCaption
        topToolbarUiModel.description.value = getString(R.string.alco_batch_select)    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRvConfig()
    }

    private fun initRvConfig() {
        binding?.let { layoutBinding ->
            layoutBinding.rvConfig = oldInitRecycleAdapterDataBinding(
                    layoutId = R.layout.item_tile_alcohol_batch,
                    itemId = BR.item,
                    onAdapterItemBind = { binding: ItemTileAlcoholBatchBinding, position: Int ->
                        onAdapterBindHandler(binding, position)
                    },
                    onAdapterItemClicked = {position ->
                        recyclerViewKeyHandler?.onItemClicked(position)
                    }
            )

            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner

            recyclerViewKeyHandler = oldInitRecyclerViewKeyHandler(
                    recyclerView = layoutBinding.rv,
                    items = vm.batches,
                    previousPosInfo = recyclerViewKeyHandler?.posInfo?.value,
                    onClickHandler = vm::onClickItemPosition
            )
        }
    }

    override fun onResume() {
        super.onResume()
        vm.onResume()
    }

    companion object {
        fun create(matnr: String, type: ProductDocumentType): AlcoholBatchSelectFragment {
            val fragment = AlcoholBatchSelectFragment()
            fragment.matnr = matnr
            fragment.type = type
            return fragment
        }
    }

}
