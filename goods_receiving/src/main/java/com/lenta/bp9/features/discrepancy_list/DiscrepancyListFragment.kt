package com.lenta.bp9.features.discrepancy_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import com.lenta.bp9.BR
import com.lenta.bp9.R
import com.lenta.bp9.databinding.*
import com.lenta.bp9.model.task.TaskType
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.keys.KeyCode
import com.lenta.shared.keys.OnKeyDownListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.databinding.*
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

class DiscrepancyListFragment : CoreFragment<FragmentDiscrepancyListBinding, DiscrepancyListViewModel>(),
        ViewPagerSettings,
        PageSelectionListener,
        ToolbarButtonsClickListener {

    private var notProcessedRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null
    private var controlRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null

    override fun getLayoutId(): Int = R.layout.fragment_discrepancy_list

    override fun getPageNumber(): String = "09/22"

    override fun getViewModel(): DiscrepancyListViewModel {
        provideViewModel(DiscrepancyListViewModel::class.java).let {vm ->
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
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.clean)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.batches)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.save)

        connectLiveData(vm.visibilityCleanButton, bottomToolbarUiModel.uiModelButton3.visibility)
        connectLiveData(vm.enabledCleanButton, bottomToolbarUiModel.uiModelButton3.enabled)
        connectLiveData(vm.visibilityBatchesButton, bottomToolbarUiModel.uiModelButton4.visibility)
        connectLiveData(vm.enabledSaveButton, bottomToolbarUiModel.uiModelButton5.enabled)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_3 -> vm.onClickClean()
            R.id.b_4 -> vm.onClickBatches()
            R.id.b_5 -> vm.onClickSave()
        }
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return if (vm.isAlco.value == true) {
            when(position) {
                0 -> prepareNotProcessedView(container)
                1 -> prepareControlView(container)
                2 -> prepareProcessedView(container)
                else -> View(context)
            }
        } else {
            when(position) {
                0 -> prepareNotProcessedView(container)
                1 -> prepareProcessedView(container)
                else -> View(context)
            }
        }
    }

    private fun prepareNotProcessedView(container: ViewGroup): View {
        DataBindingUtil
                .inflate<LayoutDiscrepancyListNotProcessedBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_discrepancy_list_not_processed,
                        container,
                        false).let { layoutBinding ->

                    layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                            layoutId = R.layout.item_tile_discrepancy_list_not_processed,
                            itemId = BR.vm,
                            realisation = object : DataBindingAdapter<ItemTileDiscrepancyListNotProcessedBinding> {
                                override fun onCreate(binding: ItemTileDiscrepancyListNotProcessedBinding) {
                                }

                                override fun onBind(binding: ItemTileDiscrepancyListNotProcessedBinding, position: Int) {
                                    notProcessedRecyclerViewKeyHandler?.let {
                                        binding.root.isSelected = it.isSelected(position)
                                    }
                                }

                            },
                            onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                                notProcessedRecyclerViewKeyHandler?.let {
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
                    notProcessedRecyclerViewKeyHandler = RecyclerViewKeyHandler(
                            rv = layoutBinding.rv,
                            items = vm.countNotProcessed,
                            lifecycleOwner = layoutBinding.lifecycleOwner!!,
                            initPosInfo = notProcessedRecyclerViewKeyHandler?.posInfo?.value
                    )
                    return layoutBinding.root
                }
    }

    private fun prepareProcessedView(container: ViewGroup): View {
        DataBindingUtil
                .inflate<LayoutDiscrepancyListProcessedBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_discrepancy_list_processed,
                        container,
                        false).let { layoutBinding ->

                    val onClickSelectionListener = View.OnClickListener {
                        (it!!.tag as Int).let { position ->
                            vm.processedSelectionsHelper.revert(position = position)
                            layoutBinding.rv.adapter?.notifyItemChanged(position)
                        }
                    }

                    layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                            layoutId = R.layout.item_tile_discrepancy_list_processed,
                            itemId = BR.vm,
                            realisation = object : DataBindingAdapter<ItemTileDiscrepancyListProcessedBinding> {
                                override fun onCreate(binding: ItemTileDiscrepancyListProcessedBinding) {
                                }

                                override fun onBind(binding: ItemTileDiscrepancyListProcessedBinding, position: Int) {
                                    binding.tvCounter.tag = position
                                    binding.tvCounter.setOnClickListener(onClickSelectionListener)
                                    binding.selectedForDelete = vm.processedSelectionsHelper.isSelected(position)
                                }

                            }
                    )

                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner
                    return layoutBinding.root
                }
    }

    private fun prepareControlView(container: ViewGroup): View {
        DataBindingUtil
                .inflate<LayoutDiscrepancyListControlBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_discrepancy_list_control,
                        container,
                        false).let { layoutBinding ->

                    layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                            layoutId = R.layout.item_tile_discrepancy_list_control,
                            itemId = BR.vm,
                            realisation = object : DataBindingAdapter<ItemTileDiscrepancyListControlBinding> {
                                override fun onCreate(binding: ItemTileDiscrepancyListControlBinding) {
                                }

                                override fun onBind(binding: ItemTileDiscrepancyListControlBinding, position: Int) {
                                    controlRecyclerViewKeyHandler?.let {
                                        binding.root.isSelected = it.isSelected(position)
                                    }
                                }

                            },
                            onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                                controlRecyclerViewKeyHandler?.let {
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
                    controlRecyclerViewKeyHandler = RecyclerViewKeyHandler(
                            rv = layoutBinding.rv,
                            items = vm.countControl,
                            lifecycleOwner = layoutBinding.lifecycleOwner!!,
                            initPosInfo = controlRecyclerViewKeyHandler?.posInfo?.value
                    )
                    return layoutBinding.root
                }
    }

    override fun getTextTitle(position: Int): String {
        return if (vm.isAlco.value == true) {
            when (position) {
                0 -> getString(R.string.not_processed)
                1 -> getString(R.string.control)
                2 -> getString(R.string.processed)
                else -> ""
            }
        } else {
            when (position) {
                0 -> getString(R.string.not_processed)
                1 -> getString(R.string.processed)
                else -> ""
            }
        }
    }

    override fun countTab(): Int {
        return if (vm.isAlco.value == true) 3 else 2
    }

    override fun onPageSelected(position: Int) {
        vm.onPageSelected(position)
    }

    override fun onResume() {
        super.onResume()
        vm.onResume()
    }

}
