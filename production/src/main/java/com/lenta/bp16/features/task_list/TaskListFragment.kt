package com.lenta.bp16.features.task_list

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.lenta.bp16.R
import com.lenta.bp16.databinding.FragmentTaskListBinding
import com.lenta.bp16.platform.extention.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class TaskListFragment : CoreFragment<FragmentTaskListBinding, TaskListViewModel>(), ViewPagerSettings {

    override fun getLayoutId(): Int = R.layout.fragment_task_list

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix("5")

    override fun getViewModel(): TaskListViewModel {
        provideViewModel(TaskListViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.work_with_pu_quantity, vm.tasks.value?.size ?: 0)
        topToolbarUiModel.title.value = vm.title
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return View(context)
    }

    override fun getTextTitle(position: Int): String {
        return "Title: $position"
    }

    override fun countTab(): Int {
        return 2
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }

}
