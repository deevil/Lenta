package com.lenta.bp9.features.task_card

import android.content.res.Resources
import com.lenta.bp9.R
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel
import android.os.Bundle
import android.view.LayoutInflater
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import android.view.ViewGroup
import android.view.View
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import com.lenta.bp9.BR
import com.lenta.bp9.databinding.*
import com.lenta.bp9.features.loading.tasks.TaskCardMode
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.DataBindingAdapter
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.databinding.RecyclerViewKeyHandler
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumber

class TaskCardFragment : CoreFragment<FragmentTaskCardBinding, TaskCardViewModel>(), ViewPagerSettings, ToolbarButtonsClickListener, OnBackPresserListener {

    var notificationsRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null

    private var mode: TaskCardMode = TaskCardMode.None

    override fun getLayoutId(): Int = R.layout.fragment_task_card

    override fun getPageNumber() = "09/06"

    override fun getViewModel(): TaskCardViewModel {
        provideViewModel(TaskCardViewModel::class.java).let {
            getAppComponent()?.inject(it)
            it.mode = mode
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.taskCaption
        topToolbarUiModel.description.value = getString(R.string.task_card)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        if (vm.mode == TaskCardMode.Full) {
            bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.verify)
            bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.docs)
            bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.nextAlternate)
            connectLiveData(source = vm.redIndicatorAbsent, target = bottomToolbarUiModel.uiModelButton5.enabled)
        }
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return when(position) {
            0 -> prepareStatusView(container)
            1 -> prepareDeliveryView(container)
            2 -> prepareNotificationsView(container)
            else -> View(context)
        }
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_2 -> vm.onClickVerify()
            R.id.b_4 -> vm.onClickSupply()
            R.id.b_5 -> vm.onClickNext()
        }
    }

    override fun onResume() {
        super.onResume()
        val tabItemLayout = (binding?.tabStrip?.getChildAt(0) as LinearLayout).getChildAt(2) as LinearLayout
        tabItemLayout.orientation = LinearLayout.HORIZONTAL
        val iconView = tabItemLayout.getChildAt(0) as ImageView
        val textView = tabItemLayout.getChildAt(1) as TextView
        tabItemLayout.removeView(iconView)
        textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_indicator_red_tablayout, 0)
        textView.compoundDrawablePadding = 5
        vm.onResume()
    }

    fun prepareNotificationsView(container: ViewGroup): View {
        DataBindingUtil
                .inflate<LayoutTaskCardNotificationsBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_task_card_notifications,
                        container,
                        false).let { layoutBinding ->

                    layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                            layoutId = R.layout.item_tile_notifications,
                            itemId = BR.vm,
                            realisation = object : DataBindingAdapter<ItemTileNotificationsBinding> {
                                override fun onCreate(binding: ItemTileNotificationsBinding) {
                                }

                                override fun onBind(binding: ItemTileNotificationsBinding, position: Int) {
                                    binding.tvCounter.tag = position
                                }
                            }
                    )

                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner
                    val rvKeyHandler = RecyclerViewKeyHandler(
                            rv = layoutBinding.rv,
                            items = vm.notifications,
                            lifecycleOwner = layoutBinding.lifecycleOwner!!
                    )
                    notificationsRecyclerViewKeyHandler = rvKeyHandler
                    return layoutBinding.root
                }
    }

    fun prepareDeliveryView(container: ViewGroup): View {
        DataBindingUtil
                .inflate<LayoutTaskCardDeliveryBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_task_card_delivery,
                        container,
                        false).let { layoutBinding ->
                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner
                    return layoutBinding.root
                }
    }

    fun prepareStatusView(container: ViewGroup): View {
        DataBindingUtil
                .inflate<LayoutTaskCardStatusBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_task_card_status,
                        container,
                        false).let { layoutBinding ->
                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner
                    return layoutBinding.root
                }
    }

    override fun getTextTitle(position: Int): String {
        Logg.d { "testddi_1" }
        return when (position) {
            0 -> getString(R.string.status)
            1 -> getString(R.string.delivery)
            2 -> getString(R.string.notifications)
            else -> ""
        }
    }

    override fun countTab(): Int {
        return 3
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
        /**binding?.let {bind ->
            Logg.d { "testddi_1" }
            bind.tabStrip?.let {
                Logg.d { "testddi_2" }
                Logg.d { "testddi_3 ${it.tabCount}" }
                it.getTabAt(1)?.let {tab ->
                    Logg.d { "testddi_4" }
                    tab.setIcon(R.drawable.ic_indicator_red)
                }
            }
        }*/
    }

    override fun onBackPressed(): Boolean {
        vm.onBackPressed()
        return false
    }

    companion object {
        fun create(mode: TaskCardMode): TaskCardFragment {
            TaskCardFragment().let {
                it.mode = mode
                return it
            }
        }
    }
}
