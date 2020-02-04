package com.lenta.bp16.features.processing_unit_task_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import com.lenta.bp16.BR
import com.lenta.bp16.R
import com.lenta.bp16.databinding.FragmentProcessingUnitTaskListBinding
import com.lenta.bp16.databinding.ItemPuTaskBinding
import com.lenta.bp16.databinding.LayoutPuTaskListProcessedBinding
import com.lenta.bp16.databinding.LayoutPuTaskListProcessingBinding
import com.lenta.bp16.platform.extention.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.databinding.DataBindingAdapter
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.databinding.RecyclerViewKeyHandler
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.lenta.shared.utilities.extentions.provideViewModel

class ProcessingUnitTaskListFragment : CoreFragment<FragmentProcessingUnitTaskListBinding, ProcessingUnitTaskListViewModel>(),
        ViewPagerSettings, ToolbarButtonsClickListener, OnScanResultListener {

    private var processingRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null
    private var processedRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null

    override fun getLayoutId(): Int = R.layout.fragment_processing_unit_task_list

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix("51")

    override fun getViewModel(): ProcessingUnitTaskListViewModel {
        provideViewModel(ProcessingUnitTaskListViewModel::class.java).let {
            getAppComponent()?.inject(it)

            it.deviceIp.value = context!!.getDeviceIp()

            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.title

        connectLiveData(vm.description, topToolbarUiModel.description)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.menu)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.update)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_1 -> vm.onClickMenu()
            R.id.b_5 -> vm.onClickRefresh()
        }
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        if (position == 0) {
            DataBindingUtil.inflate<LayoutPuTaskListProcessingBinding>(LayoutInflater.from(container.context),
                    R.layout.layout_pu_task_list_processing,
                    container,
                    false).let { layoutBinding ->

                layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                        layoutId = R.layout.item_pu_task,
                        itemId = BR.item,
                        realisation = object : DataBindingAdapter<ItemPuTaskBinding> {
                            override fun onCreate(binding: ItemPuTaskBinding) {
                            }

                            override fun onBind(binding: ItemPuTaskBinding, position: Int) {
                                processingRecyclerViewKeyHandler?.let {
                                    binding.root.isSelected = it.isSelected(position)
                                }
                            }
                        },
                        onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                            processingRecyclerViewKeyHandler?.let {
                                if (it.isSelected(position)) {
                                    vm.onClickItemPosition(position)
                                } else {
                                    it.selectPosition(position)
                                }
                            }
                        })

                layoutBinding.vm = vm
                layoutBinding.lifecycleOwner = viewLifecycleOwner
                processingRecyclerViewKeyHandler = RecyclerViewKeyHandler(
                        rv = layoutBinding.rv,
                        items = vm.processing,
                        lifecycleOwner = layoutBinding.lifecycleOwner!!,
                        initPosInfo = processingRecyclerViewKeyHandler?.posInfo?.value
                )

                return layoutBinding.root
            }
        }

        DataBindingUtil.inflate<LayoutPuTaskListProcessedBinding>(LayoutInflater.from(container.context),
                R.layout.layout_pu_task_list_processed,
                container,
                false).let { layoutBinding ->

            layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                    layoutId = R.layout.item_pu_task,
                    itemId = BR.item,
                    realisation = object : DataBindingAdapter<ItemPuTaskBinding> {
                        override fun onCreate(binding: ItemPuTaskBinding) {
                        }

                        override fun onBind(binding: ItemPuTaskBinding, position: Int) {
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
                    })

            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner
            processedRecyclerViewKeyHandler = RecyclerViewKeyHandler(
                    rv = layoutBinding.rv,
                    items = vm.processed,
                    lifecycleOwner = layoutBinding.lifecycleOwner!!,
                    initPosInfo = processedRecyclerViewKeyHandler?.posInfo?.value
            )

            return layoutBinding.root
        }
    }

    override fun getTextTitle(position: Int): String {
        return when (position) {
            0 -> getString(R.string.processing)
            1 -> getString(R.string.processed)
            else -> throw IllegalArgumentException("Wrong pager position!")
        }
    }

    override fun countTab(): Int {
        return 2
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }

    override fun onResume() {
        super.onResume()
        vm.loadTaskList()
    }
}