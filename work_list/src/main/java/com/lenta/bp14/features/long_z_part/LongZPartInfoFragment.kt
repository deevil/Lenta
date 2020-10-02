package com.lenta.bp14.features.long_z_part

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import com.lenta.bp14.R
import com.lenta.bp14.databinding.FragmentLongZPartInfoBinding
import com.lenta.bp14.models.ui.ZPartUi
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel

class LongZPartInfoFragment : CoreFragment<FragmentLongZPartInfoBinding, LongZPartInfoViewModel>() {
    override fun getLayoutId(): Int = R.layout.fragment_long_z_part_info

    override fun getPageNumber(): String? = PAGE_NUMBER

    //Не нужно ничего инжектить
    override fun getViewModel(): LongZPartInfoViewModel = ViewModelProvider(this)[LongZPartInfoViewModel::class.java]


    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            vm.initZPart(it.getParcelable(Z_PART))
        }
    }

    companion object {
        private const val PAGE_NUMBER = "10/05"
        private const val Z_PART = "Z_PART"

        fun newInstance(zPart: ZPartUi) = LongZPartInfoFragment().apply {
            arguments = bundleOf(Pair(Z_PART, zPart))
        }
    }
}