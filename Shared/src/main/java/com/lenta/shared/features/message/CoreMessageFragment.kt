package com.lenta.shared.features.message

import android.view.View
import com.lenta.shared.R
import com.lenta.shared.databinding.LayoutMessageBinding
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.state.state

abstract class CoreMessageFragment : CoreFragment<LayoutMessageBinding, MessageViewModel>(), ToolbarButtonsClickListener {
    protected var message by state("")
    protected var iconRes by state(0)
    protected var codeConfirm by state<Int?>(null)
    protected var pageNumb by state("")

    override fun getLayoutId(): Int = R.layout.layout_message

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.visibility.value = true
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        if (codeConfirm != null) {
            bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply)
        }

    }

    override fun cleanTopToolbar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.uiModelButton1.visibility.value = false
        topToolbarUiModel.uiModelButton2.visibility.value = false
    }

    override fun onToolbarButtonClick(view: View) {
        if (view.id == R.id.b_5) {
            vm.onClickApply()
        }
    }

    override fun getPageNumber(): String = pageNumb
}