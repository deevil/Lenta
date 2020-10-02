package com.lenta.bp9.features.task_card

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.lenta.bp9.BR
import com.lenta.bp9.R
import com.lenta.bp9.databinding.*
import com.lenta.bp9.features.loading.tasks.TaskCardMode
import com.lenta.bp9.model.task.NotificationIndicatorType
import com.lenta.bp9.model.task.TaskStatus
import com.lenta.bp9.model.task.TaskType
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.provideViewModel

class TaskCardFragment : CoreFragment<FragmentTaskCardBinding, TaskCardViewModel>(), ViewPagerSettings, ToolbarButtonsClickListener, OnBackPresserListener {

    companion object {
        fun create(mode: TaskCardMode, taskType: TaskType): TaskCardFragment {
            TaskCardFragment().let {
                it.mode = mode
                it.taskType = taskType
                return it
            }
        }
    }

    private var mode: TaskCardMode = TaskCardMode.None
    private var taskType: TaskType = TaskType.None

    override fun getLayoutId(): Int = R.layout.fragment_task_card

    override fun getPageNumber() = if (this.taskType == TaskType.ShipmentPP || this.taskType == TaskType.ShipmentRC) "09/109" else "09/06"

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
        bottomToolbarUiModel.cleanAll()
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        if (vm.mode == TaskCardMode.Full) {
            when (vm.currentStatus.value) {
                TaskStatus.Unloaded -> {
                    when (vm.taskType) {
                        TaskType.RecalculationCargoUnit -> bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.skipAlternate)
                        TaskType.ReceptionDistributionCenter, TaskType.ShoppingMall -> bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.transportMarriage)
                        else -> Unit
                    }
                }
                TaskStatus.Checked -> {
                    if (vm.taskType == TaskType.ShipmentPP) {
                        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.skipAlternate)
                    }
                    bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.verify)
                }
                TaskStatus.Recounted -> bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.recount)
                TaskStatus.ReadyToShipment -> {
                    if (vm.taskType == TaskType.ShipmentRC) {
                        bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.skipAlternate)
                    }
                }
                else -> Unit
            }

            bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.docs)
            bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.nextAlternate)
            connectLiveData(vm.isSecondBtnVisible, bottomToolbarUiModel.uiModelButton2.visibility)
            connectLiveData(vm.enabledBtn, bottomToolbarUiModel.uiModelButton2.enabled)
            connectLiveData(vm.visibilityBtnFourth, bottomToolbarUiModel.uiModelButton4.visibility)
            connectLiveData(vm.enabledBtn, bottomToolbarUiModel.uiModelButton4.enabled)
            connectLiveData(vm.visibilityNextBtn, bottomToolbarUiModel.uiModelButton5.visibility)
            connectLiveData(vm.enabledBtn, bottomToolbarUiModel.uiModelButton5.enabled)
        }

        if (vm.mode == TaskCardMode.ReadOnly) {
            when (vm.currentStatus.value) {
                TaskStatus.Unloaded -> {
                    when (vm.taskType) {
                        TaskType.RecalculationCargoUnit -> bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.skipAlternate, enabled = false)
                        TaskType.ReceptionDistributionCenter, TaskType.ShoppingMall -> bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.transportMarriage, enabled = false)
                        else -> Unit
                    }
                }
                TaskStatus.Checked -> {
                    if (vm.taskType == TaskType.ShipmentPP) {
                        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.skipAlternate, enabled = false)
                    }
                    bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.verify, enabled = false)
                }
                TaskStatus.Recounted -> bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.recount, enabled = false)
                TaskStatus.ReadyToShipment -> {
                    if (vm.taskType == TaskType.ShipmentPP || vm.taskType == TaskType.ShipmentRC) {
                        bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.skipAlternate, enabled = false)
                    }
                }
                else -> Unit
            }

            bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.docs)
            bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.nextAlternate, enabled = false)
            connectLiveData(vm.isSecondBtnVisible, bottomToolbarUiModel.uiModelButton2.visibility)
            connectLiveData(vm.visibilityBtnFourth, bottomToolbarUiModel.uiModelButton4.visibility)
            connectLiveData(vm.visibilityNextBtn, bottomToolbarUiModel.uiModelButton5.visibility)
        }
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return if (vm.taskType == TaskType.ShipmentPP || vm.taskType == TaskType.ShipmentRC) {
            when (position) {
                0 -> prepareStatusView(container)
                1 -> prepareShipmentView(container)
                2 -> prepareDetailsView(container)
                3 -> prepareNotificationsView(container)
                else -> View(context)
            }
        } else {
            when (position) {
                0 -> prepareStatusView(container)
                1 -> prepareDeliveryView(container)
                2 -> prepareNotificationsView(container)
                else -> View(context)
            }
        }
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_2 -> vm.onClickSecondButton()
            R.id.b_3 -> vm.onClickDocs()
            R.id.b_4 -> vm.onClickFourth()
            R.id.b_5 -> vm.onClickNext()
        }
    }

    override fun onResume() {
        super.onResume()
        if (vm.bookmarkIndicator != NotificationIndicatorType.None) {
            val tabItemLayout = (binding?.tabStrip?.getChildAt(0) as LinearLayout).getChildAt(2) as LinearLayout
            tabItemLayout.orientation = LinearLayout.HORIZONTAL
            val iconView = tabItemLayout.getChildAt(0) as ImageView
            val textView = tabItemLayout.getChildAt(1) as TextView
            tabItemLayout.removeView(iconView)
            if (vm.bookmarkIndicator == NotificationIndicatorType.Red) {
                textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_indicator_tablayout_red_8dp, 0)
            } else {
                textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_indicator_tablayout_yellow_8dp, 0)
            }
            textView.compoundDrawablePadding = 5
        }
        vm.onResume()
    }

    private fun prepareStatusView(container: ViewGroup): View {
        DataBindingUtil
                .inflate<LayoutTaskCardStatusBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_task_card_status,
                        container,
                        false)
                .let { layoutBinding ->
                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner
                    return layoutBinding.root
                }
    }

    private fun prepareDeliveryView(container: ViewGroup): View {
        DataBindingUtil
                .inflate<LayoutTaskCardDeliveryBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_task_card_delivery,
                        container,
                        false)
                .let { layoutBinding ->
                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner
                    return layoutBinding.root
                }
    }

    private fun prepareShipmentView(container: ViewGroup): View {
        DataBindingUtil
                .inflate<LayoutTaskCardShipmentBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_task_card_shipment,
                        container,
                        false)
                .let { layoutBinding ->
                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner
                    return layoutBinding.root
                }
    }

    private fun prepareDetailsView(container: ViewGroup): View {
        DataBindingUtil
                .inflate<LayoutTaskCardDetailsBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_task_card_details,
                        container,
                        false)
                .let { layoutBinding ->
                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner
                    return layoutBinding.root
                }
    }

    private fun prepareNotificationsView(container: ViewGroup): View {
        DataBindingUtil
                .inflate<LayoutTaskCardNotificationsBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_task_card_notifications,
                        container,
                        false)
                .let { layoutBinding ->

                    layoutBinding.rvConfig = initRecycleAdapterDataBinding<ItemTileNotificationsBinding>(
                            layoutId = R.layout.item_tile_notifications,
                            itemId = BR.item
                    )

                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner
                    return layoutBinding.root
                }
    }

    override fun getTextTitle(position: Int): String {
        return if (vm.taskType == TaskType.ShipmentPP || vm.taskType == TaskType.ShipmentRC) {
            when (position) {
                0 -> getString(R.string.status)
                1 -> getString(R.string.shipment)
                2 -> getString(R.string.details)
                3 -> getString(R.string.notifications)
                else -> ""
            }
        } else {
            when (position) {
                0 -> getString(R.string.status)
                1 -> getString(R.string.delivery)
                2 -> getString(R.string.notifications)
                else -> ""
            }
        }
    }

    override fun countTab(): Int {
        return if (vm.taskType == TaskType.ShipmentPP || vm.taskType == TaskType.ShipmentRC) 4 else 3
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }

    override fun onBackPressed(): Boolean {
        vm.onBackPressed()
        return false
    }

}
