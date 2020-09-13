package com.lenta.bp9.features.mercury_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.lenta.bp9.BR
import com.lenta.bp9.R
import com.lenta.bp9.databinding.FragmentMercuryListBinding
import com.lenta.bp9.databinding.ItemTileMercuryListBinding
import com.lenta.bp9.databinding.LayoutMercuryListTiedBinding
import com.lenta.bp9.databinding.LayoutMercuryListUntiedBinding
import com.lenta.bp9.model.task.revise.DeliveryProductDocumentRevise
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.utilities.state.state

class MercuryListFragment : CoreFragment<FragmentMercuryListBinding, MercuryListViewModel>(),
        ViewPagerSettings,
        PageSelectionListener,
        ToolbarButtonsClickListener,
        OnBackPresserListener {

    private var productDoc by state<DeliveryProductDocumentRevise?>(null)

    override fun getLayoutId(): Int = R.layout.fragment_mercury_list

    override fun getPageNumber(): String = SCREEN_NUMBER

    override fun getViewModel(): MercuryListViewModel {
        provideViewModel(MercuryListViewModel::class.java).let { vm ->
            getAppComponent()?.inject(vm)
            vm.productDoc.value = this.productDoc
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
        topToolbarUiModel.description.value = getString(R.string.mercury_list_head) + " " + vm.productDoc.value?.initialCount.toStringFormatted() + " " + vm.productDoc.value?.measureUnits?.name
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.next)
        viewLifecycleOwner.apply {
            vm.selectedPage.observe(this, Observer {
                if (it == 0) {
                    bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.untie, enabled = vm.untiedEnabled.value
                            ?: false)
                    connectLiveData(vm.untiedEnabled, bottomToolbarUiModel.uiModelButton2.enabled)
                } else {
                    bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.tied, enabled = vm.tiedEnabled.value
                            ?: false)
                    connectLiveData(vm.tiedEnabled, bottomToolbarUiModel.uiModelButton2.enabled)
                }
            })
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_2 -> vm.onClickTiedUntied()
            R.id.b_5 -> vm.onClickNext()
        }
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        if (position == TAB_TIED) {
            DataBindingUtil
                    .inflate<LayoutMercuryListTiedBinding>(LayoutInflater.from(container.context),
                            R.layout.layout_mercury_list_tied,
                            container,
                            false)
                    .let { layoutBinding ->
                        val onClickSelectionListener = View.OnClickListener {
                            (it!!.tag as Int).let { position ->
                                vm.tiedSelectionsHelper.revert(position = position)
                                layoutBinding.rv.adapter?.notifyItemChanged(position)
                            }
                        }

                        layoutBinding.rvConfig = initRecycleAdapterDataBinding(
                                layoutId = R.layout.item_tile_mercury_list,
                                itemId = BR.item,
                                onItemBind = { binding: ItemTileMercuryListBinding, position: Int ->
                                    binding.tvItemNumber.tag = position
                                    binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                                    binding.selectedForDelete = vm.tiedSelectionsHelper.isSelected(position)
                                },
                                keyHandlerId = TAB_TIED,
                                recyclerView = layoutBinding.rv,
                                items = vm.listTied,
                                onClickHandler = vm::onClickItemPosition
                        )

                        layoutBinding.vm = vm
                        layoutBinding.lifecycleOwner = viewLifecycleOwner

                        return layoutBinding.root
                    }
        }

        DataBindingUtil
                .inflate<LayoutMercuryListUntiedBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_mercury_list_untied,
                        container,
                        false)
                .let { layoutBinding ->
                    val onClickSelectionListener = View.OnClickListener {
                        (it!!.tag as Int).let { position ->
                            vm.untiedSelectionsHelper.revert(position = position)
                            layoutBinding.rv.adapter?.notifyItemChanged(position)
                        }
                    }

                    layoutBinding.rvConfig = initRecycleAdapterDataBinding(
                            layoutId = R.layout.item_tile_mercury_list,
                            itemId = BR.item,
                            onItemBind = { binding: ItemTileMercuryListBinding, position: Int ->
                                binding.tvItemNumber.tag = position
                                binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                                binding.selectedForDelete = vm.untiedSelectionsHelper.isSelected(position)
                            },
                            keyHandlerId = TAB_UNTIED,
                            recyclerView = layoutBinding.rv,
                            items = vm.listUntied,
                            onClickHandler = vm::onClickItemPosition
                    )

                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner

                    return layoutBinding.root
                }
    }

    override fun getTextTitle(position: Int): String = getString(if (position == TAB_TIED) R.string.tied else R.string.untied)

    override fun countTab(): Int {
        return TABS
    }

    override fun onPageSelected(position: Int) {
        vm.onPageSelected(position)
    }

    override fun onResume() {
        super.onResume()
        vm.onResume()
    }

    override fun onBackPressed(): Boolean {
        vm.onBackPressed()
        return false
    }

    companion object {
        fun create(productDoc: DeliveryProductDocumentRevise): MercuryListFragment {
            MercuryListFragment().let {
                it.productDoc = productDoc
                return it
            }
        }

        const val SCREEN_NUMBER = "09/100"

        private const val TABS = 2
        private const val TAB_TIED = 0
        private const val TAB_UNTIED = 1
    }

}
