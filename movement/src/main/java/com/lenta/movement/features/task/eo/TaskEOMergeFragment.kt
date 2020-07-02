package com.lenta.movement.features.task.eo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.lenta.movement.R
import com.lenta.movement.databinding.FragmentTaskEoMergeBinding
import com.lenta.movement.databinding.LayoutTaskEoMergeEoListTabBinding
import com.lenta.movement.databinding.LayoutTaskEoMergeGeListTabBinding
import com.lenta.movement.models.ProcessingUnit
import com.lenta.movement.platform.extensions.getAppComponent
import com.lenta.movement.requests.network.models.startConsolidation.CargoUnit
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
import com.lenta.shared.utilities.databinding.RecyclerViewKeyHandler
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.provideViewModel

class TaskEOMergeFragment : CoreFragment<FragmentTaskEoMergeBinding, TaskEOMergeViewModel>(),
        ViewPagerSettings,
        ToolbarButtonsClickListener,
        OnBackPresserListener,
        OnScanResultListener,
        OnKeyDownListener {

    private var eoListRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null
    private var geListRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null
    private var eoList: List<ProcessingUnit>? = null
    private var geList: List<CargoUnit>? = null

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

    override fun onResume() {
        super.onResume()
        vm.onResume()
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
                iconRes = R.drawable.ic_process,
                titleRes = R.string.process
        ))
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.exclude)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.save)
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
                        rvConfig = simpleListRecyclerViewConfig(
                                recyclerView = eoRecyclerView,
                                selectionItemsHelper = vm.eoSelectionHelper,
                                recyclerViewKeyHandler = eoListRecyclerViewKeyHandler,
                                onClickItem = { position -> vm.onClickEOListItem(position) })

                        viewModel = vm
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

                        viewModel = vm
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

        fun newInstance(eoList: List<ProcessingUnit>, geList: List<CargoUnit>): TaskEOMergeFragment {
            return TaskEOMergeFragment().apply {
                this.eoList = eoList
                this.geList = geList
            }
        }
    }
}