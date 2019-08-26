package com.lenta.bp9.features.task_card

import com.lenta.bp9.R
import com.lenta.bp9.databinding.FragmentTaskCardBinding
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel
import android.os.Bundle
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import android.view.ViewGroup
import android.view.View
import com.lenta.shared.utilities.extentions.generateScreenNumber

class TaskCardFragment : CoreFragment<FragmentTaskCardBinding, TaskCardViewModel>(), ViewPagerSettings {

    override fun getLayoutId(): Int = R.layout.fragment_task_card

    override fun getPageNumber() = generateScreenNumber()

    override fun getViewModel(): TaskCardViewModel {
        provideViewModel(TaskCardViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {

    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {

    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return View(context)
    }

    override fun getTextTitle(position: Int): String {
        return "Title: $position"
    }

    override fun countTab(): Int {
        return 3
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }


}
