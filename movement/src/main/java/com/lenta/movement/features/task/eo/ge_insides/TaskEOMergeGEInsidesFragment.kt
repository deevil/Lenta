package com.lenta.movement.features.task.eo.ge_insides

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import com.lenta.movement.R
import com.lenta.movement.databinding.FragmentTaskEoMergeBinding
import com.lenta.movement.models.ProcessingUnit
import com.lenta.movement.platform.extensions.getAppComponent
import com.lenta.movement.platform.extensions.unsafeLazy
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
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.provideViewModel

/** Список вложенных ЕО (при нажатии на элемент в списке ГЕ на экране Объединение ЕО (TaskEOMergeFragment)*/
class TaskEOMergeGEInsidesFragment : CoreFragment<FragmentTaskEoMergeBinding, TaskEOMergeGEInsidesViewModel>(),
        ToolbarButtonsClickListener,
        OnBackPresserListener,
        OnScanResultListener,
        OnKeyDownListener {

    private var eoListRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null
    private val eoList: List<ProcessingUnit>? by unsafeLazy {
        arguments?.getParcelableArrayList<ProcessingUnit>(EO_LIST_KEY)
    }

    override fun getLayoutId() = R.layout.fragment_task_eo_merge_ge_insides

    override fun getPageNumber() = PAGE_NUMBER

    override fun getViewModel(): TaskEOMergeGEInsidesViewModel {
        provideViewModel(TaskEOMergeGEInsidesViewModel::class.java).let { vm ->
            getAppComponent()?.inject(vm)
            if (eoList != null) {
                vm.eoList.value = eoList
            }
            return vm
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
        topToolbarUiModel.description.value = getString(R.string.task_eo_merge_title)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.cleanAll()

        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.exclude)


        connectLiveData(vm.isExcludeBtnEnabled, bottomToolbarUiModel.uiModelButton4.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_5 -> vm.onExcludeBtnClick()
        }
    }

    override fun onBackPressed(): Boolean {
        vm.onBackPressed()
        return false
    }

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }

    override fun onKeyDown(keyCode: KeyCode): Boolean {
        eoListRecyclerViewKeyHandler?.let {
            if (it.onKeyDown(keyCode)) {
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

        fun newInstance(eoList: List<ProcessingUnit>): TaskEOMergeGEInsidesFragment {
            return TaskEOMergeGEInsidesFragment().apply {
                arguments = bundleOf (
                    EO_LIST_KEY to eoList
                )
            }
        }
    }
}

