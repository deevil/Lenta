package com.lenta.bp15.features.task_card

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.lenta.bp15.R
import com.lenta.bp15.databinding.FragmentTaskCardBinding
import com.lenta.bp15.databinding.LayoutTaskCardCommentBinding
import com.lenta.bp15.databinding.LayoutTaskCardTypeBinding
import com.lenta.bp15.platform.extention.getAppComponent
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.TabIndicatorColor
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.setIndicatorForTab

class TaskCardFragment : CoreFragment<FragmentTaskCardBinding, TaskCardViewModel>(),
        ToolbarButtonsClickListener, ViewPagerSettings, OnBackPresserListener {

    override fun getLayoutId(): Int = R.layout.fragment_task_card

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix(SCREEN_NUMBER)

    override fun getViewModel(): TaskCardViewModel {
        provideViewModel(TaskCardViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.task_card)

        connectLiveData(vm.title, topToolbarUiModel.title)
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
            TAB_TYPE -> initTaskCardType(container)
            TAB_COMMENT -> initTaskCardComment(container)
            else -> View(context)
        }
    }

    private fun initTaskCardType(container: ViewGroup): View {
        DataBindingUtil.inflate<LayoutTaskCardTypeBinding>(LayoutInflater.from(container.context),
                R.layout.layout_task_card_type,
                container,
                false).let { layoutBinding ->

            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner

            return layoutBinding.root
        }
    }

    private fun initTaskCardComment(container: ViewGroup): View {
        DataBindingUtil.inflate<LayoutTaskCardCommentBinding>(LayoutInflater.from(container.context),
                R.layout.layout_task_card_comment,
                container,
                false).let { layoutBinding ->

            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner

            return layoutBinding.root
        }
    }

    override fun getTextTitle(position: Int): String {
        return when (position) {
            TAB_TYPE -> getString(R.string.task_type)
            TAB_COMMENT -> getString(R.string.comment)
            else -> throw IllegalArgumentException("Wrong pager position!")
        }
    }

    override fun countTab(): Int {
        return TABS
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

    companion object {
        const val SCREEN_NUMBER = "18"

        private const val TABS = 2
        private const val TAB_TYPE = 0
        private const val TAB_COMMENT = 1
    }

}