package com.lenta.inventory.features.storages_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import com.lenta.inventory.BR
import com.lenta.inventory.R
import com.lenta.inventory.databinding.*
import com.lenta.inventory.platform.extentions.getAppComponent
import com.lenta.shared.keys.KeyCode
import com.lenta.shared.keys.OnKeyDownListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.*
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.provideViewModel

class StoragesListFragment : CoreFragment<FragmentStoragesListBinding, StoragesListViewModel>(), ToolbarButtonsClickListener, ViewPagerSettings, PageSelectionListener, OnKeyDownListener, OnScanResultListener
{
    private var unprocessedRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null
    private var processedRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null

    override fun getLayoutId(): Int = R.layout.fragment_storages_list
    override fun getPageNumber(): String = "11/"

    override fun getViewModel(): StoragesListViewModel {
        provideViewModel(StoragesListViewModel::class.java).let { vm ->
            getAppComponent()?.inject(vm)
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
        topToolbarUiModel.description.value = getString(R.string.list_of_storages)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.clean)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.update)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.complete)
        viewLifecycleOwner.apply {
            connectLiveData(source = vm.deleteEnabled, target = bottomToolbarUiModel.uiModelButton3.enabled)
        }
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_3 -> vm.onClickClean()
            R.id.b_4 -> vm.onClickRefresh()
            R.id.b_5 -> vm.onClickComplete()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.let {
            it.viewPagerSettings = this
            it.pageSelectionListener = this
        }
    }

    override fun onResume() {
        super.onResume()
        vm.onResume()
    }

    override fun getTextTitle(position: Int): String = getString(if (position == 0) R.string.not_processed else R.string.processed)

    override fun onPageSelected(position: Int) {
        Logg.d { "onPageSelected $position" }
        vm.onPageSelected(position)
    }

    override fun countTab(): Int = 2

    override fun getPagerItemView(container: ViewGroup, position: Int): View {

        if (position == 0) {
            DataBindingUtil
                    .inflate<LayoutStoragesUnprocessedBinding>(LayoutInflater.from(container.context),
                            R.layout.layout_storages_unprocessed,
                            container,
                            false).let { layoutBinding ->

                        layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                                layoutId = R.layout.item_tile_unprocessed_storages,
                                itemId = BR.vm,
                                realisation = object : DataBindingAdapter<ItemTileUnprocessedStoragesBinding> {
                                    override fun onCreate(binding: ItemTileUnprocessedStoragesBinding) {
                                    }

                                    override fun onBind(binding: ItemTileUnprocessedStoragesBinding, position: Int) {
                                        binding.tvCounter.tag = position
                                        unprocessedRecyclerViewKeyHandler?.let {
                                            binding.root.isSelected = it.isSelected(position)
                                        }
                                    }

                                },
                                onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                                    unprocessedRecyclerViewKeyHandler?.let {
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
                        unprocessedRecyclerViewKeyHandler = RecyclerViewKeyHandler(
                                rv = layoutBinding.rv,
                                items = vm.unprocessedStorages,
                                lifecycleOwner = layoutBinding.lifecycleOwner!!,
                                initPosInfo = unprocessedRecyclerViewKeyHandler?.posInfo?.value
                        )
                        return layoutBinding.root
                    }
        }

        DataBindingUtil
                .inflate<LayoutStoragesProcessedBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_storages_processed,
                        container,
                        false).let { layoutBinding ->

                    val onClickSelectionListener = View.OnClickListener {
                        (it!!.tag as Int).let { position ->
                            vm.processedSelectionHelper.revert(position = position)
                            layoutBinding.rv.adapter?.notifyItemChanged(position)
                        }
                    }

                    layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                            layoutId = R.layout.item_tile_processed_storages,
                            itemId = BR.vm,
                            realisation = object : DataBindingAdapter<ItemTileProcessedStoragesBinding> {
                                override fun onCreate(binding: ItemTileProcessedStoragesBinding) {
                                }

                                override fun onBind(binding: ItemTileProcessedStoragesBinding, position: Int) {
                                    binding.tvCounter.tag = position
                                    binding.tvCounter.setOnClickListener(onClickSelectionListener)
                                    binding.selectedForDelete = vm.processedSelectionHelper.isSelected(position)
                                    processedRecyclerViewKeyHandler?.let {
                                        binding.root.isSelected = it.isSelected(position)
                                    }
                                }

                            },
                            onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                                processedRecyclerViewKeyHandler?.let {
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
                    processedRecyclerViewKeyHandler = RecyclerViewKeyHandler(
                            rv = layoutBinding.rv,
                            items = vm.processedStorages,
                            lifecycleOwner = layoutBinding.lifecycleOwner!!,
                            initPosInfo = processedRecyclerViewKeyHandler?.posInfo?.value
                    )
                    return layoutBinding.root
                }
    }

    override fun onKeyDown(keyCode: KeyCode): Boolean {
        (if (vm.selectedPage.value == 0) {
            unprocessedRecyclerViewKeyHandler
        } else {
            processedRecyclerViewKeyHandler
        })?.let {
            if (!it.onKeyDown(keyCode)) {
                keyCode.digit?.let { digit ->
                    vm.onDigitPressed(digit)
                }
                return true
            }
        }
        return false
    }

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }
}