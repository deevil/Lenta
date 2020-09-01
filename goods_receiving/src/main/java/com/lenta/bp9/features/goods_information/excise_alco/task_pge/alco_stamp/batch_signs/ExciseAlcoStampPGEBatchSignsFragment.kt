package com.lenta.bp9.features.goods_information.excise_alco.task_pge.alco_stamp.batch_signs

import android.os.Bundle
import android.view.View
import com.lenta.bp9.R
import com.lenta.bp9.databinding.FragmentExciseAlcoStampPgeBatchSignsBinding
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.DateInputMask
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

class ExciseAlcoStampPGEBatchSignsFragment : CoreFragment<FragmentExciseAlcoStampPgeBatchSignsBinding, ExciseAlcoStampPGEBatchSignsViewModel>(),
        ToolbarButtonsClickListener {

    companion object {
        fun create(title: String): ExciseAlcoStampPGEBatchSignsFragment {
            ExciseAlcoStampPGEBatchSignsFragment().let {
                it.title = title
                return it
            }
        }
    }

    private var title by state<String?>(null)

    override fun getLayoutId(): Int = R.layout.fragment_excise_alco_stamp_pge_batch_signs

    override fun getPageNumber(): String = "09/97"

    override fun getViewModel(): ExciseAlcoStampPGEBatchSignsViewModel {
        provideViewModel(ExciseAlcoStampPGEBatchSignsViewModel::class.java).let {vm ->
            getAppComponent()?.inject(vm)
            return vm
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        DateInputMask(binding?.etBottlingDate!!).listen()
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = title
        topToolbarUiModel.description.value = getString(R.string.goods_info)
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


}
