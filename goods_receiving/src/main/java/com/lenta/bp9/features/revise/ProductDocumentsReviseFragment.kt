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
import com.lenta.bp9.model.task.TaskType
import com.lenta.bp9.platform.extentions.getAppComponent
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

class ProductDocumentsReviseFragment : CoreFragment<FragmentProductDocumentsReviseBinding, ProductDocumentsReviseViewModel>(), ViewPagerSettings, ToolbarButtonsClickListener {

    var notificationsRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null
    var toCheckRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null
    var checkedRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null

    override fun getLayoutId(): Int = R.layout.fragment_product_documents_revise

    override fun getPageNumber() = "09/10"

    override fun getViewModel(): ProductDocumentsReviseViewModel {
        provideViewModel(ProductDocumentsReviseViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.taskCaption
        topToolbarUiModel.description.value = getString(R.string.product_documents)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.cleanAll()
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        if (vm.typeTask != TaskType.ReceptionDistributionCenter && vm.typeTask != TaskType.OwnProduction) {
            bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.refusal)
        }
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.sort)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.save)
        connectLiveData(vm.sortEnabled, bottomToolbarUiModel.uiModelButton4.enabled)
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return when(position) {
            0 -> prepareToCheckView(container)
            1 -> prepareCheckedView(container)
            2 -> prepareNotificationsView(container)
            else -> View(context)
        }
    }

    private fun prepareNotificationsView(container: ViewGroup): View {
        DataBindingUtil
                .inflate<LayoutProductsReviseInformationBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_products_revise_information,
                        container,
                        false).let { layoutBinding ->

                    layoutBinding.rvConfig = initRecycleAdapterDataBinding(
                            layoutId = R.layout.item_tile_notifications,
                            itemId = BR.item,
                            onAdapterItemBind = { binding: ItemTileNotificationsBinding, position: Int ->
                                binding.tvItemNumber.tag = position
                                onAdapterBindHandler(binding, position)
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

    private fun prepareCheckedView(container: ViewGroup): View {
        DataBindingUtil
                .inflate<LayoutProductsCheckedDocumentsBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_products_checked_documents,
                        container,
                        false).let { layoutBinding ->

                    layoutBinding.rvConfig = initRecycleAdapterDataBinding(
                            layoutId = R.layout.item_tile_product_documents,
                            itemId = BR.item,
                            onAdapterItemBind = { binding: ItemTileProductDocumentsBinding, position: Int ->
                                binding.tvItemNumber.tag = position
                                binding.cbChecked.setOnClickListener { view ->
                                    val cb = view as? CheckBox
                                    cb?.let { vm.checkedChanged(position, it.isChecked) }
                                }
                                checkedRecyclerViewKeyHandler?.let {
                                    binding.root.isSelected = it.isSelected(position)
                                }
                                onAdapterBindHandler(binding, position)
                            },
                            onAdapterItemClicked = { position ->
                                checkedRecyclerViewKeyHandler
                                        ?.let {
                                            if (it.isSelected(position)) {
                                                vm.onClickCheckedPosition(position)
                                            } else {
                                                it.selectPosition(position)
                                            }
                                        }
                            }
                    )

                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner
                    val rvKeyHandler = RecyclerViewKeyHandler(
                            rv = layoutBinding.rv,
                            items = vm.checkedDocs,
                            lifecycleOwner = layoutBinding.lifecycleOwner!!,
                            initPosInfo = checkedRecyclerViewKeyHandler?.posInfo?.value
                    )
                    checkedRecyclerViewKeyHandler = rvKeyHandler
                    return layoutBinding.root
                }
    }

    private fun prepareToCheckView(container: ViewGroup): View {
        DataBindingUtil
                .inflate<LayoutProductsDocumentsBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_products_documents,
                        container,
                        false).let { layoutBinding ->

                    layoutBinding.rvConfig = initRecycleAdapterDataBinding(
                            layoutId = R.layout.item_tile_product_documents,
                            itemId = BR.item,
                            onAdapterItemBind = { binding: ItemTileProductDocumentsBinding, position: Int ->
                                binding.tvItemNumber.tag = position
                                binding.cbChecked.setOnClickListener { view ->
                                    val cb = view as? CheckBox
                                    cb?.let { vm.checkedChanged(position, it.isChecked) }
                                }
                                toCheckRecyclerViewKeyHandler?.let {
                                    binding.root.isSelected = it.isSelected(position)
                                }
                                onAdapterBindHandler(binding, position)
                            },
                            onAdapterItemClicked = { position ->
                                toCheckRecyclerViewKeyHandler
                                        ?.let {
                                            if (it.isSelected(position)) {
                                                vm.onClickUncheckedPosition(position)
                                            } else {
                                                it.selectPosition(position)
                                            }
                                        }
                            }
                    )

                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner
                    val rvKeyHandler = RecyclerViewKeyHandler(
                            rv = layoutBinding.rv,
                            items = vm.docsToCheck,
                            lifecycleOwner = layoutBinding.lifecycleOwner!!,
                            initPosInfo = toCheckRecyclerViewKeyHandler?.posInfo?.value
                    )
                    toCheckRecyclerViewKeyHandler = rvKeyHandler
                    return layoutBinding.root
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
            R.id.b_2 -> vm.onClickReject()
            R.id.b_4 -> vm.onClickSort()
            R.id.b_5 -> vm.onClickSave()
        }
    }
}
