package com.lenta.movement.features.task.goods.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.lenta.movement.R
import com.lenta.movement.databinding.FragmentTaskGoodsDetailsBinding
import com.lenta.movement.databinding.LayoutTaskGoodsDetailsBucketsTabBinding
import com.lenta.movement.models.ProductInfo
import com.lenta.movement.platform.extensions.getAppComponent
import com.lenta.movement.view.simpleListRecyclerViewConfig
import com.lenta.shared.keys.KeyCode
import com.lenta.shared.keys.OnKeyDownListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.databinding.RecyclerViewKeyHandler
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

class TaskGoodsDetailsFragment :
    CoreFragment<FragmentTaskGoodsDetailsBinding, TaskGoodsDetailsViewModel>(),
    ViewPagerSettings,
    ToolbarButtonsClickListener,
    OnKeyDownListener {

    private var productInfo: ProductInfo? by state(null)

    companion object {
        fun newInstance(productInfo: ProductInfo): TaskGoodsDetailsFragment {
            return TaskGoodsDetailsFragment().apply {
                this.productInfo = productInfo
            }
        }
    }

    private var basketRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null

    override fun getLayoutId() = R.layout.fragment_task_goods_details

    override fun getPageNumber() = "13/06"

    override fun getViewModel(): TaskGoodsDetailsViewModel {
        return provideViewModel(TaskGoodsDetailsViewModel::class.java).also {
            it.product = productInfo
            getAppComponent()?.inject(it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
        topToolbarUiModel.description.value = getString(R.string.details_of_goods)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.delete)

        connectLiveData(vm.deleteEnabled, bottomToolbarUiModel.uiModelButton3.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_3 -> vm.onDeleteClick()
        }
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return when (vm.getAvailablePages()[position]) {
            TaskGoodsDetailsPage.BASKETS -> {
                return DataBindingUtil.inflate<LayoutTaskGoodsDetailsBucketsTabBinding>(
                    LayoutInflater.from(context),
                    R.layout.layout_task_goods_details_buckets_tab,
                    container,
                    false
                ).also { layoutBinding ->
                    layoutBinding?.rvConfig = simpleListRecyclerViewConfig(
                        recyclerView = layoutBinding?.basketRecyclerView,
                        selectionItemsHelper = vm.basketSelectionHelper,
                        recyclerViewKeyHandler = basketRecyclerViewKeyHandler
                    )

                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner

                    basketRecyclerViewKeyHandler = RecyclerViewKeyHandler(
                        layoutBinding?.basketRecyclerView!!,
                        vm.basketList,
                        binding?.lifecycleOwner!!,
                        basketRecyclerViewKeyHandler?.posInfo?.value
                    )
                }.root
            }
            TaskGoodsDetailsPage.BOXES -> View(context) // TODO
        }
    }

    override fun getTextTitle(position: Int): String {
        return when (vm.getAvailablePages()[position]) {
            TaskGoodsDetailsPage.BASKETS -> getString(R.string.task_goods_buckets_tab_title)
            TaskGoodsDetailsPage.BOXES -> getString(R.string.boxes)
        }
    }

    override fun countTab() = vm.getAvailablePages().size

    override fun onKeyDown(keyCode: KeyCode): Boolean {
        when (vm.currentPage.value) {
            TaskGoodsDetailsPage.BASKETS -> basketRecyclerViewKeyHandler
            else -> null
        }?.let {
            if (!it.onKeyDown(keyCode)) {
                return false
            }
            return true
        }
        return false
    }
}