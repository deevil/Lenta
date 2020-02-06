package com.lenta.inventory.features.loading.tasks

import android.os.Bundle
import android.view.View
import com.lenta.inventory.R
import com.lenta.inventory.models.RecountType
import com.lenta.inventory.platform.extentions.getAppComponent
import com.lenta.inventory.requests.network.TasksItem
import com.lenta.shared.features.loading.CoreLoadingFragment
import com.lenta.shared.features.loading.CoreLoadingViewModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

class LoadingTaskContentFragment : CoreLoadingFragment() {

    private var taskInfo: TasksItem? by state(null)

    private var recountType: RecountType? by state(null)

    override fun getPageNumber(): String? {
        return generateScreenNumberFromPostfix("98")
    }

    override fun getViewModel(): CoreLoadingViewModel {
        provideViewModel(LoadingTaskContentViewModel::class.java).let {
            getAppComponent()?.inject(it)
            it.taskInfo = taskInfo!!
            it.recountType = recountType!!
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.job_card)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.hide()
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    override fun onToolbarButtonClick(view: View) {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.title.value = getString(R.string.task_content_loading)
    }

    companion object {
        fun create(taskInfo: TasksItem, recountType: RecountType): LoadingTaskContentFragment {
            LoadingTaskContentFragment().let {
                it.taskInfo = taskInfo
                it.recountType = recountType
                return it
            }
        }
    }
}