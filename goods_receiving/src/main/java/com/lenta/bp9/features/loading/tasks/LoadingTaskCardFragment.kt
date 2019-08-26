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

    private var mode: TaskCardLoadingMode = TaskCardLoadingMode.None
    private var taskNumber: String = ""

    override fun getPageNumber(): String? {
        return generateScreenNumberFromPostfix("98")
    }

    override fun getViewModel(): CoreLoadingViewModel {
        provideViewModel(LoadingTaskCardViewModel::class.java).let {
            getAppComponent()?.inject(it)
            it.taskNumber = taskNumber
            it.mode = mode
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.tasks_loading)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.visibility.value = false
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    override fun onToolbarButtonClick(view: View) {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.title.value = getString(R.string.tasks_loading)
    }

    companion object {
        fun create(taskNumber: String, mode: TaskCardLoadingMode): LoadingTaskCardFragment {
            LoadingTaskCardFragment().let {
                it.taskNumber = taskNumber
                it.mode = mode
                return it
            }
        }
    }
}