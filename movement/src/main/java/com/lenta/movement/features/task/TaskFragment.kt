package com.lenta.movement.features.task

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import com.lenta.movement.R
import com.lenta.movement.databinding.*
import com.lenta.movement.models.Task
import com.lenta.movement.platform.extensions.getAppComponent
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.extentions.unsafeLazy

class TaskFragment : CoreFragment<FragmentTaskBinding, TaskViewModel>(),
    ViewPagerSettings,
    ToolbarButtonsClickListener,
    OnBackPresserListener {

    val task: Task? by unsafeLazy {
        arguments?.getParcelable<Task>(TASK_KEY)
    }

    override fun onResume() {
        super.onResume()
        vm.onResume()
    }

    override fun getLayoutId() = R.layout.fragment_task

    override fun getPageNumber() = PAGE_NUMBER

    override fun getViewModel(): TaskViewModel {
        provideViewModel(TaskViewModel::class.java).let { vm ->
            getAppComponent()?.inject(vm)
            if (task != null) {
                vm.task.value = task
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
        topToolbarUiModel.description.value = getString(R.string.task_settings_title)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.cleanAll()

        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.next)

        connectLiveData(vm.nextEnabled, bottomToolbarUiModel.uiModelButton5.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_5 -> vm.onNextClick()
        }
    }

    override fun countTab() = TaskPage.values().size

    override fun getTextTitle(position: Int): String {
        return when(TaskPage.values()[position]) {
            TaskPage.STATUS -> getString(R.string.task_settings_status_title)
            TaskPage.TASK_TYPE -> getString(R.string.task_settings_task_type_tab_title)
            TaskPage.PROPERTIES -> getString(R.string.task_settings_properties_tab_title)
            TaskPage.COMMENTS -> getString(R.string.task_settings_comments_tab_title)
        }
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return when (TaskPage.values()[position]) {
            TaskPage.STATUS -> {
                DataBindingUtil.inflate<LayoutTaskStatusBinding>(
                    LayoutInflater.from(context),
                    R.layout.layout_task_status,
                    container,
                    false
                ).also { layoutBinding ->
                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner
                }.root
            }
            TaskPage.TASK_TYPE -> {
                DataBindingUtil.inflate<LayoutTaskTypeTabBinding>(
                    LayoutInflater.from(context),
                    R.layout.layout_task_type_tab,
                    container,
                    false
                ).also { layoutBinding ->
                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner
                }.root
            }
            TaskPage.PROPERTIES -> {
                DataBindingUtil.inflate<LayoutTaskPropertiesTabBinding>(
                    LayoutInflater.from(context),
                    R.layout.layout_task_properties_tab,
                    container,
                    false
                ).also { layoutBinding ->
                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner
                }.root
            }
            TaskPage.COMMENTS -> {
                DataBindingUtil.inflate<LayoutTaskCommentsTabBinding>(
                    LayoutInflater.from(context),
                    R.layout.layout_task_comments_tab,
                    container,
                    false
                ).also { layoutBinding ->
                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner
                }.root
            }
        }
    }

    override fun onBackPressed(): Boolean {
        vm.onBackPressed()
        return false
    }

    companion object {
        private const val PAGE_NUMBER = "13/05"
        private const val TASK_KEY = "TASK_KEY"

        fun newInstance(task: Task?): TaskFragment {
            return TaskFragment().apply {
                arguments = bundleOf(
                        TASK_KEY to task
                )
            }
        }
    }
}