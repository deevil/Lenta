package com.lenta.inventory.features.loading.store_place_lock

import android.os.Bundle
import android.view.View
import com.lenta.inventory.R
import com.lenta.inventory.features.task_list.TaskItemVm
import com.lenta.inventory.models.StorePlaceLockMode
import com.lenta.inventory.models.task.TaskStorePlaceInfo
import com.lenta.inventory.platform.extentions.getAppComponent
import com.lenta.shared.features.loading.CoreLoadingFragment
import com.lenta.shared.features.loading.CoreLoadingViewModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class LoadingStorePlaceLockFragment: CoreLoadingFragment() {

    private var storePlaceInfo: TaskStorePlaceInfo? = null
    private var mode: StorePlaceLockMode = StorePlaceLockMode.None
    private var taskInfo: TaskItemVm? = null

    override fun getPageNumber(): String? {
        return generateScreenNumberFromPostfix("98")
    }

    override fun getViewModel(): CoreLoadingViewModel {
        provideViewModel(LoadingStorePlaceLockViewModel::class.java).let {
            getAppComponent()?.inject(it)
            it.taskInfo = taskInfo
            it.mode = mode
            it.storePlaceInfo = storePlaceInfo
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = taskInfo?.title
        topToolbarUiModel.description.value = getString(R.string.data_loading)
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
        vm.title.value = when (mode) {
            StorePlaceLockMode.Lock -> getString(R.string.store_place_locking)
            StorePlaceLockMode.Unlock -> getString(R.string.store_place_unlocking)
            else -> ""
        }
    }

    companion object {
        fun create(taskInfo: TaskItemVm, lockMode: StorePlaceLockMode, storePlaceInfo: TaskStorePlaceInfo): LoadingStorePlaceLockFragment {
            LoadingStorePlaceLockFragment().let {
                it.taskInfo = taskInfo
                it.mode = lockMode
                it.storePlaceInfo = storePlaceInfo
                return it
            }
        }
    }
}