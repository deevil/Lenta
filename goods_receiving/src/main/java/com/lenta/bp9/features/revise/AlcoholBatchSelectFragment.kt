package com.lenta.bp9.features.revise

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
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
import com.lenta.shared.utilities.databinding.DataBindingAdapter
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.databinding.RecyclerViewKeyHandler
import com.lenta.shared.utilities.extentions.generateScreenNumber
import com.lenta.shared.utilities.extentions.provideViewModel

class AlcoholBatchSelectFragment : CoreFragment<FragmentAlcoholBatchSelectBinding, AlcoholBatchSelectViewModel>() {

    private lateinit var matnr: String
    private lateinit var type: ProductDocumentType

    private var recyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null

    override fun getLayoutId(): Int = R.layout.fragment_alcohol_batch_select

    override fun getPageNumber() = generateScreenNumber()

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
            layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                    layoutId = R.layout.item_tile_alcohol_batch,
                    itemId = BR.vm,
                    realisation = object : DataBindingAdapter<ItemTileAlcoholBatchBinding> {
                        override fun onCreate(binding: ItemTileAlcoholBatchBinding) {
                        }

                        override fun onBind(binding: ItemTileAlcoholBatchBinding, position: Int) {
                            binding.tvCounter.tag = position
                            recyclerViewKeyHandler?.let {
                                binding.root.isSelected = it.isSelected(position)
                            }
                        }
                    },
                    onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                        recyclerViewKeyHandler?.let {
                            if (it.isSelected(position)) {
                                vm.onClickItemPosition(position)
                            } else {
                                it.selectPosition(position)
                            }
                        }

                    }
            )

            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner
            recyclerViewKeyHandler = RecyclerViewKeyHandler(
                    rv = layoutBinding.rv,
                    items = vm.batches,
                    lifecycleOwner = layoutBinding.lifecycleOwner!!,
                    initPosInfo = recyclerViewKeyHandler?.posInfo?.value
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
