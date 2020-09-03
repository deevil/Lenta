package com.lenta.bp9.features.revise

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.CheckBox
import androidx.databinding.DataBindingUtil
import com.lenta.bp9.BR
import com.lenta.bp9.R
import com.lenta.bp9.databinding.*
import com.lenta.bp9.model.task.TaskType
import com.lenta.bp9.model.task.revise.ConditionType
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.databinding.DataBindingAdapter
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.databinding.RecyclerViewKeyHandler
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.provideViewModel

class TransportConditionsReviseFragment : CoreFragment<FragmentTransportConditionsReviseBinding, TransportConditionsReviseViewModel>(),
        ViewPagerSettings,
        ToolbarButtonsClickListener,
        OnBackPresserListener {

    private var toCheckRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null
    private var checkedRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null

    override fun getLayoutId(): Int = R.layout.fragment_transport_conditions_revise

    override fun getPageNumber() = "09/14"

    override fun getViewModel(): TransportConditionsReviseViewModel {
        provideViewModel(TransportConditionsReviseViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.taskCaption
        topToolbarUiModel.description.value = getString(R.string.transport_conditions_revise)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.cleanAll()
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        if (vm.typeTask == TaskType.ReceptionDistributionCenter || vm.typeTask == TaskType.OwnProduction || vm.typeTask == TaskType.ShoppingMall) {
            bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.breaking)
        } else {
            if (vm.typeTask != TaskType.ShipmentRC) { //https://trello.com/c/vMdcpNPY
                bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.refusal)
            }
        }
        if (vm.isTaskPRCorPSPStatusUnloading.value == true) {
            bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.next)
        } else {
            bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.save)
        }
        connectLiveData(vm.saveEnabled, bottomToolbarUiModel.uiModelButton5.enabled)
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return when(position) {
            0 -> prepareToCheckView(container)
            1 -> prepareCheckedView(container)
            2 -> prepareNotificationsView(container)
            else -> View(context)
        }
    }

    private fun prepareToCheckView(container: ViewGroup): View {
        DataBindingUtil
                .inflate<LayoutTransportConditionsUncheckedBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_transport_conditions_unchecked,
                        container,
                        false)
                .let { layoutBinding ->
                    layoutBinding.rvConfig = initRecycleAdapterDataBinding(
                            layoutId = R.layout.item_tile_transport_condition,
                            itemId = BR.item,
                            onAdapterItemBind = { binding: ItemTileTransportConditionBinding, position: Int ->
                                binding.tvItemNumber.tag = position
                                binding.cbChecked.setOnClickListener { view ->
                                    val cb = view as? CheckBox
                                    cb?.let { vm.checkedChanged(position, it.isChecked) }
                                }
                                binding.etEditText.setOnFocusChangeListener { v, hasFocus ->
                                    if (!hasFocus) {
                                        vm.finishedInput(position)
                                    }
                                }
                                binding.etEditText.setOnEditorActionListener { v, actionId, event ->
                                    when(actionId) {
                                        EditorInfo.IME_ACTION_DONE -> {
                                            vm.finishedInput(position)
                                            return@setOnEditorActionListener false
                                        }
                                        else -> return@setOnEditorActionListener false
                                    }
                                }
                                val conditionsToCheckSize = vm.conditionsToCheck.value?.size ?: 0
                                if (conditionsToCheckSize - 1 >= position) {
                                    binding.cbChecked.visibility =
                                            when (vm.conditionsToCheck.value?.get(position)?.conditionType) {
                                                ConditionType.Checkbox -> View.VISIBLE
                                                else -> View.INVISIBLE
                                            }
                                    binding.etEditText.visibility =
                                            when (vm.conditionsToCheck.value?.get(position)?.conditionType) {
                                                ConditionType.Input -> View.VISIBLE
                                                else -> View.INVISIBLE
                                            }
                                }
                                toCheckRecyclerViewKeyHandler?.let {
                                    binding.root.isSelected = it.isSelected(position)
                                }
                            }
                    )

                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner

                    toCheckRecyclerViewKeyHandler = initRecyclerViewKeyHandler(
                            recyclerView = layoutBinding.rv,
                            previousPosInfo = toCheckRecyclerViewKeyHandler?.posInfo?.value,
                            items = vm.conditionsToCheck
                    )

                    return layoutBinding.root
                }
    }

    private fun prepareCheckedView(container: ViewGroup): View {
        DataBindingUtil
                .inflate<LayoutTransportConditionsCheckedBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_transport_conditions_checked,
                        container,
                        false)
                .let { layoutBinding ->
                    layoutBinding.rvConfig = initRecycleAdapterDataBinding(
                            layoutId = R.layout.item_tile_transport_condition_checked,
                            itemId = BR.item,
                            onAdapterItemBind = { binding: ItemTileTransportConditionCheckedBinding, position: Int ->
                                binding.tvItemNumber.tag = position
                                binding.cbChecked.setOnClickListener { view ->
                                    val cb = view as? CheckBox
                                    cb?.let { vm.checkedChanged(position, it.isChecked) }
                                }
                                if ((vm.checkedConditions.value?.size ?: 0) - 1 >= position) {
                                    binding.cbChecked.visibility = when (vm.checkedConditions.value?.get(position)?.conditionType) {
                                        ConditionType.Checkbox -> View.VISIBLE
                                        else -> View.INVISIBLE
                                    }
                                }
                                if ((vm.checkedConditions.value?.size ?: 0) - 1 >= position) {
                                    binding.etConditionValue.visibility = when (vm.checkedConditions.value?.get(position)?.conditionType) {
                                        ConditionType.Input -> View.VISIBLE
                                        else -> View.INVISIBLE
                                    }
                                }
                                binding.etConditionValue.setOnFocusChangeListener { v, hasFocus ->
                                    if (!hasFocus) {
                                        vm.finishedInput(position)
                                    }
                                }
                                binding.etConditionValue.setOnEditorActionListener { v, actionId, event ->
                                    when(actionId) {
                                        EditorInfo.IME_ACTION_DONE -> {
                                            vm.finishedInput(position)
                                            return@setOnEditorActionListener false
                                        }
                                        else -> return@setOnEditorActionListener false
                                    }
                                }
                                checkedRecyclerViewKeyHandler?.let {
                                    binding.root.isSelected = it.isSelected(position)
                                }
                            }
                    )

                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner

                    checkedRecyclerViewKeyHandler = initRecyclerViewKeyHandler(
                            recyclerView = layoutBinding.rv,
                            previousPosInfo = checkedRecyclerViewKeyHandler?.posInfo?.value,
                            items = vm.checkedConditions
                    )

                    return layoutBinding.root
                }
    }

    private fun prepareNotificationsView(container: ViewGroup): View {
        DataBindingUtil
                .inflate<LayoutTransportConditionsInformationBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_transport_conditions_information,
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
        return when (position) {
            0 -> getString(R.string.verify)
            1 -> getString(R.string.verified)
            2 -> getString(R.string.information)
            else -> ""
        }
    }

    override fun countTab(): Int {
        return 3
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }

    override fun onResume() {
        super.onResume()
        vm.onResume()
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_2 -> vm.onClickSecondButton()
            R.id.b_5 -> vm.onClickNext()
        }
    }

    override fun onBackPressed(): Boolean {
        vm.onBackPressed()
        return false
    }
}
