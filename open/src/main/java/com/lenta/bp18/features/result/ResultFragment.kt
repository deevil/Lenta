package com.lenta.bp18.features.result

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lenta.bp18.R
import com.lenta.bp18.databinding.FragmentResultBinding
import com.lenta.bp18.platform.extention.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.getDeviceId
import com.lenta.shared.utilities.extentions.provideViewModel

class ResultFragment : CoreFragment<FragmentResultBinding, ResultViewModel>(), ToolbarButtonsClickListener {
    override fun getLayoutId(): Int = R.layout.fragment_result

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix(SCREEN_NUMBER)

    override fun getViewModel(): ResultViewModel {
        provideViewModel(ResultViewModel::class.java).let {
            getAppComponent()?.inject(it)
            it.deviceIp.value = context!!.getDeviceId()
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.good_card)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {

        /*bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply)*/

        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.next)

    }

    override fun onToolbarButtonClick(view: View) {
        when(view.id){
          //  R.id.b_5 -> vm.onClickNext()
        }
    }

    companion object{
        const val SCREEN_NUMBER = "101"
    }

}