package com.lenta.bp14.features.work_list.storage_z_parts

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import com.lenta.bp14.BR
import com.lenta.bp14.R
import com.lenta.bp14.databinding.FragmentStorageZPartsBinding
import com.lenta.bp14.databinding.ItemStorageZPartBinding
import com.lenta.bp14.di.WorkListComponent
import com.lenta.bp14.platform.extentions.getHelperComponent
import com.lenta.shared.di.CoreInjectHelper
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel

class StorageZPartsFragment : CoreFragment<FragmentStorageZPartsBinding, StorageZPartsViewModel>() {
    override fun getLayoutId(): Int = R.layout.fragment_storage_z_parts

    override fun getPageNumber(): String = PAGE_NUMBER

    override fun getViewModel(): StorageZPartsViewModel = provideViewModel(StorageZPartsViewModel::class.java).apply {
        getHelperComponent<WorkListComponent>()?.inject(this)
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = vm.title
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            vm.storage = it.getString(SELECTED_STORAGE, "")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.rvConfig = initRecycleAdapterDataBinding<ItemStorageZPartBinding>(
                layoutId = R.layout.item_storage_z_part,
                itemId = BR.stock
        )
    }

    companion object {
        private const val PAGE_NUMBER = "14/19"
        private const val SELECTED_STORAGE = "SELECTED_STORAGE"

        fun newInstance(storage: String): StorageZPartsFragment = StorageZPartsFragment().apply {
            arguments = bundleOf(Pair(SELECTED_STORAGE, storage))
        }
    }
}