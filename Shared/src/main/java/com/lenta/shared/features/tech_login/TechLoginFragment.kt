package com.lenta.shared.features.tech_login

import android.view.View
import com.lenta.shared.R
import com.lenta.shared.databinding.FragmentTechLoginBinding
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumber
import com.lenta.shared.utilities.extentions.provideViewModel

class TechLoginFragment : CoreFragment<FragmentTechLoginBinding, TechLoginViewModel>(), ToolbarButtonsClickListener {

    override fun getLayoutId(): Int = R.layout.fragment_tech_login



    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_5 -> vm.onClickApply()
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = resources.getString(R.string.tech_login)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.cleanAll()
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply)
        connectLiveData(vm.applyButtonEnabled, bottomToolbarUiModel.uiModelButton5.enabled)
    }

    override fun getPageNumber(): String =  generateScreenNumber()

    override fun getViewModel(): TechLoginViewModel {
        provideViewModel(TechLoginViewModel::class.java).let {
            coreComponent.inject(it)
            return it
        }
    }


}
