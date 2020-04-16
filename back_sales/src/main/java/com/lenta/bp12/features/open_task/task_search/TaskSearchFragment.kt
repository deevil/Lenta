package com.lenta.bp12.features.open_task.task_search

import android.view.View
import com.lenta.bp12.R
import com.lenta.bp12.databinding.FragmentTaskSearchBinding
import com.lenta.bp12.platform.extention.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class TaskSearchFragment : CoreFragment<FragmentTaskSearchBinding, TaskSearchViewModel>(),
        ToolbarButtonsClickListener {

    override fun getLayoutId(): Int = R.layout.fragment_task_search

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix("70")

    override fun getViewModel(): TaskSearchViewModel {
        provideViewModel(TaskSearchViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.title
        topToolbarUiModel.description.value = getString(R.string.filter_for_search)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.search)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_5 -> vm.onClickSearch()
        }
    }

}
