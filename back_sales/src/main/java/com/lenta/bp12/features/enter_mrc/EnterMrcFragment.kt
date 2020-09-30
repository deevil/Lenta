package com.lenta.bp12.features.enter_mrc

import android.view.View
import com.lenta.bp12.R
import com.lenta.bp12.databinding.FragmentEnterMrcBinding
import com.lenta.bp12.model.WorkType
import com.lenta.bp12.platform.extention.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumber
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

class EnterMrcFragment : CoreFragment<FragmentEnterMrcBinding, EnterMrcViewModel>(), ToolbarButtonsClickListener {

    private var workType by state(WorkType.CREATE)

    private var codeConfirmForRight by state<Int?>(null)

    override fun getLayoutId(): Int = R.layout.fragment_enter_mrc

    override fun getPageNumber(): String = generateScreenNumber()

    override fun getViewModel(): EnterMrcViewModel {
        return provideViewModel(EnterMrcViewModel::class.java).also { vm ->
            getAppComponent()?.inject(vm)
            vm.workType = workType
            vm.codeConfirmForRight = codeConfirmForRight
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.goods_info)
        connectLiveData(vm.title, topToolbarUiModel.title)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.next)

        connectLiveData(vm.enabledNextBtn, bottomToolbarUiModel.uiModelButton5.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        if (view.id == R.id.b_5) {
            vm.onClickNext()
        }
    }

    override fun onResume() {
        super.onResume()
        vm.requestFocus.value = true
    }

    companion object {
        fun newInstance(workType: WorkType, codeConfirmForRight: Int?): EnterMrcFragment {
            return EnterMrcFragment().apply {
                this.workType = workType
                this.codeConfirmForRight = codeConfirmForRight
            }
        }
    }
}
