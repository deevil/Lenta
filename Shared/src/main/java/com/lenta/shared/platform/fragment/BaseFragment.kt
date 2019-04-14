package com.lenta.shared.platform.fragment

import android.os.Bundle
import android.view.*
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.lenta.shared.platform.activity.main_activity.BaseMainActivity
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.utilities.extentions.implementationOf
import java.lang.NullPointerException

abstract class BaseFragment<T : ViewDataBinding> : Fragment() {
    var binding: T? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false)
        binding?.let {
            it.lifecycleOwner = this
            return it.root
        }
        throw NullPointerException("DataBinding is null")
    }

    fun getBottomToolBarUIModel(): BottomToolbarUiModel? {
        return getBaseMainActivity()?.getBottomToolBarUIModel()
    }

    private fun getBaseMainActivity(): BaseMainActivity? {
        return activity?.implementationOf(BaseMainActivity::class.java)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    @LayoutRes
    abstract fun getLayoutId(): Int


}