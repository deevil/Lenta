package com.lenta.movement.features.task.goods.info

import android.view.View
import com.lenta.movement.R
import com.lenta.movement.databinding.FragmentTaskGoodsInfoBinding
import com.lenta.movement.models.ProductInfo
import com.lenta.movement.platform.extensions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

class TaskGoodsInfoFragment : CoreFragment<FragmentTaskGoodsInfoBinding, TaskGoodsInfoViewModel>(),
    ToolbarButtonsClickListener, OnScanResultListener {

    private var productInfo: ProductInfo? by state(null)

    override fun getLayoutId() = R.layout.fragment_task_goods_info

    override fun getPageNumber() = PAGE_NUMBER

    override fun getViewModel(): TaskGoodsInfoViewModel {
        return provideViewModel(TaskGoodsInfoViewModel::class.java).also { vm ->
            productInfo?.let {
                vm.productInfo.value = it
            }
            getAppComponent()?.inject(vm)

            vm.quantityList.value = listOf(resources.getString(R.string.quantity))
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
        topToolbarUiModel.description.value = getString(R.string.task_goods_info_title)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.cleanAll()

        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.detail)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply)

        connectLiveData(vm.applyEnabled, bottomToolbarUiModel.uiModelButton5.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_3 -> vm.onDetailsClick()
            R.id.b_5 -> vm.onApplyClick()
        }
    }

    companion object {
        private const val PAGE_NUMBER = "13/06"

        fun newInstance(productInfo: ProductInfo): TaskGoodsInfoFragment {
            return TaskGoodsInfoFragment().apply {
                this.productInfo = productInfo
            }
        }
    }

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }
}