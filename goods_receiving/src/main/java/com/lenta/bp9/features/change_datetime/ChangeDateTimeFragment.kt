package com.lenta.bp9.features.change_datetime

import android.view.View
import com.lenta.bp9.R
import com.lenta.bp9.databinding.FragmentChangeDateTimeBinding
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumber
import com.lenta.shared.utilities.extentions.provideViewModel

class ChangeDateTimeFragment : CoreFragment<FragmentChangeDateTimeBinding, ChangeDateTimeViewModel>(), ToolbarButtonsClickListener {

    private var mode: ChangeDateTimeMode = ChangeDateTimeMode.None

    override fun getLayoutId(): Int = R.layout.fragment_change_date_time

    override fun getPageNumber() = generateScreenNumber()

    override fun getViewModel(): ChangeDateTimeViewModel {
        provideViewModel(ChangeDateTimeViewModel::class.java).let {
            getAppComponent()?.inject(it)
            it.mode = mode
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.taskCaption
        topToolbarUiModel.description.value = vm.screenDescription
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply)
    }

    override fun onResume() {
        super.onResume()
        vm.onResume()
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_5 -> vm.onClickApply()
        }
    }

    companion object {
        fun create(mode: ChangeDateTimeMode): ChangeDateTimeFragment {
            val fragment = ChangeDateTimeFragment()
            fragment.mode = mode
            return fragment
        }
    }

}
