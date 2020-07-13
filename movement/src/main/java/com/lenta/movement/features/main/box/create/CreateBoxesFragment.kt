package com.lenta.movement.features.main.box.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.lenta.movement.BR
import com.lenta.movement.R
import com.lenta.movement.databinding.FragmentCreateBoxesBinding
import com.lenta.movement.databinding.LayoutBoxCreateBoxListTabBinding
import com.lenta.movement.databinding.LayoutBoxCreatePuckerTabBinding
import com.lenta.movement.databinding.LayoutItemBoxListBinding
import com.lenta.movement.models.ProductInfo
import com.lenta.movement.platform.extensions.getAppComponent
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.databinding.*
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

class CreateBoxesFragment : CoreFragment<FragmentCreateBoxesBinding, CreateBoxesViewModel>(),
    ToolbarButtonsClickListener,
    ViewPagerSettings,
    OnScanResultListener,
    OnBackPresserListener {

    private var productInfo: ProductInfo? by state(null)

    override fun getLayoutId() = R.layout.fragment_create_boxes

    override fun getPageNumber() = PAGE_NUMBER

    override fun getViewModel(): CreateBoxesViewModel {
        provideViewModel(CreateBoxesViewModel::class.java).let { vm ->
            getAppComponent()?.inject(vm)
            productInfo?.let {
                vm.productInfo.value = it
            }
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
        topToolbarUiModel.description.value = getString(R.string.goods_info)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        vm.selectedPage.observe(this, Observer { page ->
            bottomToolbarUiModel.cleanAll()

            bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply)

            connectLiveData(vm.addAndApplyEnabled, bottomToolbarUiModel.uiModelButton5.enabled)

            when (page) {
                CreateBoxesPage.FILLING -> {
                    bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
                    bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.rollback)
                    bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.add)

                    connectLiveData(vm.rollbackEnabled, bottomToolbarUiModel.uiModelButton2.enabled)
                    connectLiveData(vm.addAndApplyEnabled, bottomToolbarUiModel.uiModelButton4.enabled)
                }
                CreateBoxesPage.BOX_LIST -> {
                    bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
                    bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.delete)

                    connectLiveData(vm.deleteEnabled, bottomToolbarUiModel.uiModelButton3.enabled)
                }
            }
        })

    }

    override fun onToolbarButtonClick(view: View) {
        when (vm.selectedPage.value) {
            CreateBoxesPage.FILLING -> {
                when (view.id) {
                    R.id.b_2 -> vm.onRollbackClick()
                    R.id.b_4 -> vm.onAddClick()
                    R.id.b_5 -> vm.onCompleteClick()
                }
            }
            CreateBoxesPage.BOX_LIST -> {
                when (view.id) {
                    R.id.b_3 -> vm.onDeleteClick()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }

    override fun onResume() {
        super.onResume()
        vm.onResume()
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return when (CreateBoxesPage.values()[position]) {
            CreateBoxesPage.FILLING -> {
                DataBindingUtil.inflate<LayoutBoxCreatePuckerTabBinding>(
                    LayoutInflater.from(container.context),
                    R.layout.layout_box_create_pucker_tab,
                    container,
                    false
                ).also { layoutBinding ->
                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner
                }.root
            }
            CreateBoxesPage.BOX_LIST -> {
                DataBindingUtil.inflate<LayoutBoxCreateBoxListTabBinding>(
                    LayoutInflater.from(container.context),
                    R.layout.layout_box_create_box_list_tab,
                    container,
                    false
                ).also { layoutBinding ->
                    vm.boxList.observe(this, Observer {
                        setupRecyclerView(layoutBinding.recyclerView, it, layoutBinding.rvConfig)
                    })

                    val onClickSelectionListener = View.OnClickListener {
                        (it.tag as? Int)?.let { position ->
                            vm.selectionsHelper.revert(position = position)
                            layoutBinding?.recyclerView?.adapter?.notifyItemChanged(position)
                        }
                    }

                    layoutBinding.vm = vm
                    layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                        layoutId = R.layout.layout_item_box_list,
                        itemId = BR.item,
                        realisation = object : DataBindingAdapter<LayoutItemBoxListBinding> {
                            override fun onCreate(binding: LayoutItemBoxListBinding) = Unit

                            override fun onBind(binding: LayoutItemBoxListBinding, position: Int) {
                                binding.counterText.tag = position
                                binding.counterText.setOnClickListener(onClickSelectionListener)
                                binding.selectedForDelete = vm.selectionsHelper.isSelected(position)
                                recyclerViewKeyHandler?.let {
                                    binding.root.isSelected = it.isSelected(position)
                                }
                            }
                        },
                        onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                            recyclerViewKeyHandler?.let {
                                if (it.isSelected(position)) {
                                    //vm.onClickItemPosition(position)
                                } else {
                                    it.selectPosition(position)
                                }
                            }
                        }
                    )

                    binding?.lifecycleOwner?.let { lifecycleOwner ->
                        recyclerViewKeyHandler = RecyclerViewKeyHandler(
                                layoutBinding.recyclerView,
                                vm.boxList,
                                lifecycleOwner,
                                recyclerViewKeyHandler?.posInfo?.value
                        )
                    }
                }.root
            }
        }
    }

    override fun getTextTitle(position: Int): String {
        return when (CreateBoxesPage.values()[position]) {
            CreateBoxesPage.FILLING -> getString(R.string.create_boxes_filling_tab_title)
            CreateBoxesPage.BOX_LIST -> getString(R.string.create_boxes_box_list_tab_title)
        }
    }

    override fun countTab() = CreateBoxesPage.values().size

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }

    override fun onBackPressed(): Boolean {
        vm.onBackPressed()
        return false
    }

    companion object {
        private const val PAGE_NUMBER = "13/18"

        fun newInstance(productInfo: ProductInfo): CreateBoxesFragment {
            return CreateBoxesFragment().apply {
                this.productInfo = productInfo
            }
        }
    }
}