package com.lenta.bp12.features.create_task.task_card

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.lenta.bp12.R
import com.lenta.bp12.databinding.*
import com.lenta.bp12.platform.extention.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class TaskCardCreateFragment : CoreFragment<FragmentTaskCardCreateBinding, TaskCardCreateViewModel>(),
        ToolbarButtonsClickListener, ViewPagerSettings {

    override fun getLayoutId(): Int = R.layout.fragment_task_card_create

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix(SCREEN_NUMBER)

    override fun getViewModel(): TaskCardCreateViewModel {
        provideViewModel(TaskCardCreateViewModel::class.java).let {
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
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.next, enabled = false)

        connectLiveData(vm.nextEnabled, getBottomToolBarUIModel()!!.uiModelButton5.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_5 -> vm.onClickNext()
        }
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return when (position) {
            TAB_TASK_TYPE -> initTaskCardType(container)
            TAB_COMMENT -> initTaskCardComment(container)
            else -> View(context)
        }
    }

    private fun initTaskCardType(container: ViewGroup): View {
        DataBindingUtil.inflate<LayoutTaskCardCreateTypeBinding>(LayoutInflater.from(container.context),
                R.layout.layout_task_card_create_type,
                container,
                false).let { layoutBinding ->

            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner

            return layoutBinding.root
        }
    }

    private fun initTaskCardComment(container: ViewGroup): View {
        DataBindingUtil.inflate<LayoutTaskCardCreateCommentBinding>(LayoutInflater.from(container.context),
                R.layout.layout_task_card_create_comment,
                container,
                false).let { layoutBinding ->

            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner

            return layoutBinding.root
        }
    }

    override fun getTextTitle(position: Int): String {
        return when (position) {
            TAB_TASK_TYPE -> getString(R.string.task_type)
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

    companion object {
        const val SCREEN_NUMBER = "8"

        private const val TABS = 2
        private const val TAB_TASK_TYPE = 0
        private const val TAB_COMMENT = 1
    }

}
