package com.lenta.shared.features.message

import android.view.View
import com.lenta.shared.R
import com.lenta.shared.databinding.LayoutMessageBinding
import com.lenta.shared.keys.KeyCode
import com.lenta.shared.keys.OnKeyUpListener
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.state.state

abstract class CoreMessageFragment : CoreFragment<LayoutMessageBinding, MessageViewModel>(), ToolbarButtonsClickListener, OnBackPresserListener, OnKeyUpListener {
    protected var message by state("")
    protected var title by state<String?>(null)
    protected var description by state<String?>(null)
    protected var iconRes by state(0)
    protected var textColor by state<Int?>(null)
    protected var codeConfirmForExit by state<Int?>(null)
    protected var codeConfirmForRight by state<Int?>(null)
    protected var codeConfirmForButton2 by state<Int?>(null)
    protected var codeConfirmForButton3 by state<Int?>(null)
    protected var codeConfirmForButton4 by state<Int?>(null)
    protected var codeConfirmForLeft by state<Int?>(null)
    protected var isVisibleLeftButton by state<Boolean>(true)
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

        if (title?.isNotEmpty() == true) topToolbarUiModel.title.value = title
        if (description?.isNotEmpty() == true) topToolbarUiModel.description.value = description
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        if (timeAutoExitInMillis != null) {
            bottomToolbarUiModel.visibility.value = false
            return
        }

        if (isVisibleLeftButton) {
            bottomToolbarUiModel.uiModelButton1.show(leftButtonDecorationInfo)
        }

        if (codeConfirmForRight != null) {
            bottomToolbarUiModel.uiModelButton5.apply {
                show(rightButtonDecorationInfo)
                requestFocus()
            }
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

        if (bottomToolbarUiModel.uiModelButton5.enabled.value == true) {
            bottomToolbarUiModel.uiModelButton5.requestFocus()
        }
    }

    override fun cleanTopToolbar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.cleanAll()
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

    override fun onKeyUp(keyCode: KeyCode): Boolean {
        if (isAllowHandleKeyCode() && keyCode == KeyCode.KEYCODE_ENTER) {
            if (getBottomToolBarUIModel()?.uiModelButton5?.enabled?.value == true) {
                vm.onClickRightButton()
            }
            return true
        }

        return false
    }

}