package com.lenta.bp9.features.revise.invoice

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
import com.lenta.shared.platform.activity.OnBackPresserListener
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

class InvoiceReviseFragment : CoreFragment<FragmentInvoiceReviseBinding, InvoiceReviseViewModel>(), ViewPagerSettings, ToolbarButtonsClickListener, OnBackPresserListener {

    private var notesRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null

    override fun getLayoutId(): Int = R.layout.fragment_invoice_revise

    override fun getPageNumber() = "09/08"

    override fun getViewModel(): InvoiceReviseViewModel {
        provideViewModel(InvoiceReviseViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.taskCaption
        topToolbarUiModel.description.value = getString(R.string.invoice_revise)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.refusal)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.next)
        connectLiveData(vm.nextPossible, bottomToolbarUiModel.uiModelButton5.enabled)
        viewLifecycleOwner.apply {
            vm.selectedPage.observe(this, Observer {
                if (it == 2) {
                    if (vm.isEInvoice) {
                        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.browsing)
                    } else {
                        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.fix)
                    }
                } else {
                    bottomToolbarUiModel.uiModelButton3.clean()
                }
            })
        }
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return when (position) {
            0 -> prepareHeaderView(container)
            1 -> prepareSupplierView(container)
            2 -> prepareDetailsView(container)
            3 -> prepareNotesView(container)
            else -> View(context)
        }
    }

    private fun prepareHeaderView(container: ViewGroup): View {
        DataBindingUtil
                .inflate<LayoutInvoiceReviseHeaderBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_invoice_revise_header,
                        container,
                        false).let { layoutBinding ->
                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner
                    return layoutBinding.root
                }
    }

    private fun prepareSupplierView(container: ViewGroup): View {
        DataBindingUtil
                .inflate<LayoutInvoiceReviseSupplierBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_invoice_revise_supplier,
                        container,
                        false).let { layoutBinding ->
                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner
                    return layoutBinding.root
                }
    }

    private fun prepareDetailsView(container: ViewGroup): View {
        DataBindingUtil
                .inflate<LayoutInvoiceReviseDetailsBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_invoice_revise_details,
                        container,
                        false).let { layoutBinding ->
                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner
                    return layoutBinding.root
                }
    }

    private fun prepareNotesView(container: ViewGroup): View {
        DataBindingUtil
                .inflate<LayoutInvoiceReviseNotesBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_invoice_revise_notes,
                        container,
                        false).let { layoutBinding ->

                    layoutBinding.rvConfig = initRecycleAdapterDataBinding(
                            layoutId = R.layout.item_tile_notes,
                            itemId = BR.item,
                            onAdapterItemBind = { binding: ItemTileNotesBinding, position: Int ->
                                binding.tvItemNumber.tag = position
                                onAdapterBindHandler(binding, position)
                            }
                    )

                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner
                    val rvKeyHandler = RecyclerViewKeyHandler(
                            rv = layoutBinding.rv,
                            items = vm.notes,
                            lifecycleOwner = layoutBinding.lifecycleOwner!!
                    )
                    notesRecyclerViewKeyHandler = rvKeyHandler
                    return layoutBinding.root
                }
    }

    override fun getTextTitle(position: Int): String {
        return when (position) {
            0 -> getString(R.string.header)
            1 -> getString(R.string.supplier)
            2 -> getString(R.string.details)
            3 -> getString(R.string.notes)
            else -> ""
        }
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_2 -> vm.onClickReject()
            R.id.b_3 -> vm.onClickEdit()
            R.id.b_5 -> vm.onClickNext()
        }
    }

    override fun countTab(): Int {
        return 4
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }

    override fun onResume() {
        super.onResume()
        vm.onResume()
    }

    override fun onBackPressed(): Boolean {
        vm.onBackPressed()
        return false
    }

}
