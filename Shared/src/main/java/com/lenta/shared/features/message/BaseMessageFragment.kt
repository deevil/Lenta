package com.lenta.shared.features.message

import com.lenta.shared.R
import com.lenta.shared.databinding.LayoutMessageBinding
import com.lenta.shared.platform.fragment.BaseFragment

abstract class BaseMessageFragment : BaseFragment<LayoutMessageBinding, MessageViewModel>() {
    protected var message: String? = null
    override fun getLayoutId(): Int = R.layout.layout_message
}