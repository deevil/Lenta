package com.lenta.bp12.features.task_composition

import com.lenta.bp12.R
import com.lenta.bp12.databinding.FragmentTaskCompositionBinding
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel
import android.os.Bundle
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import android.view.ViewGroup
import android.view.View
import com.lenta.bp12.platform.extention.getAppComponent

class TaskCompositionFragment : CoreFragment<FragmentTaskCompositionBinding, TaskCompositionViewModel>(), ViewPagerSettings {

    override fun getLayoutId(): Int = R.layout.fragment_task_composition

    override fun getPageNumber(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getViewModel(): TaskCompositionViewModel {
        provideViewModel(TaskCompositionViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
