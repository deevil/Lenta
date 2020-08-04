package com.lenta.bp9.features.editing_invoice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.lenta.bp9.BR
import com.lenta.bp9.R
import com.lenta.bp9.databinding.*
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.keys.KeyCode
import com.lenta.shared.keys.OnKeyDownListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.databinding.DataBindingAdapter
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.databinding.RecyclerViewKeyHandler
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.provideViewModel

class EditingInvoiceFragment : CoreFragment<FragmentEditingInvoiceBinding, EditingInvoiceViewModel>(),
        ViewPagerSettings,
        ToolbarButtonsClickListener,
        OnKeyDownListener {

    private var totalRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null
    private var delRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null
    private var addRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null
    private var notesRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null

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
                        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.restore, enabled = vm.enabledRestoreDelBtn.value ?: false)
                    } else {
                        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.delete, enabled = vm.enabledRestoreDelBtn.value ?: false)
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
            0 -> {
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
                                    onAdapterItemBind = { binding: ItemTileEditingInvoiceTotalBinding, position: Int ->
                                        binding.tvItemNumber.tag = position
                                        binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                                        binding.etQuantity.setOnFocusChangeListener { v, hasFocus ->
                                            if (!hasFocus) {
                                                vm.finishedInput(position)
                                            }
                                        }
                                        binding.selectedForDelete = vm.totalSelectionsHelper.isSelected(position)
                                        totalRecyclerViewKeyHandler
                                                ?.let {
                                                    binding.root.isSelected = it.isSelected(position)
                                                }
                                    },
                                    onAdapterItemClicked = { position ->
                                        totalRecyclerViewKeyHandler?.selectPosition(position)
                                    }
                            )

                            layoutBinding.vm = vm
                            layoutBinding.lifecycleOwner = viewLifecycleOwner

                            totalRecyclerViewKeyHandler = initRecyclerViewKeyHandler(
                                    recyclerView = layoutBinding.rv,
                                    previousPosInfo = totalRecyclerViewKeyHandler?.posInfo?.value,
                                    items = vm.listTotal
                            )

                            return layoutBinding.root
                        }
            }
            1 -> {
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
                                    onAdapterItemBind = { binding: ItemTileEditingInvoiceDelAddBinding, position: Int ->
                                        binding.tvItemNumber.tag = position
                                        binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                                        binding.selectedForDelete = vm.delSelectionsHelper.isSelected(position)
                                        delRecyclerViewKeyHandler?.let {
                                            binding.root.isSelected = it.isSelected(position)
                                        }
                                    },
                                    onAdapterItemClicked = { position ->
                                        delRecyclerViewKeyHandler?.selectPosition(position)
                                    }
                            )

                            layoutBinding.vm = vm
                            layoutBinding.lifecycleOwner = viewLifecycleOwner

                            delRecyclerViewKeyHandler = initRecyclerViewKeyHandler(
                                    recyclerView = layoutBinding.rv,
                                    previousPosInfo = delRecyclerViewKeyHandler?.posInfo?.value,
                                    items = vm.listDelItem
                            )

                            return layoutBinding.root
                        }
            }
            2 -> {
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
                                    onAdapterItemBind = { binding: ItemTileEditingInvoiceDelAddBinding, position: Int ->
                                        binding.tvItemNumber.tag = position
                                        binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                                        binding.selectedForDelete = vm.addSelectionsHelper.isSelected(position)
                                        addRecyclerViewKeyHandler?.let {
                                            binding.root.isSelected = it.isSelected(position)
                                        }
                                    },
                                    onAdapterItemClicked = { position ->
                                        addRecyclerViewKeyHandler?.selectPosition(position)
                                    }
                            )

                            layoutBinding.vm = vm
                            layoutBinding.lifecycleOwner = viewLifecycleOwner

                            addRecyclerViewKeyHandler = initRecyclerViewKeyHandler(
                                    recyclerView = layoutBinding.rv,
                                    previousPosInfo = addRecyclerViewKeyHandler?.posInfo?.value,
                                    items = vm.listAddItem
                            )

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
                                    onAdapterItemBind = { binding: ItemTileEditingInvoiceNotesBinding, position: Int ->
                                        binding.tvItemNumber.tag = position
                                        binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                                        binding.selectedForDelete = vm.notesSelectionsHelper.isSelected(position)
                                        notesRecyclerViewKeyHandler?.let {
                                            binding.root.isSelected = it.isSelected(position)
                                        }
                                    },
                                    onAdapterItemClicked = { position ->
                                        notesRecyclerViewKeyHandler?.selectPosition(position)
                                    }
                            )

                            layoutBinding.vm = vm
                            layoutBinding.lifecycleOwner = viewLifecycleOwner

                            notesRecyclerViewKeyHandler = initRecyclerViewKeyHandler(
                                    recyclerView = layoutBinding.rv,
                                    previousPosInfo = notesRecyclerViewKeyHandler?.posInfo?.value,
                                    items = vm.listNotes
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

    override fun onKeyDown(keyCode: KeyCode): Boolean {
        when (vm.selectedPage.value) {
            0 -> totalRecyclerViewKeyHandler
            1 -> delRecyclerViewKeyHandler
            2 -> addRecyclerViewKeyHandler
            3 -> notesRecyclerViewKeyHandler
            else -> null
        }?.let {
            if (!it.onKeyDown(keyCode)) {
                keyCode.digit?.let { digit ->
                    vm.onDigitPressed(digit)
                    return true
                }
                return false
            }
            return true
        }
        return false
    }

}
