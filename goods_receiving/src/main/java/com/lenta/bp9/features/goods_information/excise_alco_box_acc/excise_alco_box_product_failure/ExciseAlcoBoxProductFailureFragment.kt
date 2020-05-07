package com.lenta.bp9.features.goods_information.excise_alco_box_acc.excise_alco_box_product_failure

import android.view.View
import com.lenta.bp9.R
import com.lenta.bp9.databinding.FragmentExciseAlcoBoxProductFailureBinding
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

class ExciseAlcoBoxProductFailureFragment : CoreFragment<FragmentExciseAlcoBoxProductFailureBinding, ExciseAlcoBoxProductFailureViewModel>(),
        ToolbarButtonsClickListener {

    companion object {
        fun create(productInfo: TaskProductInfo): ExciseAlcoBoxProductFailureFragment {
            ExciseAlcoBoxProductFailureFragment().let {
                it.productInfo = productInfo
                return it
            }
        }
    }

    private var productInfo by state<TaskProductInfo?>(null)

    override fun getLayoutId(): Int = R.layout.fragment_excise_alco_box_product_failure

    override fun getPageNumber(): String = "09/69"

    override fun getViewModel(): ExciseAlcoBoxProductFailureViewModel {
        provideViewModel(ExciseAlcoBoxProductFailureViewModel::class.java).let {vm ->
            getAppComponent()?.inject(vm)
            vm.productInfo.value = productInfo
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = "${vm.productInfo.value?.getMaterialLastSix()} ${vm.productInfo.value?.description}"
        topToolbarUiModel.description.value = getString(R.string.discrepancies_found)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.completeRejection)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.partialFailure)
        connectLiveData(vm.enabledPartialFailureBtn, bottomToolbarUiModel.uiModelButton5.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_3 -> vm.onClickCompleteRejection()
            R.id.b_5 -> vm.onClickPartialFailure()
        }
    }


}
