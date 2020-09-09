package com.lenta.shared.platform.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.observe
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.lenta.shared.BR
import com.lenta.shared.R
import com.lenta.shared.di.CoreComponent
import com.lenta.shared.di.FromParentToCoreProvider
import com.lenta.shared.exception.CoreFailureInterpreter
import com.lenta.shared.exception.Failure
import com.lenta.shared.features.alert.AlertFragment
import com.lenta.shared.platform.activity.CoreActivity
import com.lenta.shared.platform.activity.main_activity.CoreMainActivity
import com.lenta.shared.platform.navigation.FragmentStack
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.DataBindingAdapter
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.databinding.PosInfo
import com.lenta.shared.utilities.databinding.RecyclerViewKeyHandler
import com.lenta.shared.utilities.extentions.getFragmentResultCode
import com.lenta.shared.utilities.extentions.implementationOf
import com.lenta.shared.utilities.state.GsonBundle
import com.lenta.shared.utilities.state.GsonBundleDelegate

abstract class CoreFragment<T : ViewDataBinding, S : CoreViewModel> : Fragment(), GsonBundle by GsonBundleDelegate() {
    var binding: T? = null

    open val vm: S by lazy {
        getViewModel()
    }

    private var timeForAllowHandleEnter = Long.MAX_VALUE
    private var mCount = 0

    protected open var recyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null

    private var keyHandlers = mutableMapOf<Int, RecyclerViewKeyHandler<*>>()

    val coreComponent: CoreComponent by lazy {
        (activity as CoreActivity<*>).coreComponent
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restoreInstanceStateGsonBundle(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.failure.observe(viewLifecycleOwner, ::showFailureHandler)
    }

    private fun showFailureHandler(failure: Failure) {
        val failureDescription = CoreFailureInterpreter(requireContext()).getFailureDescription(failure)
        (activity as? CoreMainActivity)?.fragmentStack?.push(
                AlertFragment.create(
                        message = failureDescription.message,
                        iconRes = failureDescription.iconRes,
                        pageNumber = "97",
                        textColor = failureDescription.textColor,
                        title = context?.getString(R.string.error)
                )
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        saveInstanceStateGsonBundle(outState)
        bundle.putInt("mCount", mCount)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false)
        return binding?.run {
            setVariable(BR.vm, vm)
            lifecycleOwner = viewLifecycleOwner
            root
        } ?: throw NullPointerException("DataBinding is null")
    }

    fun getBottomToolBarUIModel(): BottomToolbarUiModel? {
        return getCoreMainActivity()?.getBottomToolBarUIModel()
    }

    fun getTopToolBarUIModel(): TopToolbarUiModel? {
        return getCoreMainActivity()?.getTopToolbarUIModel()
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

    /*protected open fun <T : ViewDataBinding> initRecycleAdapterDataBinding(
            @LayoutRes layoutId: Int,
            itemId: Int,
            onAdapterItemCreate: ((T) -> Unit)? = null,
            onAdapterItemBind: ((T, Int) -> Unit)? = ::onAdapterBindHandler,
            onAdapterItemClicked: ((Int) -> Unit)? = ::onAdapterItemClickHandler
    ): DataBindingRecyclerViewConfig<T> {
        return DataBindingRecyclerViewConfig(
                layoutId = layoutId,
                itemId = itemId,
                realisation = object : DataBindingAdapter<T> {
                    override fun onCreate(binding: T) {
                        onAdapterItemCreate?.invoke(binding)
                    }

                    override fun onBind(binding: T, position: Int) {
                        onAdapterItemBind?.invoke(binding, position)
                    }
                },
                onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                    onAdapterItemClicked?.invoke(position)
                }
        )
    }*/

    protected open fun <Item : Any, T : ViewDataBinding> initRecycleAdapterDataBinding(
            @LayoutRes layoutId: Int,
            itemId: Int,
            onAdapterItemCreate: ((T) -> Unit)? = null,
            onAdapterItemBind: ((T, keyHandler: RecyclerViewKeyHandler<*>?, Int) -> Unit)? = ::onAdapterBindHandler,
            onAdapterItemClicked: ((keyHandler: RecyclerViewKeyHandler<*>?, Int) -> Unit)? = ::onAdapterItemClickHandler,
            recyclerView: RecyclerView,
            items: LiveData<List<Item>>,
            previousPosInfo: PosInfo? = null,
            onClickHandler: ((Int) -> Unit)? = null
    ): DataBindingRecyclerViewConfig<T> {
        val keyHandler = getKeyHandler(recyclerView.hashCode()) ?: initRecyclerViewKeyHandler(
                recyclerView = recyclerView,
                previousPosInfo = getKeyHandler(recyclerView.hashCode())?.posInfo?.value,
                items = items,
                onClickHandler = onClickHandler
        )

        return DataBindingRecyclerViewConfig(
                layoutId = layoutId,
                itemId = itemId,
                realisation = object : DataBindingAdapter<T> {
                    override fun onCreate(binding: T) {
                        onAdapterItemCreate?.invoke(binding)
                    }

                    override fun onBind(binding: T, position: Int) {
                        onAdapterItemBind?.invoke(binding, keyHandler, position)
                    }
                },
                onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                    onAdapterItemClicked?.invoke(keyHandler, position)
                }
        )
    }

    private fun getKeyHandler(key: Int): RecyclerViewKeyHandler<*>? {
        return keyHandlers.getValue(key)
    }

    protected open fun <Item : Any> initRecyclerViewKeyHandler(
            recyclerView: RecyclerView,
            items: LiveData<List<Item>>,
            previousPosInfo: PosInfo? = null,
            onClickHandler: ((Int) -> Unit)? = null
    ): RecyclerViewKeyHandler<Item> {
        val key = recyclerView.hashCode()
        val keyHandler = RecyclerViewKeyHandler(
                rv = recyclerView,
                items = items,
                lifecycleOwner = viewLifecycleOwner,
                initPosInfo = previousPosInfo,
                onClickPositionFunc = onClickHandler
        )

        keyHandlers[key] = keyHandler

        return keyHandler
    }

    protected open fun onAdapterBindHandler(bindItem: ViewBinding, keyHandler: RecyclerViewKeyHandler<*>? = null, position: Int) {
        keyHandler?.let {
            bindItem.root.isSelected = it.isSelected(position)
        }
    }

    protected open fun onAdapterItemClickHandler(keyHandler: RecyclerViewKeyHandler<*>? = null, position: Int) {
        keyHandler?.onItemClicked(position)
    }

    @LayoutRes
    abstract fun getLayoutId(): Int

    abstract fun getPageNumber(): String?

    abstract fun getViewModel(): S

    abstract fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel)

    abstract fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel)

    override fun onDestroyView() {
        recyclerViewKeyHandler?.onClickPositionFunc = null
        super.onDestroyView()
    }

    override fun onDestroy() {
        binding?.unbind()
        binding = null
        keyHandlers.clear()
        super.onDestroy()
    }

    private fun getCoreMainActivity(): CoreMainActivity? {
        return activity?.implementationOf(CoreMainActivity::class.java)
    }
}