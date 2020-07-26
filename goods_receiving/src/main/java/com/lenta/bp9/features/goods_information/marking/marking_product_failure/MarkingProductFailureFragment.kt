package com.lenta.bp9.features.goods_information.marking.marking_product_failure

import android.view.View
import com.lenta.bp9.R
import com.lenta.bp9.databinding.FragmentMarkingProductFailureBinding
import com.lenta.bp9.features.goods_information.excise_alco_receiving.excise_alco_box_acc.excise_alco_box_product_failure.ExciseAlcoBoxProductFailureFragment
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

class MarkingProductFailureFragment : CoreFragment<FragmentMarkingProductFailureBinding, MarkingProductFailureViewModel>(),
        ToolbarButtonsClickListener {

    private var productInfo by state<TaskProductInfo?>(null)

    override fun getLayoutId(): Int = R.layout.fragment_marking_product_failure

    override fun getPageNumber(): String = PAGE_NUMBER

    override fun getViewModel(): MarkingProductFailureViewModel {
        provideViewModel(MarkingProductFailureViewModel::class.java).let {vm ->
            getAppComponent()?.inject(vm)
            vm.productInfo.value = productInfo
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = "${vm.productInfo.value?.getMaterialLastSix().orEmpty()} ${vm.productInfo.value?.description.orEmpty()}"
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

    companion object {
        private const val PAGE_NUMBER = "09/69"
        fun newInstance(productInfo: TaskProductInfo): MarkingProductFailureFragment {
            MarkingProductFailureFragment().let {
                it.productInfo = productInfo
                return it
            }
        }
    }

}
