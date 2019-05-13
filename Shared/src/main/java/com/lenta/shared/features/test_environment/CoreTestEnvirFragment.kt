package com.lenta.shared.features.test_environment

import android.view.View
import com.lenta.shared.R
import com.lenta.shared.databinding.FragmentTestEnvironmentBinding
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel

abstract class CoreTestEnvirFragment : CoreFragment<FragmentTestEnvironmentBinding, CoreTestEnvirViewModel>(), OnBackPresserListener, ToolbarButtonsClickListener {
    override fun getLayoutId(): Int = R.layout.fragment_test_environment

    override fun onBackPressed(): Boolean {
        vm.onBackPressed()
        return true
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_1 -> vm.onClickBack()
            R.id.b_5 -> vm.onClickGoOver()
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = resources.getString(R.string.test_environment)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.cleanAll()
        bottomToolbarUiModel.uiModelButton1.let { buttonUiModel -> buttonUiModel.show(ButtonDecorationInfo.back) }
        //todo исправить на кнопку "ПЕРЕЙТИ"
        bottomToolbarUiModel.uiModelButton5.let { buttonUiModel -> buttonUiModel.show(ButtonDecorationInfo.next) }
    }
}