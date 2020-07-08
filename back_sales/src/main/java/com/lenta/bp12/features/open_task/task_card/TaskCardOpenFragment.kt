package com.lenta.bp12.features.open_task.task_card

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.lenta.bp12.R
import com.lenta.bp12.databinding.FragmentTaskCardOpenBinding
import com.lenta.bp12.databinding.LayoutTaskCardOpenCommentBinding
import com.lenta.bp12.databinding.LayoutTaskCardOpenTypeBinding
import com.lenta.bp12.platform.extention.getAppComponent
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.TabIndicatorColor
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.setIndicatorForTab

class TaskCardOpenFragment : CoreFragment<FragmentTaskCardOpenBinding, TaskCardOpenViewModel>(),
        ToolbarButtonsClickListener, ViewPagerSettings, OnBackPresserListener {

    override fun getLayoutId(): Int = R.layout.fragment_task_card_open

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix("8")

    override fun getViewModel(): TaskCardOpenViewModel {
        provideViewModel(TaskCardOpenViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.title
        topToolbarUiModel.description.value = getString(R.string.task_card)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.next)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_5 -> vm.onClickNext()
        }
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return when (position) {
            0 -> initTaskCardType(container)
            1 -> initTaskCardComment(container)
            else -> View(context)
        }
    }

    private fun initTaskCardType(container: ViewGroup): View {
        DataBindingUtil.inflate<LayoutTaskCardOpenTypeBinding>(LayoutInflater.from(container.context),
                R.layout.layout_task_card_open_type,
                container,
                false).let { layoutBinding ->

            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner

            return layoutBinding.root
        }
    }

    private fun initTaskCardComment(container: ViewGroup): View {
        DataBindingUtil.inflate<LayoutTaskCardOpenCommentBinding>(LayoutInflater.from(container.context),
                R.layout.layout_task_card_open_comment,
                container,
                false).let { layoutBinding ->

            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner

            return layoutBinding.root
        }
    }

    override fun getTextTitle(position: Int): String {
        return when (position) {
            0 -> getString(R.string.task_type)
            1 -> getString(R.string.comment)
            else -> throw IllegalArgumentException("Wrong pager position!")
        }
    }

    override fun countTab(): Int {
        return 2
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }

    override fun onResume() {
        super.onResume()

        viewLifecycleOwner.apply {
            vm.isExistComment.observe(this, Observer { isExistComment ->
                if (isExistComment) {
                    setIndicatorForTab(binding?.tabStrip, 1, TabIndicatorColor.YELLOW)
                }
            })
        }
    }

    override fun onBackPressed(): Boolean {
        vm.onBackPressed()
        return false
    }

}
