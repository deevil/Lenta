package com.lenta.shared.features.message

import android.view.View
import com.lenta.shared.R
import com.lenta.shared.databinding.LayoutMessageBinding
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.state.state

abstract class CoreMessageFragment : CoreFragment<LayoutMessageBinding, MessageViewModel>(), ToolbarButtonsClickListener, OnBackPresserListener {
    protected var message by state("")
    protected var iconRes by state(0)
    protected var textColor by state<Int?>(null)
    protected var codeConfirmForExit by state<Int?>(null)
    protected var codeConfirmForRight by state<Int?>(null)
    var codeConfirmForButton2 by state<Int?>(null)
    var codeConfirmForButton3 by state<Int?>(null)
    var codeConfirmForButton4 by state<Int?>(null)
    var codeConfirmForLeft by state<Int?>(null)
    protected var pageNumb by state<String?>(null)
    protected var leftButtonDecorationInfo by state(ButtonDecorationInfo.back)
    protected var buttonDecorationInfo2: ButtonDecorationInfo? by state(null)
    protected var buttonDecorationInfo3: ButtonDecorationInfo? by state(null)
    protected var buttonDecorationInfo4: ButtonDecorationInfo? by state(null)
    protected var rightButtonDecorationInfo by state(ButtonDecorationInfo.apply)
    protected var timeAutoExitInMillis by state<Int?>(null)

    override fun getLayoutId(): Int = R.layout.layout_message

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.visibility.value = true
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        if (timeAutoExitInMillis != null) {
            bottomToolbarUiModel.visibility.value = false
            return
        }
        bottomToolbarUiModel.uiModelButton1.show(leftButtonDecorationInfo)
        if (codeConfirmForRight != null) {
            bottomToolbarUiModel.uiModelButton5.show(rightButtonDecorationInfo)
        }

        buttonDecorationInfo2?.let {
            bottomToolbarUiModel.uiModelButton2.show(it)
        }
        buttonDecorationInfo3?.let {
            bottomToolbarUiModel.uiModelButton3.show(it)
        }
        buttonDecorationInfo4?.let {
            bottomToolbarUiModel.uiModelButton4.show(it)
        }
    }

    override fun cleanTopToolbar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.uiModelButton1.visibility.value = false
        topToolbarUiModel.uiModelButton2.visibility.value = false
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_1 -> vm.onClickLeftButton()
            R.id.b_2 -> vm.onClickButton2()
            R.id.b_3 -> vm.onClickButton3()
            R.id.b_4 -> vm.onClickButton4()
            R.id.b_5 -> vm.onClickRightButton()
        }
    }

    override fun getPageNumber(): String? = if (pageNumb != null) generateScreenNumberFromPostfix(pageNumb) else null


    override fun onBackPressed(): Boolean {
        return true
    }

}