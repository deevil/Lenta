package com.lenta.bp10.features.job_card

import android.os.Bundle
import android.view.View
import com.lenta.bp10.R
import com.lenta.bp10.databinding.FragmentJobCardBinding
import com.lenta.bp10.platform.extentions.getAppComponent
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.provideViewModel

class JobCardFragment : CoreFragment<FragmentJobCardBinding, JobCardViewModel>(), ToolbarButtonsClickListener, OnBackPresserListener {
    override fun getLayoutId() = R.layout.fragment_job_card

    override fun getPageNumber() = "10/05"

    override fun getViewModel(): JobCardViewModel {
        provideViewModel(JobCardViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = "${getString(R.string.tk)} - ${vm.getMarket()}"
        topToolbarUiModel.description.value = getString(R.string.job_card)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.next)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getBottomToolBarUIModel()?.let { bottomToolbarUiModel ->
            connectLiveData(vm.selectedStorePosition, bottomToolbarUiModel.uiModelButton5.requestFocus)
            connectLiveData(vm.enabledNextButton, bottomToolbarUiModel.uiModelButton5.enabled)
        }
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_5 -> vm.onClickNext()
        }
    }

    override fun onBackPressed(): Boolean {
        vm.onClickBack()
        return false
    }

    override fun onFragmentResult(arguments: Bundle) {
        super.onFragmentResult(arguments)
        vm.onConfirmRemoving()
    }


}