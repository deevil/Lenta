package com.lenta.bp9.features.editing_invoice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.lenta.bp9.BR
import com.lenta.bp9.R
import com.lenta.bp9.databinding.*
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.KeyDownCoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.provideViewModel

class EditingInvoiceFragment : KeyDownCoreFragment<FragmentEditingInvoiceBinding, EditingInvoiceViewModel>(),
        ViewPagerSettings,
        ToolbarButtonsClickListener {

    override fun getLayoutId(): Int = R.layout.fragment_editing_invoice

    override fun getPageNumber() = SCREEN_NUMBER

    override fun getViewModel(): EditingInvoiceViewModel {
        provideViewModel(EditingInvoiceViewModel::class.java).let { vm ->
            getAppComponent()?.inject(vm)
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
        topToolbarUiModel.description.value = vm.getDescription()
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.refusal)
        viewLifecycleOwner.apply {
            vm.selectedPage.observe(this, Observer {
                if (vm.editingAvailable) {
                    bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.save)
                    if (it == 1) {
                        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.restore, enabled = vm.enabledRestoreDelBtn.value
                                ?: false)
                    } else {
                        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.delete, enabled = vm.enabledRestoreDelBtn.value
                                ?: false)
                    }
                    connectLiveData(vm.enabledRestoreDelBtn, bottomToolbarUiModel.uiModelButton3.enabled)
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
            TAB_TOTAL -> {
                DataBindingUtil
                        .inflate<LayoutEditingInvoiceTotalBinding>(LayoutInflater.from(container.context),
                                R.layout.layout_editing_invoice_total,
                                container,
                                false)
                        .let { layoutBinding ->

                            val onClickSelectionListener = View.OnClickListener {
                                (it!!.tag as Int).let { position ->
                                    vm.totalSelectionsHelper.revert(position = position)
                                    layoutBinding.rv.adapter?.notifyItemChanged(position)
                                }
                            }

                            layoutBinding.rvConfig = initRecycleAdapterDataBinding(
                                    layoutId = R.layout.item_tile_editing_invoice_total,
                                    itemId = BR.item,
                                    onItemBind = { binding: ItemTileEditingInvoiceTotalBinding, position: Int ->
                                        binding.tvItemNumber.tag = position
                                        binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                                        binding.etQuantity.setOnFocusChangeListener { _, hasFocus ->
                                            if (!hasFocus) {
                                                vm.finishedInput(position)
                                            }
                                        }
                                        binding.selectedForDelete = vm.totalSelectionsHelper.isSelected(position)
                                    },
                                    keyHandlerId = TAB_TOTAL,
                                    recyclerView = layoutBinding.rv,
                                    items = vm.listTotal
                            )

                            layoutBinding.vm = vm
                            layoutBinding.lifecycleOwner = viewLifecycleOwner

                            return layoutBinding.root
                        }
            }
            TAB_REMOVED -> {
                DataBindingUtil
                        .inflate<LayoutEditingInvoiceDelBinding>(LayoutInflater.from(container.context),
                                R.layout.layout_editing_invoice_del,
                                container,
                                false)
                        .let { layoutBinding ->
                            val onClickSelectionListener = View.OnClickListener {
                                (it!!.tag as Int).let { position ->
                                    vm.delSelectionsHelper.revert(position = position)
                                    layoutBinding.rv.adapter?.notifyItemChanged(position)
                                }
                            }

                            layoutBinding.rvConfig = initRecycleAdapterDataBinding(
                                    layoutId = R.layout.item_tile_editing_invoice_del_add,
                                    itemId = BR.item,
                                    onItemBind = { binding: ItemTileEditingInvoiceDelAddBinding, position: Int ->
                                        binding.tvItemNumber.tag = position
                                        binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                                        binding.selectedForDelete = vm.delSelectionsHelper.isSelected(position)
                                    },
                                    keyHandlerId = TAB_REMOVED,
                                    recyclerView = layoutBinding.rv,
                                    items = vm.listDelItem
                            )

                            layoutBinding.vm = vm
                            layoutBinding.lifecycleOwner = viewLifecycleOwner

                            return layoutBinding.root
                        }
            }
            TAB_ADDED -> {
                DataBindingUtil
                        .inflate<LayoutEditingInvoiceAddBinding>(LayoutInflater.from(container.context),
                                R.layout.layout_editing_invoice_add,
                                container,
                                false)
                        .let { layoutBinding ->
                            val onClickSelectionListener = View.OnClickListener {
                                (it!!.tag as Int).let { position ->
                                    vm.addSelectionsHelper.revert(position = position)
                                    layoutBinding.rv.adapter?.notifyItemChanged(position)
                                }
                            }

                            layoutBinding.rvConfig = initRecycleAdapterDataBinding(
                                    layoutId = R.layout.item_tile_editing_invoice_del_add,
                                    itemId = BR.item,
                                    onItemBind = { binding: ItemTileEditingInvoiceDelAddBinding, position: Int ->
                                        binding.tvItemNumber.tag = position
                                        binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                                        binding.selectedForDelete = vm.addSelectionsHelper.isSelected(position)
                                    },
                                    keyHandlerId = TAB_ADDED,
                                    recyclerView = layoutBinding.rv,
                                    items = vm.listAddItem
                            )

                            layoutBinding.vm = vm
                            layoutBinding.lifecycleOwner = viewLifecycleOwner

                            return layoutBinding.root
                        }
            }
            else -> {
                DataBindingUtil
                        .inflate<LayoutEditingInvoiceNotesBinding>(LayoutInflater.from(container.context),
                                R.layout.layout_editing_invoice_notes,
                                container,
                                false)
                        .let { layoutBinding ->
                            val onClickSelectionListener = View.OnClickListener {
                                (it!!.tag as Int).let { position ->
                                    vm.notesSelectionsHelper.revert(position = position)
                                    layoutBinding.rv.adapter?.notifyItemChanged(position)
                                }
                            }

                            layoutBinding.rvConfig = initRecycleAdapterDataBinding(
                                    layoutId = R.layout.item_tile_editing_invoice_notes,
                                    itemId = BR.item,
                                    onItemBind = { binding: ItemTileEditingInvoiceNotesBinding, position: Int ->
                                        binding.tvItemNumber.tag = position
                                        binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                                        binding.selectedForDelete = vm.notesSelectionsHelper.isSelected(position)
                                    },
                                    keyHandlerId = TAB_NOTE,
                                    recyclerView = layoutBinding.rv,
                                    items = vm.listNotes
                            )

                            layoutBinding.vm = vm
                            layoutBinding.lifecycleOwner = viewLifecycleOwner

                            return layoutBinding.root
                        }
            }
        }

    }

    override fun getTextTitle(position: Int): String = getString(
            when (position) {
                TAB_TOTAL -> R.string.in_total
                TAB_REMOVED -> R.string.removed
                TAB_ADDED -> R.string.added
                else -> R.string.note
            }
    )

    override fun countTab(): Int {
        return TABS
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }

    companion object {
        const val SCREEN_NUMBER = "09/09"

        private const val TABS = 4
        private const val TAB_TOTAL = 0
        private const val TAB_REMOVED = 1
        private const val TAB_ADDED = 2
        private const val TAB_NOTE = 3
    }

}
