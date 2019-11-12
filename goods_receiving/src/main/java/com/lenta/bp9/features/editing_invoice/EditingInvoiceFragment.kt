package com.lenta.bp9.features.editing_invoice

import com.lenta.bp9.R
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel
import android.os.Bundle
import android.view.LayoutInflater
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import android.view.ViewGroup
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.lenta.bp9.BR
import com.lenta.bp9.databinding.*
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.utilities.databinding.DataBindingAdapter
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.databinding.RecyclerViewKeyHandler

class EditingInvoiceFragment : CoreFragment<FragmentEditingInvoiceBinding, EditingInvoiceViewModel>(),
        ViewPagerSettings,
        ToolbarButtonsClickListener {

    private var recyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null

    override fun getLayoutId(): Int = R.layout.fragment_editing_invoice

    override fun getPageNumber() = "09/09"

    override fun getViewModel(): EditingInvoiceViewModel {
        provideViewModel(EditingInvoiceViewModel::class.java).let {vm ->
            getAppComponent()?.inject(vm)
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
        topToolbarUiModel.description.value = getString(R.string.delivery_note_correction)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.refusal)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.save)
        viewLifecycleOwner.apply {
            vm.selectedPage.observe(this, Observer {
                if (it == 1) {
                    bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.restore)
                    //connectLiveData(vm.enabledDelBtn, bottomToolbarUiModel.uiModelButton3.enabled)
                } else {
                    bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.delete)
                    //connectLiveData(vm.enabledDelBtn, bottomToolbarUiModel.uiModelButton3.enabled)
                }
            })
        }
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_2 -> vm.onClickRefusal()
            R.id.b_3 -> if (vm.selectedPage.value == 1) vm.onClickRestore() else vm.onClickDelete()
            R.id.b_5 -> vm.onClickSave()
        }
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        when (position) {
            0 -> {
                DataBindingUtil
                        .inflate<LayoutEditingInvoiceTotalBinding>(LayoutInflater.from(container.context),
                                R.layout.layout_editing_invoice_total,
                                container,
                                false).let { layoutBinding ->

                            val onClickSelectionListener = View.OnClickListener {
                                (it!!.tag as Int).let { position ->
                                    vm.totalSelectionsHelper.revert(position = position)
                                    layoutBinding.rv.adapter?.notifyItemChanged(position)
                                }
                            }

                            layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                                    layoutId = R.layout.item_tile_editing_invoice_total,
                                    itemId = BR.vm,
                                    realisation = object : DataBindingAdapter<ItemTileEditingInvoiceTotalBinding> {
                                        override fun onCreate(binding: ItemTileEditingInvoiceTotalBinding) {
                                        }

                                        override fun onBind(binding: ItemTileEditingInvoiceTotalBinding, position: Int) {
                                            binding.tvCounter.tag = position
                                            binding.tvCounter.setOnClickListener(onClickSelectionListener)
                                            binding.selectedForDelete = vm.totalSelectionsHelper.isSelected(position)
                                            recyclerViewKeyHandler?.let {
                                                binding.root.isSelected = it.isSelected(position)
                                            }
                                        }

                                    }
                            )

                            layoutBinding.vm = vm
                            layoutBinding.lifecycleOwner = viewLifecycleOwner
                            recyclerViewKeyHandler = RecyclerViewKeyHandler(
                                    rv = layoutBinding.rv,
                                    items = vm.listTotal,
                                    lifecycleOwner = layoutBinding.lifecycleOwner!!,
                                    initPosInfo = recyclerViewKeyHandler?.posInfo?.value
                            )
                            return layoutBinding.root
                        }
            }
            1 -> {
                DataBindingUtil
                        .inflate<LayoutEditingInvoiceDelBinding>(LayoutInflater.from(container.context),
                                R.layout.layout_editing_invoice_del,
                                container,
                                false).let { layoutBinding ->

                            val onClickSelectionListener = View.OnClickListener {
                                (it!!.tag as Int).let { position ->
                                    vm.delSelectionsHelper.revert(position = position)
                                    layoutBinding.rv.adapter?.notifyItemChanged(position)
                                }
                            }

                            layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                                    layoutId = R.layout.item_tile_editing_invoice_del_add,
                                    itemId = BR.vm,
                                    realisation = object : DataBindingAdapter<ItemTileEditingInvoiceDelAddBinding> {
                                        override fun onCreate(binding: ItemTileEditingInvoiceDelAddBinding) {
                                        }

                                        override fun onBind(binding: ItemTileEditingInvoiceDelAddBinding, position: Int) {
                                            binding.tvCounter.tag = position
                                            binding.tvCounter.setOnClickListener(onClickSelectionListener)
                                            binding.selectedForDelete = vm.delSelectionsHelper.isSelected(position)
                                            recyclerViewKeyHandler?.let {
                                                binding.root.isSelected = it.isSelected(position)
                                            }
                                        }

                                    }
                            )

                            layoutBinding.vm = vm
                            layoutBinding.lifecycleOwner = viewLifecycleOwner
                            recyclerViewKeyHandler = RecyclerViewKeyHandler(
                                    rv = layoutBinding.rv,
                                    items = vm.listDelItem,
                                    lifecycleOwner = layoutBinding.lifecycleOwner!!,
                                    initPosInfo = recyclerViewKeyHandler?.posInfo?.value
                            )
                            return layoutBinding.root
                        }
            }
            2 -> {
                DataBindingUtil
                        .inflate<LayoutEditingInvoiceAddBinding>(LayoutInflater.from(container.context),
                                R.layout.layout_editing_invoice_add,
                                container,
                                false).let { layoutBinding ->

                            val onClickSelectionListener = View.OnClickListener {
                                (it!!.tag as Int).let { position ->
                                    vm.addSelectionsHelper.revert(position = position)
                                    layoutBinding.rv.adapter?.notifyItemChanged(position)
                                }
                            }

                            layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                                    layoutId = R.layout.item_tile_editing_invoice_del_add,
                                    itemId = BR.vm,
                                    realisation = object : DataBindingAdapter<ItemTileEditingInvoiceDelAddBinding> {
                                        override fun onCreate(binding: ItemTileEditingInvoiceDelAddBinding) {
                                        }

                                        override fun onBind(binding: ItemTileEditingInvoiceDelAddBinding, position: Int) {
                                            binding.tvCounter.tag = position
                                            binding.tvCounter.setOnClickListener(onClickSelectionListener)
                                            binding.selectedForDelete = vm.addSelectionsHelper.isSelected(position)
                                            recyclerViewKeyHandler?.let {
                                                binding.root.isSelected = it.isSelected(position)
                                            }
                                        }

                                    }
                            )

                            layoutBinding.vm = vm
                            layoutBinding.lifecycleOwner = viewLifecycleOwner
                            recyclerViewKeyHandler = RecyclerViewKeyHandler(
                                    rv = layoutBinding.rv,
                                    items = vm.listAddItem,
                                    lifecycleOwner = layoutBinding.lifecycleOwner!!,
                                    initPosInfo = recyclerViewKeyHandler?.posInfo?.value
                            )
                            return layoutBinding.root
                        }
            }
            else -> {
                DataBindingUtil
                        .inflate<LayoutEditingInvoiceNotesBinding>(LayoutInflater.from(container.context),
                                R.layout.layout_editing_invoice_notes,
                                container,
                                false).let { layoutBinding ->

                            val onClickSelectionListener = View.OnClickListener {
                                (it!!.tag as Int).let { position ->
                                    vm.notesSelectionsHelper.revert(position = position)
                                    layoutBinding.rv.adapter?.notifyItemChanged(position)
                                }
                            }

                            layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                                    layoutId = R.layout.item_tile_editing_invoice_notes,
                                    itemId = BR.vm,
                                    realisation = object : DataBindingAdapter<ItemTileEditingInvoiceNotesBinding> {
                                        override fun onCreate(binding: ItemTileEditingInvoiceNotesBinding) {
                                        }

                                        override fun onBind(binding: ItemTileEditingInvoiceNotesBinding, position: Int) {
                                            binding.tvCounter.tag = position
                                            binding.tvCounter.setOnClickListener(onClickSelectionListener)
                                            binding.selectedForDelete = vm.notesSelectionsHelper.isSelected(position)
                                            recyclerViewKeyHandler?.let {
                                                binding.root.isSelected = it.isSelected(position)
                                            }
                                        }

                                    }
                            )

                            layoutBinding.vm = vm
                            layoutBinding.lifecycleOwner = viewLifecycleOwner
                            recyclerViewKeyHandler = RecyclerViewKeyHandler(
                                    rv = layoutBinding.rv,
                                    items = vm.listNotes,
                                    lifecycleOwner = layoutBinding.lifecycleOwner!!,
                                    initPosInfo = recyclerViewKeyHandler?.posInfo?.value
                            )
                            return layoutBinding.root
                        }
            }
        }

    }

    override fun getTextTitle(position: Int): String = getString(
            when (position) {
                0 -> R.string.in_total
                1 -> R.string.removed
                2 -> R.string.added
                else -> R.string.note
            }
    )

    override fun countTab(): Int {
        return 4
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }


}
