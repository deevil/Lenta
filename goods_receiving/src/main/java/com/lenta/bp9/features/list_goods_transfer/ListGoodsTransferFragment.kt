package com.lenta.bp9.features.list_goods_transfer

import android.os.Bundle
import android.view.View
import com.lenta.bp9.BR
import com.lenta.bp9.R
import com.lenta.bp9.databinding.FragmentListGoodsTransferBinding
import com.lenta.bp9.databinding.ItemTileListGoodsTransferBinding
import com.lenta.bp9.model.task.TaskSectionInfo
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

class ListGoodsTransferFragment : CoreFragment<FragmentListGoodsTransferBinding, ListGoodsTransferViewModel>(),
        ToolbarButtonsClickListener {

    companion object {
        fun create(sectionInfo: TaskSectionInfo): ListGoodsTransferFragment {
            ListGoodsTransferFragment().let {
                it.sectionInfo = sectionInfo
                return it
            }
        }
    }

    private var sectionInfo by state<TaskSectionInfo?>(null)

    override fun getLayoutId(): Int = R.layout.fragment_list_goods_transfer

    override fun getPageNumber(): String = "09/73"

    override fun getViewModel(): ListGoodsTransferViewModel {
        provideViewModel(ListGoodsTransferViewModel::class.java).let {vm ->
            getAppComponent()?.inject(vm)
            vm.sectionInfo.value = sectionInfo
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
        topToolbarUiModel.description.value = getString(R.string.section) + " " + vm.getDescription()
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.next)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_5 -> vm.onClickNext()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRvConfig()
    }

    private fun initRvConfig() {
        binding?.let { layoutBinding ->
            layoutBinding.rvConfig = oldInitRecycleAdapterDataBinding<ItemTileListGoodsTransferBinding>(
                    layoutId = R.layout.item_tile_list_goods_transfer,
                    itemId = BR.item
            )

            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner
        }
    }

}
