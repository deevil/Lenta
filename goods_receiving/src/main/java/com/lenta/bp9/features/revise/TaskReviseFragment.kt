package com.lenta.bp9.features.revise

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.CheckBox
import androidx.databinding.DataBindingUtil
import com.lenta.bp9.BR
import com.lenta.bp9.R
import com.lenta.bp9.databinding.*
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

class TaskReviseFragment : CoreFragment<FragmentTaskReviseBinding, TaskReviseViewModel>(), ViewPagerSettings, ToolbarButtonsClickListener, OnBackPresserListener {

    var toCheckRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null
    var checkedRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null

    override fun getLayoutId(): Int = R.layout.fragment_task_revise

    override fun getPageNumber() = "09/07"

    override fun getViewModel(): TaskReviseViewModel {
        provideViewModel(TaskReviseViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.taskCaption
        topToolbarUiModel.description.value = getString(R.string.delivery_documents)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.cleanAll()
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.refusal)
        if (vm.isDocsForVerification == true) {
            bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.next)
        } else {
            bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.save)
        }
        connectLiveData(vm.refusalVisibility, bottomToolbarUiModel.uiModelButton2.visibility)
        connectLiveData(vm.nextEnabled, bottomToolbarUiModel.uiModelButton5.enabled)
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return when(position) {
            0 -> prepareToCheckView(container)
            1 -> prepareCheckedView(container)
            2 -> prepareNotificationsView(container)
            else -> View(context)
        }
    }

    override fun getTextTitle(position: Int): String {
        return when (position) {
            0 -> getString(R.string.to_check)
            1 -> getString(R.string.checked)
            2 -> getString(R.string.information)
            else -> ""
        }
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_2 -> vm.onClickRefusal()
            R.id.b_5 -> vm.onClickSave()
        }
    }

    private fun prepareNotificationsView(container: ViewGroup): View {
        DataBindingUtil
                .inflate<LayoutTaskReviseInformationBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_task_revise_information,
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

    private fun prepareCheckedView(container: ViewGroup): View {
        DataBindingUtil
                .inflate<LayoutDeliveryCheckedDocumentsBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_delivery_checked_documents,
                        container,
                        false)
                .let { layoutBinding ->
                    layoutBinding.rvConfig = initRecycleAdapterDataBinding(
                            layoutId = R.layout.item_tile_delivery_documents,
                            itemId = BR.item,
                            onAdapterItemBind = { binding: ItemTileDeliveryDocumentsBinding, position: Int ->
                                binding.tvItemNumber.tag = position
                                binding.cbChecked.setOnClickListener { view ->
                                    val cb = view as? CheckBox
                                    cb?.let { vm.checkedChanged(position, it.isChecked) }
                                }
                                checkedRecyclerViewKeyHandler?.let {
                                    binding.root.isSelected = it.isSelected(position)
                                }
                            },
                            onAdapterItemClicked = {position ->
                                checkedRecyclerViewKeyHandler?.onItemClicked(position)
                            }
                    )

                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner

                    checkedRecyclerViewKeyHandler = initRecyclerViewKeyHandler(
                            recyclerView = layoutBinding.rv,
                            previousPosInfo = checkedRecyclerViewKeyHandler?.posInfo?.value,
                            items = vm.checkedDocs,
                            onClickHandler = vm::onClickCheckedPosition
                    )

                    return layoutBinding.root
                }
    }

    private fun prepareToCheckView(container: ViewGroup): View {
        DataBindingUtil
                .inflate<LayoutDeliveryDocumentsBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_delivery_documents,
                        container,
                        false)
                .let { layoutBinding ->
                    layoutBinding.rvConfig = initRecycleAdapterDataBinding(
                            layoutId = R.layout.item_tile_delivery_documents,
                            itemId = BR.item,
                            onAdapterItemBind = { binding: ItemTileDeliveryDocumentsBinding, position: Int ->
                                binding.tvItemNumber.tag = position
                                binding.cbChecked.setOnClickListener { view ->
                                    val cb = view as? CheckBox
                                    cb?.let { vm.checkedChanged(position, it.isChecked) }
                                }
                                toCheckRecyclerViewKeyHandler?.let {
                                    binding.root.isSelected = it.isSelected(position)
                                }
                            },
                            onAdapterItemClicked = {position ->
                                toCheckRecyclerViewKeyHandler?.onItemClicked(position)
                            }
                    )

                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner

                    toCheckRecyclerViewKeyHandler = initRecyclerViewKeyHandler(
                            recyclerView = layoutBinding.rv,
                            previousPosInfo = toCheckRecyclerViewKeyHandler?.posInfo?.value,
                            items = vm.docsToCheck,
                            onClickHandler = vm::onClickUncheckedPosition
                    )

                    return layoutBinding.root
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

    override fun onBackPressed(): Boolean {
        vm.onBackPressed()
        return false
    }
    
}
