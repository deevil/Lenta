package com.lenta.bp9.features.mercury_list

import com.lenta.bp9.R
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.lenta.bp9.BR
import com.lenta.bp9.databinding.*
import com.lenta.bp9.model.task.revise.DeliveryProductDocumentRevise
import com.lenta.bp9.model.task.revise.ProductVetDocumentRevise
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.*
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.utilities.state.state

class MercuryListFragment : CoreFragment<FragmentMercuryListBinding, MercuryListViewModel>(),
        ViewPagerSettings,
        PageSelectionListener,
        ToolbarButtonsClickListener,
        OnBackPresserListener {

    companion object {
        fun create(productDoc: DeliveryProductDocumentRevise): MercuryListFragment {
            MercuryListFragment().let {
                it.productDoc = productDoc
                return it
            }
        }
    }

    private var productDoc by state<DeliveryProductDocumentRevise?>(null)

    private var tiedRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null
    private var untiedRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null

    override fun getLayoutId(): Int = R.layout.fragment_mercury_list

    override fun getPageNumber(): String = "09/100"

    override fun getViewModel(): MercuryListViewModel {
        provideViewModel(MercuryListViewModel::class.java).let {vm ->
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
                    bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.untie)
                    connectLiveData(vm.untiedEnabled, bottomToolbarUiModel.uiModelButton2.enabled)
                } else {
                    bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.tied)
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
        if (position == 0) {
            DataBindingUtil
                    .inflate<LayoutMercuryListTiedBinding>(LayoutInflater.from(container.context),
                            R.layout.layout_mercury_list_tied,
                            container,
                            false).let { layoutBinding ->

                        val onClickSelectionListener = View.OnClickListener {
                            (it!!.tag as Int).let { position ->
                                vm.tiedSelectionsHelper.revert(position = position)
                                layoutBinding.rv.adapter?.notifyItemChanged(position)
                            }
                        }

                        layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                                layoutId = R.layout.item_tile_mercury_list,
                                itemId = BR.vm,
                                realisation = object : DataBindingAdapter<ItemTileMercuryListBinding> {
                                    override fun onCreate(binding: ItemTileMercuryListBinding) {
                                    }

                                    override fun onBind(binding: ItemTileMercuryListBinding, position: Int) {
                                        binding.tvCounter.tag = position
                                        binding.tvCounter.setOnClickListener(onClickSelectionListener)
                                        binding.selectedForDelete = vm.tiedSelectionsHelper.isSelected(position)
                                        tiedRecyclerViewKeyHandler?.let {
                                            binding.root.isSelected = it.isSelected(position)
                                        }
                                    }

                                },
                                onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                                    tiedRecyclerViewKeyHandler?.let {
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
                        tiedRecyclerViewKeyHandler = RecyclerViewKeyHandler(
                                rv = layoutBinding.rv,
                                items = vm.listTied,
                                lifecycleOwner = layoutBinding.lifecycleOwner!!,
                                initPosInfo = tiedRecyclerViewKeyHandler?.posInfo?.value
                        )
                        return layoutBinding.root
                    }
        }

        DataBindingUtil
                .inflate<LayoutMercuryListUntiedBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_mercury_list_untied,
                        container,
                        false).let { layoutBinding ->

                    val onClickSelectionListener = View.OnClickListener {
                        (it!!.tag as Int).let { position ->
                            vm.untiedSelectionsHelper.revert(position = position)
                            layoutBinding.rv.adapter?.notifyItemChanged(position)
                        }
                    }

                    layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                            layoutId = R.layout.item_tile_mercury_list,
                            itemId = BR.vm,
                            realisation = object : DataBindingAdapter<ItemTileMercuryListBinding> {
                                override fun onCreate(binding: ItemTileMercuryListBinding) {
                                }

                                override fun onBind(binding: ItemTileMercuryListBinding, position: Int) {
                                    binding.tvCounter.tag = position
                                    binding.tvCounter.setOnClickListener(onClickSelectionListener)
                                    binding.selectedForDelete = vm.untiedSelectionsHelper.isSelected(position)
                                    untiedRecyclerViewKeyHandler?.let {
                                        binding.root.isSelected = it.isSelected(position)
                                    }
                                }

                            },
                            onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                                untiedRecyclerViewKeyHandler?.let {
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
                    untiedRecyclerViewKeyHandler = RecyclerViewKeyHandler(
                            rv = layoutBinding.rv,
                            items = vm.listUntied,
                            lifecycleOwner = layoutBinding.lifecycleOwner!!,
                            initPosInfo = untiedRecyclerViewKeyHandler?.posInfo?.value
                    )
                    return layoutBinding.root
                }
    }

    override fun getTextTitle(position: Int): String = getString(if (position == 0) R.string.tied else R.string.untied)

    override fun countTab(): Int {
        return 2
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

}
