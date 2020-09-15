package com.lenta.bp9.features.revise

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.lenta.shared.utilities.databinding.RecyclerViewKeyHandler
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.provideViewModel

class ProductDocumentsReviseFragment : CoreFragment<FragmentProductDocumentsReviseBinding, ProductDocumentsReviseViewModel>(), ViewPagerSettings, ToolbarButtonsClickListener {

    var toCheckRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null
    var checkedRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null

    override fun getLayoutId(): Int = R.layout.fragment_product_documents_revise

    override fun getPageNumber() = SCREEN_NUMBER

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
        if (!(vm.typeTask == TaskType.ReceptionDistributionCenter
                        || vm.typeTask == TaskType.OwnProduction
                        || vm.typeTask == TaskType.ShoppingMall)) {
            bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.refusal)
        }
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.sort)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.save)
        connectLiveData(vm.sortEnabled, bottomToolbarUiModel.uiModelButton4.enabled)
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return when (position) {
            TAB_TO_CHECK -> prepareToCheckView(container)
            TAB_CHECKED -> prepareCheckedView(container)
            TAB_INFO -> prepareNotificationsView(container)
            else -> View(context)
        }
    }

    private fun prepareNotificationsView(container: ViewGroup): View {
        DataBindingUtil
                .inflate<LayoutProductsReviseInformationBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_products_revise_information,
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
                .inflate<LayoutProductsCheckedDocumentsBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_products_checked_documents,
                        container,
                        false)
                .let { layoutBinding ->
                    layoutBinding.rvConfig = initRecycleAdapterDataBinding(
                            layoutId = R.layout.item_tile_product_documents,
                            itemId = BR.item,
                            onItemBind = { binding: ItemTileProductDocumentsBinding, position: Int ->
                                binding.tvItemNumber.tag = position
                                binding.cbChecked.setOnClickListener { view ->
                                    val cb = view as? CheckBox
                                    cb?.let { vm.checkedChanged(position, it.isChecked) }
                                }
                            },
                            keyHandlerId = TAB_CHECKED,
                            recyclerView = layoutBinding.rv,
                            items = vm.checkedDocs,
                            onClickHandler = vm::onClickCheckedPosition
                    )

                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner

                    return layoutBinding.root
                }
    }

    private fun prepareToCheckView(container: ViewGroup): View {
        DataBindingUtil
                .inflate<LayoutProductsDocumentsBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_products_documents,
                        container,
                        false)
                .let { layoutBinding ->
                    layoutBinding.rvConfig = initRecycleAdapterDataBinding(
                            layoutId = R.layout.item_tile_product_documents,
                            itemId = BR.item,
                            onItemBind = { binding: ItemTileProductDocumentsBinding, position: Int ->
                                binding.tvItemNumber.tag = position
                                binding.cbChecked.setOnClickListener { view ->
                                    val cb = view as? CheckBox
                                    cb?.let { vm.checkedChanged(position, it.isChecked) }
                                }
                            },
                            keyHandlerId = TAB_TO_CHECK,
                            recyclerView = layoutBinding.rv,
                            items = vm.docsToCheck,
                            onClickHandler = vm::onClickUncheckedPosition
                    )

                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner

                    return layoutBinding.root
                }
    }

    override fun getTextTitle(position: Int): String {
        return when (position) {
            TAB_TO_CHECK -> getString(R.string.to_check)
            TAB_CHECKED -> getString(R.string.checked)
            TAB_INFO -> getString(R.string.information)
            else -> ""
        }
    }

    override fun countTab(): Int {
        return TABS
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

    companion object {
        const val SCREEN_NUMBER = "09/10"

        private const val TABS = 3
        private const val TAB_TO_CHECK = 0
        private const val TAB_CHECKED = 1
        private const val TAB_INFO = 2
    }

}
