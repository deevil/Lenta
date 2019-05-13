package com.lenta.shared.features.message

import com.lenta.shared.R
import com.lenta.shared.databinding.LayoutMessageBinding
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener

abstract class CoreMessageFragment : CoreFragment<LayoutMessageBinding, MessageViewModel>(), ToolbarButtonsClickListener {
    protected var message: String? = null
    protected var iconRes: Int = 0
    protected var codeConfirm: Int? = null
    override fun getLayoutId(): Int = R.layout.layout_message
}