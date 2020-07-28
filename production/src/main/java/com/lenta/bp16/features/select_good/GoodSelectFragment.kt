package com.lenta.bp16.features.select_good

import android.view.View
import androidx.lifecycle.observe
import com.lenta.bp16.R
import com.lenta.bp16.databinding.FragmentGoodSelectBinding
import com.lenta.bp16.platform.Constants
import com.lenta.bp16.platform.extention.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.getDeviceId
import com.lenta.shared.utilities.extentions.provideViewModel

class GoodSelectFragment : CoreFragment<FragmentGoodSelectBinding, GoodSelectViewModel>(), ToolbarButtonsClickListener {
    override fun getLayoutId(): Int = R.layout.fragment_good_select

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix(SCREEN_NUMBER)

    override fun getViewModel(): GoodSelectViewModel {
        provideViewModel(GoodSelectViewModel::class.java).let{
            getAppComponent()?.inject(it)
            it.deviceIp.value = context!!.getDeviceId()
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.good_select)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.menu)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.next, enabled = false)

        viewLifecycleOwner.apply {
            vm.enteredEanField.observe(viewLifecycleOwner) {
                bottomToolbarUiModel.uiModelButton5.requestFocus()
            }
        }

        connectLiveData(vm.enabledNextButton, bottomToolbarUiModel.uiModelButton5.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when(view.id){
            R.id.b_1 -> vm.onClickMenu()
            R.id.b_5 -> vm.onClickNext()
        }
    }

    companion object{
        const val SCREEN_NUMBER = Constants.SELECT_GOODS_FRAGMENT
    }

}