package com.lenta.movement.features.task.eo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import com.lenta.movement.BR
import com.lenta.movement.R
import com.lenta.movement.databinding.FragmentTaskEoMergeBinding
import com.lenta.movement.databinding.LayoutItemEoBinding
import com.lenta.movement.databinding.LayoutTaskEoMergeEoListTabBinding
import com.lenta.movement.databinding.LayoutTaskEoMergeGeListTabBinding
import com.lenta.movement.models.CargoUnit
import com.lenta.movement.models.ProcessingUnit
import com.lenta.movement.platform.extensions.getAppComponent
import com.lenta.movement.platform.extensions.unsafeLazy
import com.lenta.movement.view.simpleListRecyclerViewConfig
import com.lenta.shared.keys.KeyCode
import com.lenta.shared.keys.OnKeyDownListener
import com.lenta.shared.platform.activity.OnBackPresserListener
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
import com.lenta.shared.utilities.extentions.provideViewModel

class TaskEOMergeFragment : CoreFragment<FragmentTaskEoMergeBinding, TaskEOMergeViewModel>(),
        ViewPagerSettings,
        ToolbarButtonsClickListener,
        OnBackPresserListener,
        OnScanResultListener,
        OnKeyDownListener {

    private var eoListRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null
    private var geListRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null
    private val eoList: List<ProcessingUnit>? by unsafeLazy {
        arguments?.getParcelableArrayList<ProcessingUnit>(EO_LIST_KEY)
    }
    private val geList: MutableList<CargoUnit>? by unsafeLazy {
        arguments?.getParcelableArrayList<CargoUnit>(GE_LIST_KEY)
    }

    override fun getLayoutId() = R.layout.fragment_task_eo_merge

    override fun getPageNumber() = PAGE_NUMBER

    override fun getViewModel(): TaskEOMergeViewModel {
        provideViewModel(TaskEOMergeViewModel::class.java).let { vm ->
            getAppComponent()?.inject(vm)
            if (eoList != null && geList != null) {
                vm.eoList.value = eoList
                vm.geList.value = geList
            }
            return vm
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
        topToolbarUiModel.description.value = getString(R.string.task_eo_merge_title)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.cleanAll()

        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.print)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo(
                iconRes = R.drawable.ic_process_48dp,
                titleRes = R.string.process
        ))

        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.exclude)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.save)

        connectLiveData(vm.isProcessBtnVisible, bottomToolbarUiModel.uiModelButton3.visibility)
        connectLiveData(vm.isExcludeBtnVisible, bottomToolbarUiModel.uiModelButton4.visibility)
        connectLiveData(vm.isExcludeBtnEnabled, bottomToolbarUiModel.uiModelButton4.enabled)
        connectLiveData(vm.isSaveBtnEnabled, bottomToolbarUiModel.uiModelButton5.enabled)

    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_2 -> vm.onPrintBtnClick()
            R.id.b_3 -> vm.onProcessBtnClick()
            R.id.b_4 -> vm.onExcludeBtnClick()
            R.id.b_5 -> vm.onSaveBtnClick()
        }
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return when (TaskEOMergePage.values()[position]) {
            TaskEOMergePage.EO_LIST -> {
                DataBindingUtil.inflate<LayoutTaskEoMergeEoListTabBinding>(
                        LayoutInflater.from(context),
                        R.layout.layout_task_eo_merge_eo_list_tab,
                        container,
                        false
                ).also {
                    it.apply {

                        val onClickSelectionListener = View.OnClickListener { clickListener ->
                            val itemPosition = clickListener.tag as Int
                            vm.eoSelectionHelper.revert(position = itemPosition)
                            this.eoRecyclerView.adapter?.notifyItemChanged(itemPosition)
                        }

                        rvConfig = DataBindingRecyclerViewConfig(
                                layoutId = R.layout.layout_item_eo,
                                itemId = BR.item,
                                realisation = object : DataBindingAdapter<LayoutItemEoBinding> {
                                    override fun onCreate(binding: LayoutItemEoBinding) = Unit

                                    override fun onBind(binding: LayoutItemEoBinding, position: Int) {
                                        binding.tvCounter.tag = position
                                        binding.tvCounter.setOnClickListener(onClickSelectionListener)
                                        vm.eoItemList.value?.let { list ->
                                            binding.item = list[position]
                                        }

                                        binding.selectedToDelete = vm.eoSelectionHelper.isSelected(position)
                                        eoListRecyclerViewKeyHandler?.let { eoListRecyclerViewKeyHandler ->
                                            binding.root.isSelected = eoListRecyclerViewKeyHandler.isSelected(position)
                                        }

                                    }
                                },
                                onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                                    vm.onClickEOListItem(position)
                                }
                        )

                        dataBindingViewModel = vm
                        lifecycleOwner = viewLifecycleOwner

                        lifecycleOwner?.let { lifecycleOwner ->
                            eoListRecyclerViewKeyHandler = RecyclerViewKeyHandler(
                                    eoRecyclerView,
                                    vm.eoItemList,
                                    lifecycleOwner,
                                    eoListRecyclerViewKeyHandler?.posInfo?.value
                            )
                        }
                    }
                }.root
            }

            TaskEOMergePage.GE_LIST -> {
                DataBindingUtil.inflate<LayoutTaskEoMergeGeListTabBinding>(
                        LayoutInflater.from(context),
                        R.layout.layout_task_eo_merge_ge_list_tab,
                        container,
                        false
                ).also { layoutBinding ->
                    layoutBinding.apply {
                        rvConfig = simpleListRecyclerViewConfig(
                                recyclerView = geRecyclerView,
                                selectionItemsHelper = vm.geSelectionHelper,
                                recyclerViewKeyHandler = geListRecyclerViewKeyHandler,
                                onClickItem = { position -> vm.onClickGEListItem(position) }
                        )

                        dataBindingViewModel = vm
                        lifecycleOwner = viewLifecycleOwner

                        lifecycleOwner?.let { lifecycleOwner ->
                            geListRecyclerViewKeyHandler = RecyclerViewKeyHandler(
                                    geRecyclerView,
                                    vm.geItemList,
                                    lifecycleOwner,
                                    geListRecyclerViewKeyHandler?.posInfo?.value
                            )
                        }
                    }

                }.root
            }
        }
    }

    override fun getTextTitle(position: Int): String {
        return when (TaskEOMergePage.values()[position]) {
            TaskEOMergePage.EO_LIST -> getString(R.string.task_eo_merge_eo_list)
            TaskEOMergePage.GE_LIST -> getString(R.string.task_eo_merge_ge_list)
        }
    }

    override fun countTab() = TaskEOMergePage.values().size

    override fun onBackPressed(): Boolean {
        vm.onBackPressed()
        return false
    }

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }

    override fun onKeyDown(keyCode: KeyCode): Boolean {
        when (vm.currentPage.value) {
            TaskEOMergePage.EO_LIST -> eoListRecyclerViewKeyHandler
            TaskEOMergePage.GE_LIST -> geListRecyclerViewKeyHandler
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


    companion object {
        private const val PAGE_NUMBER = "10/06"
        private const val EO_LIST_KEY = "EO_LIST_KEY"
        private const val GE_LIST_KEY = "GE_LIST_KEY"

        fun newInstance(eoList: List<ProcessingUnit>, geList: List<CargoUnit>): TaskEOMergeFragment {
            return TaskEOMergeFragment().apply {
                arguments = bundleOf(
                        EO_LIST_KEY to eoList,
                        GE_LIST_KEY to geList
                )
            }
        }
    }
}

