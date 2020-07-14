package com.lenta.shared.platform.fragment

import android.os.Bundle
import android.view.*
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.lenta.shared.BR
import com.lenta.shared.di.CoreComponent
import com.lenta.shared.di.FromParentToCoreProvider
import com.lenta.shared.platform.activity.CoreActivity
import com.lenta.shared.platform.activity.main_activity.CoreMainActivity
import com.lenta.shared.platform.navigation.FragmentStack
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.getFragmentResultCode
import com.lenta.shared.utilities.extentions.implementationOf
import com.lenta.shared.utilities.state.GsonBundle
import com.lenta.shared.utilities.state.GsonBundleDelegate
import java.lang.NullPointerException

abstract class CoreFragment<T : ViewDataBinding, S : CoreViewModel> : Fragment(), GsonBundle by GsonBundleDelegate() {
    var binding: T? = null
    lateinit var vm: S

    private var timeForAllowHandleEnter = Long.MAX_VALUE

    val coreComponent: CoreComponent by lazy {
        (activity as CoreActivity<*>).coreComponent
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restoreInstanceStateGsonBundle(savedInstanceState)
        vm = getViewModel()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        saveInstanceStateGsonBundle(outState)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false)
        binding?.let {
            it.setVariable(BR.vm, vm)
            it.lifecycleOwner = viewLifecycleOwner
            return it.root
        }
        throw NullPointerException("DataBinding is null")
    }

    fun getBottomToolBarUIModel(): BottomToolbarUiModel? {
        return getCoreMainActivity()?.getBottomToolBarUIModel()
    }

    fun getTopToolBarUIModel(): TopToolbarUiModel? {
        return getCoreMainActivity()?.getTopToolbarUIModel()
    }

    private fun getCoreMainActivity(): CoreMainActivity? {
        return activity?.implementationOf(CoreMainActivity::class.java)
    }

    override fun onResume() {
        super.onResume()
        timeForAllowHandleEnter = System.currentTimeMillis() + 500L
        Logg.d { "onResume $this" }
        arguments?.let { arguments ->
            arguments.getBundle(FragmentStack.SAVE_TAG_FOR_ARGUMENTS)?.let {
                onFragmentResult(it)
            }
            arguments.remove(FragmentStack.SAVE_TAG_FOR_ARGUMENTS)
        }
        invalidateTopToolBar()
        invalidateBottomToolBar()
    }

    open fun onFragmentResult(arguments: Bundle) {
        Logg.d { "onFragmentResult arguments: $arguments" }
        vm.handleFragmentResult(arguments.getFragmentResultCode())
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    fun invalidateTopToolBar() {
        getTopToolBarUIModel()?.let {
            cleanTopToolbar(it)
            setupTopToolBar(it)
        }
    }

    open fun cleanTopToolbar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.cleanAll()
    }


    fun invalidateBottomToolBar() {
        getBottomToolBarUIModel()?.let {
            it.cleanAll()
            setupBottomToolBar(it)
        }
    }

    protected fun provideFromParentToCoreProvider(): FromParentToCoreProvider? {
        return getCoreMainActivity()?.provideFromParentToCoreProvider()
    }

    protected fun isAllowHandleKeyCode(): Boolean {
        return System.currentTimeMillis() > timeForAllowHandleEnter
    }

    @LayoutRes
    abstract fun getLayoutId(): Int

    abstract fun getPageNumber(): String?

    abstract fun getViewModel(): S

    abstract fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel)

    abstract fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel)


}