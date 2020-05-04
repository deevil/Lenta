package com.lenta.movement.features.task.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.lenta.movement.R
import com.lenta.movement.databinding.FragmentTaskSettingsBinding
import com.lenta.movement.databinding.LayoutTaskSettingsCommentsTabBinding
import com.lenta.movement.databinding.LayoutTaskSettingsPropertiesTabBinding
import com.lenta.movement.databinding.LayoutTaskSettingsTaskTypeTabBinding
import com.lenta.movement.features.task.settings.pages.TaskSettingsCommentsViewModel
import com.lenta.movement.features.task.settings.pages.TaskSettingsPropertiesViewModel
import com.lenta.movement.features.task.settings.pages.TaskSettingsTaskTypeViewModel
import com.lenta.movement.platform.extensions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.provideViewModel

class TaskSettingsFragment : CoreFragment<FragmentTaskSettingsBinding, TaskSettingsViewModel>(),
    ViewPagerSettings,
    ToolbarButtonsClickListener {

    override fun getLayoutId() = R.layout.fragment_task_settings

    override fun getPageNumber() = "13/05"

    override fun getViewModel(): TaskSettingsViewModel {
        provideViewModel(TaskSettingsViewModel::class.java).let { vm ->
            getAppComponent()?.inject(vm)
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

    override fun countTab() = TaskSettingsPage.values().size

    override fun getTextTitle(position: Int): String {
        return when(TaskSettingsPage.values()[position]) {
            TaskSettingsPage.TASK_TYPE -> getString(R.string.task_settings_task_type_tab_title)
            TaskSettingsPage.PROPERTIES -> getString(R.string.task_settings_properties_tab_title)
            TaskSettingsPage.COMMENTS -> getString(R.string.task_settings_comments_tab_title)
        }
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return when (TaskSettingsPage.values()[position]) {
            TaskSettingsPage.TASK_TYPE -> {
                DataBindingUtil.inflate<LayoutTaskSettingsTaskTypeTabBinding>(
                    LayoutInflater.from(context),
                    R.layout.layout_task_settings_task_type_tab,
                    container,
                    false
                ).also { layoutBinding ->
                    layoutBinding.vm = provideViewModel(TaskSettingsTaskTypeViewModel::class.java).let { vm ->
                        getAppComponent()?.inject(vm)
                        return@let vm
                    }
                    layoutBinding.lifecycleOwner = viewLifecycleOwner.apply {
                        this.lifecycle.addObserver(object : LifecycleObserver {
                            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
                            fun onResume() {
                                layoutBinding.vm?.onResume()
                            }
                        })
                    }
                    layoutBinding.taskNameEditText.addTextChangedListener { text ->
                        layoutBinding.vm?.onTaskNameChanges(text.toString())
                    }
                }.root
            }
            TaskSettingsPage.PROPERTIES -> {
                DataBindingUtil.inflate<LayoutTaskSettingsPropertiesTabBinding>(
                    LayoutInflater.from(context),
                    R.layout.layout_task_settings_properties_tab,
                    container,
                    false
                ).also { layoutBinding ->
                    layoutBinding.vm = provideViewModel(TaskSettingsPropertiesViewModel::class.java).let { vm ->
                        getAppComponent()?.inject(vm)
                        return@let vm
                    }
                    layoutBinding.lifecycleOwner = viewLifecycleOwner.apply {
                        this.lifecycle.addObserver(object : LifecycleObserver {
                            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
                            fun onResume() {
                                layoutBinding.vm?.onResume()
                            }
                        })
                    }
                    layoutBinding.shipmentDateEditText.addTextChangedListener { text ->
                        layoutBinding.vm?.onShipmentDateChange(text.toString())
                    }
                }.root
            }
            TaskSettingsPage.COMMENTS -> {
                DataBindingUtil.inflate<LayoutTaskSettingsCommentsTabBinding>(
                    LayoutInflater.from(context),
                    R.layout.layout_task_settings_comments_tab,
                    container,
                    false
                ).also { layoutBinding ->
                    layoutBinding.vm = provideViewModel(TaskSettingsCommentsViewModel::class.java).let { vm ->
                        getAppComponent()?.inject(vm)
                        return@let vm
                    }
                    layoutBinding.lifecycleOwner = viewLifecycleOwner
                }.root
            }
        }
    }
}