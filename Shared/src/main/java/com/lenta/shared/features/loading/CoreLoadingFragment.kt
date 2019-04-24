package com.lenta.shared.features.loading

import com.lenta.shared.R
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener

abstract class CoreLoadingFragment : CoreFragment<com.lenta.shared.databinding.LayoutLoginBinding, CoreLoadingViewModel>(), OnBackPresserListener, ToolbarButtonsClickListener {

    override fun getLayoutId(): Int = R.layout.layout_loading_data
}