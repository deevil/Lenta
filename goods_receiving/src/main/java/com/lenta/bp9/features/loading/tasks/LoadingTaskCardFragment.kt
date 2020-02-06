package com.lenta.bp9.features.loading.tasks

import android.os.Bundle
import android.view.View
import com.lenta.bp9.R
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.features.loading.CoreLoadingFragment
import com.lenta.shared.features.loading.CoreLoadingViewModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class LoadingTaskCardFragment : CoreLoadingFragment() {

    companion object {
        fun create(taskNumber: String, mode: TaskCardMode, loadFullData: Boolean): LoadingTaskCardFragment {
            LoadingTaskCardFragment().let {
                it.taskNumber = taskNumber
                it.mode = mode
                it.loadFullData = loadFullData
                return it
            }
        }
    }

    private var mode: TaskCardMode = TaskCardMode.None
    private var taskNumber: String = ""
    private var loadFullData: Boolean = false

    override fun getPageNumber(): String? {
        return generateScreenNumberFromPostfix("98")
    }

    override fun getViewModel(): CoreLoadingViewModel {
        provideViewModel(LoadingTaskCardViewModel::class.java).let {
            getAppComponent()?.inject(it)
            it.taskNumber = taskNumber
            it.mode = mode
            it.loadFullData = loadFullData
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.task_card)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.cleanAll(false)
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    override fun onToolbarButtonClick(view: View) {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.title.value = getString(R.string.task_loading)
    }
}