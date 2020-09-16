package com.lenta.inventory.features.discrepancies_found

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.lenta.inventory.BR
import com.lenta.inventory.R
import com.lenta.inventory.databinding.FragmentDiscrepanciesFoundBinding
import com.lenta.inventory.databinding.ItemTileDiscrepanciesBinding
import com.lenta.inventory.databinding.LayoutDiscrepanciesBinding
import com.lenta.inventory.databinding.LayoutDiscrepanciesByStorageBinding
import com.lenta.inventory.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class DiscrepanciesFoundFragment : CoreFragment<FragmentDiscrepanciesFoundBinding, DiscrepanciesFoundViewModel>(),
        ToolbarButtonsClickListener, ViewPagerSettings, PageSelectionListener {

    override fun getLayoutId(): Int = R.layout.fragment_discrepancies_found

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix(SCREEN_NUMBER)

    override fun getViewModel(): DiscrepanciesFoundViewModel {
        provideViewModel(DiscrepanciesFoundViewModel::class.java).let { vm ->
            getAppComponent()?.inject(vm)
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
        topToolbarUiModel.description.value = getString(R.string.discrepancies_found)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton2.show(if (vm.selectedPage.value == 0) ButtonDecorationInfo.delete else ButtonDecorationInfo.untie)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.missing)
        bottomToolbarUiModel.uiModelButton5.show(if (vm.isNotEmpty.value == true) ButtonDecorationInfo.skip else ButtonDecorationInfo.complete)

        connectLiveData(source = vm.untieDeleteEnabled, target = getBottomToolBarUIModel()!!.uiModelButton2.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_2 -> vm.onClickDeleteUntie()
            R.id.b_4 -> vm.onClickMissing()
            R.id.b_5 -> vm.onClickSkip()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.let {
            it.viewPagerSettings = this
            it.pageSelectionListener = this
        }

        viewLifecycleOwner.apply {
            vm.selectedPage.observe(this, Observer { getBottomToolBarUIModel()!!.uiModelButton2.show(if (vm.selectedPage.value == 0) ButtonDecorationInfo.delete else ButtonDecorationInfo.untie) })
            vm.isNotEmpty.observe(this, Observer { getBottomToolBarUIModel()!!.uiModelButton5.show(if (vm.isNotEmpty.value == true) ButtonDecorationInfo.skip else ButtonDecorationInfo.complete) })
        }
    }

    override fun onResume() {
        super.onResume()
        vm.onResume()
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return when (position) {
            TAB_BY_GOOD -> initDiscrepancyListByGood(container)
            TAB_BY_STORAGE -> initDiscrepancyListByStorage(container)
            else -> View(context)
        }
    }

    private fun initDiscrepancyListByGood(container: ViewGroup): View {
        DataBindingUtil.inflate<LayoutDiscrepanciesBinding>(LayoutInflater.from(container.context),
                R.layout.layout_discrepancies,
                container,
                false).let { layoutBinding ->

            val onClickSelectionListener = View.OnClickListener {
                (it!!.tag as Int).let { position ->
                    vm.byGoodsSelectionHelper.revert(position = position)
                    layoutBinding.rv.adapter?.notifyItemChanged(position)
                }
            }

            layoutBinding.rvConfig = initRecycleAdapterDataBinding(
                    layoutId = R.layout.item_tile_discrepancies,
                    itemId = BR.item,
                    onItemBind = { binding: ItemTileDiscrepanciesBinding, position: Int ->
                        binding.tvItemNumber.tag = position
                        binding.ivOpen.visibility = if (vm.isRecountByStorePlaces()) View.INVISIBLE else View.VISIBLE
                        binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                        binding.selectedForDelete = vm.byGoodsSelectionHelper.isSelected(position)
                    },
                    keyHandlerId = TAB_BY_GOOD,
                    recyclerView = layoutBinding.rv,
                    items = vm.discrepanciesByGoods,
                    onClickHandler = vm::onClickItemPosition
            )

            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner

            return layoutBinding.root
        }
    }

    private fun initDiscrepancyListByStorage(container: ViewGroup): View {
       DataBindingUtil.inflate<LayoutDiscrepanciesByStorageBinding>(LayoutInflater.from(container.context),
                R.layout.layout_discrepancies_by_storage,
                container,
                false).let { layoutBinding ->

            val onClickSelectionListener = View.OnClickListener {
                (it!!.tag as Int).let { position ->
                    vm.byStorageSelectionHelper.revert(position = position)
                    layoutBinding.rv.adapter?.notifyItemChanged(position)
                }
            }

            layoutBinding.rvConfig = initRecycleAdapterDataBinding(
                    layoutId = R.layout.item_tile_discrepancies,
                    itemId = BR.item,
                    onItemBind = { binding: ItemTileDiscrepanciesBinding, position: Int ->
                        binding.tvItemNumber.tag = position
                        binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                        binding.selectedForDelete = vm.byStorageSelectionHelper.isSelected(position)
                    },
                    keyHandlerId = TAB_BY_STORAGE,
                    recyclerView = layoutBinding.rv,
                    items = vm.discrepanciesByStorage,
                    onClickHandler = vm::onClickItemPosition
            )

            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner

            return layoutBinding.root
        }
    }

    override fun getTextTitle(position: Int): String {
        return when (position) {
            TAB_BY_GOOD -> getString(R.string.byGoods)
            TAB_BY_STORAGE -> getString(R.string.byStorage)
            else -> throw IllegalArgumentException("Wrong pager position!")
        }
    }

    override fun onPageSelected(position: Int) {
        Logg.d { "onPageSelected $position" }
        vm.onPageSelected(position)
    }

    override fun countTab(): Int {
        return if (vm.isRecountByStorePlaces()) 2 else 1
    }

    companion object {
        const val SCREEN_NUMBER = "10"

        private const val TAB_BY_GOOD = 0
        private const val TAB_BY_STORAGE = 1
    }

}