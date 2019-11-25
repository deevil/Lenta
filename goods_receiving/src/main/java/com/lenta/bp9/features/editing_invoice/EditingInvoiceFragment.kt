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
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.lenta.bp9.BR
import com.lenta.bp9.databinding.*
import com.lenta.shared.keys.KeyCode
import com.lenta.shared.keys.OnKeyDownListener
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.utilities.databinding.DataBindingAdapter
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.databinding.RecyclerViewKeyHandler
import com.lenta.shared.utilities.extentions.connectLiveData

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
        topToolbarUiModel.description.value = getString(R.string.delivery_note_correction)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.refusal)
        viewLifecycleOwner.apply {
            vm.selectedPage.observe(this, Observer {
                if (vm.editingAvailable) {
                    bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.save)
                    if (it == 1) {
                        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.restore)
                    } else {
                        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.delete)
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
                                            binding.etQuantity.setOnEditorActionListener { v, actionId, event ->
                                                when(actionId) {
                                                    EditorInfo.IME_ACTION_NEXT -> {
                                                        vm.finishedInput(position)
                                                        return@setOnEditorActionListener false
                                                    }
                                                    else -> return@setOnEditorActionListener false
                                                }
                                            }
                                            binding.selectedForDelete = vm.totalSelectionsHelper.isSelected(position)
                                            totalRecyclerViewKeyHandler?.let {
                                                binding.root.isSelected = it.isSelected(position)
                                            }
                                        }

                                    },
                                    onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                                        totalRecyclerViewKeyHandler?.let {
                                            it.selectPosition(position)
                                        }
                                    }
                            )

                            layoutBinding.vm = vm
                            layoutBinding.lifecycleOwner = viewLifecycleOwner
                            totalRecyclerViewKeyHandler = RecyclerViewKeyHandler(
                                    rv = layoutBinding.rv,
                                    items = vm.listTotal,
                                    lifecycleOwner = layoutBinding.lifecycleOwner!!,
                                    initPosInfo = totalRecyclerViewKeyHandler?.posInfo?.value
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
                                            delRecyclerViewKeyHandler?.let {
                                                binding.root.isSelected = it.isSelected(position)
                                            }
                                        }

                                    },
                                    onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                                        delRecyclerViewKeyHandler?.let {
                                            it.selectPosition(position)
                                        }
                                    }
                            )

                            layoutBinding.vm = vm
                            layoutBinding.lifecycleOwner = viewLifecycleOwner
                            delRecyclerViewKeyHandler = RecyclerViewKeyHandler(
                                    rv = layoutBinding.rv,
                                    items = vm.listDelItem,
                                    lifecycleOwner = layoutBinding.lifecycleOwner!!,
                                    initPosInfo = delRecyclerViewKeyHandler?.posInfo?.value
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
                                            addRecyclerViewKeyHandler?.let {
                                                binding.root.isSelected = it.isSelected(position)
                                            }
                                        }

                                    },
                                    onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                                        addRecyclerViewKeyHandler?.let {
                                            it.selectPosition(position)
                                        }
                                    }
                            )

                            layoutBinding.vm = vm
                            layoutBinding.lifecycleOwner = viewLifecycleOwner
                            addRecyclerViewKeyHandler = RecyclerViewKeyHandler(
                                    rv = layoutBinding.rv,
                                    items = vm.listAddItem,
                                    lifecycleOwner = layoutBinding.lifecycleOwner!!,
                                    initPosInfo = addRecyclerViewKeyHandler?.posInfo?.value
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
                                            notesRecyclerViewKeyHandler?.let {
                                                binding.root.isSelected = it.isSelected(position)
                                            }
                                        }

                                    },
                                    onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                                        notesRecyclerViewKeyHandler?.let {
                                            it.selectPosition(position)
                                        }
                                    }
                            )

                            layoutBinding.vm = vm
                            layoutBinding.lifecycleOwner = viewLifecycleOwner
                            notesRecyclerViewKeyHandler = RecyclerViewKeyHandler(
                                    rv = layoutBinding.rv,
                                    items = vm.listNotes,
                                    lifecycleOwner = layoutBinding.lifecycleOwner!!,
                                    initPosInfo = notesRecyclerViewKeyHandler?.posInfo?.value
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
