package com.lenta.bp12.features.create_task.task_content

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.lenta.bp12.BR
import com.lenta.bp12.R
import com.lenta.bp12.databinding.*
import com.lenta.bp12.platform.extention.getAppComponent
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class TaskContentFragment : CoreFragment<FragmentTaskContentBinding, TaskContentViewModel>(),
        ToolbarButtonsClickListener, ViewPagerSettings, OnBackPresserListener, OnScanResultListener/*, OnKeyDownListener*/ {

    override fun getLayoutId(): Int = R.layout.fragment_task_content

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix(SCREEN_NUMBER)

    override fun getViewModel(): TaskContentViewModel {
        provideViewModel(TaskContentViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.good_list)
        connectLiveData(vm.title, topToolbarUiModel.title)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.delete, enabled = false)
        bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.print, enabled = false)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.save, enabled = false)

        connectLiveData(vm.deleteEnabled, bottomToolbarUiModel.uiModelButton3.enabled)
        connectLiveData(vm.printEnabled, bottomToolbarUiModel.uiModelButton4.enabled)
        connectLiveData(vm.printVisibility, bottomToolbarUiModel.uiModelButton4.visibility)
        connectLiveData(vm.saveEnabled, bottomToolbarUiModel.uiModelButton5.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_3 -> vm.onClickDelete()
            R.id.b_4 -> vm.onPrint()
            R.id.b_5 -> vm.onClickSave()
        }
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return when (position) {
            TAB_GOODS -> initTaskContentGoods(container)
            TAB_BASKETS -> {
                if (vm.manager.isWholesaleTaskType) {
                    initTaskContentWholesaleBaskets(container)
                } else {
                    initTaskContentCommonBaskets(container)
                }
            }
            else -> View(context)
        }
    }

    private fun initTaskContentGoods(container: ViewGroup): View {
        val layoutBinding = DataBindingUtil.inflate<LayoutTaskContentGoodsBinding>(LayoutInflater.from(container.context),
                R.layout.layout_task_content_goods,
                container,
                false)

        val onClickSelectionListener = View.OnClickListener {
            (it.tag as? Int)?.let{ position ->
                vm.goodSelectionsHelper.revert(position = position)
                layoutBinding.rv.adapter?.notifyItemChanged(position)
            }
        }

        layoutBinding.rvConfig = initRecycleAdapterDataBinding(
                layoutId = R.layout.item_task_content_good,
                itemId = BR.item,
                onItemBind = { binding: ItemTaskContentGoodBinding, position: Int ->
                    binding.tvItemNumber.tag = position
                    binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                    binding.selectedForDelete = vm.goodSelectionsHelper.isSelected(position)
                },
                keyHandlerId = TAB_GOODS,
                recyclerView = layoutBinding.rv,
                items = vm.goods,
                onClickHandler = vm::onClickItemPosition
        )

        layoutBinding.vm = vm
        layoutBinding.lifecycleOwner = viewLifecycleOwner

        return layoutBinding.root

    }

    private fun initTaskContentCommonBaskets(container: ViewGroup): View {
        DataBindingUtil.inflate<LayoutTaskContentCommonBasketsBinding>(LayoutInflater.from(container.context),
                R.layout.layout_task_content_common_baskets,
                container,
                false).let { layoutBinding ->

            val onClickSelectionListener = View.OnClickListener {
                (it.tag as? Int)?.let{ position ->
                    vm.basketSelectionsHelper.revert(position = position)
                    layoutBinding.rv.adapter?.notifyItemChanged(position)
                }
            }

            layoutBinding.rvConfig = initRecycleAdapterDataBinding(
                    layoutId = R.layout.item_task_content_common_basket,
                    itemId = BR.item,
                    onItemBind = { binding: ItemTaskContentCommonBasketBinding, position: Int ->
                        binding.tvItemNumber.tag = position
                        binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                        binding.selectedForDelete = vm.basketSelectionsHelper.isSelected(position)
                    },
                    keyHandlerId = TAB_BASKETS,
                    recyclerView = layoutBinding.rv,
                    items = vm.commonBaskets,
                    onClickHandler = vm::onClickItemPosition
            )

            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner

            return layoutBinding.root
        }
    }

    private fun initTaskContentWholesaleBaskets(container: ViewGroup): View {
        DataBindingUtil.inflate<LayoutTaskContentWholesaleBasketsBinding>(LayoutInflater.from(container.context),
                R.layout.layout_task_content_wholesale_baskets,
                container,
                false).let { layoutBinding ->

            val onClickSelectionListener = View.OnClickListener {
                (it.tag as? Int)?.let{ position ->
                    vm.basketSelectionsHelper.revert(position = position)
                    layoutBinding.rv.adapter?.notifyItemChanged(position)
                }
            }

            layoutBinding.rvConfig = initRecycleAdapterDataBinding(
                    layoutId = R.layout.item_wholesale_basket,
                    itemId = BR.item,
                    onItemBind = { binding: ItemWholesaleBasketBinding, position: Int ->
                        binding.tvItemNumber.tag = position
                        binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                        binding.selectedForDelete = vm.basketSelectionsHelper.isSelected(position)
                    },
                    keyHandlerId = TAB_BASKETS,
                    recyclerView = layoutBinding.rv,
                    items = vm.wholesaleBaskets,
                    onClickHandler = vm::onClickItemPosition
            )

            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner

            return layoutBinding.root
        }
    }

    override fun getTextTitle(position: Int): String {
        return when (position) {
            TAB_GOODS -> getString(R.string.processed)
            TAB_BASKETS -> getString(R.string.baskets)
            else -> throw IllegalArgumentException("Wrong pager position!")
        }
    }

    override fun countTab(): Int {
        return TABS
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }

    override fun onBackPressed(): Boolean {
        vm.onBackPressed()
        return false
    }

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }

    companion object {
        const val SCREEN_NUMBER = "17"

        private const val TABS = 2
        private const val TAB_GOODS = 0
        private const val TAB_BASKETS = 1
    }

//// FOR TESTING: press digit to scan barcode
//    override fun onKeyDown(keyCode: KeyCode): Boolean {
//        when (keyCode) {
//            KeyCode.KEYCODE_0 -> { // Блок
//                vm.onScanResult("01046002660113672100000Ce.8005021200.938000.92NGkg+wRXz36kBFjpfwOub5DBIIpD2iS/DMYpZuuDLU0Y3pZt1z20/1ksr4004wfhDhRxu4dgUV4QN96Qtdih9g==")
//                return true
//            }
//            KeyCode.KEYCODE_1 -> { // Пачка
//                vm.onScanResult("00000046203564000001A01238000")
//                return true
//            }
//            KeyCode.KEYCODE_2 -> { // Тапки
//                vm.onScanResult("010871947716364521Gl8hHnSNy0SsN91800092NGkg+wRXz36kBFjpfwOub5DBIIpD2iS/DMYpZuuDLU0Y3pZt1z20/1ksr4004wfhDhRxu4dgUV4QN96Qtdih9g==")
//                return true
//            }
//            KeyCode.KEYCODE_3 -> { // Тапки
//                vm.onScanResult("8719477163645")
//                return true
//            }
//            KeyCode.KEYCODE_4 -> { // Коробка 082682
//                vm.onScanResult("03000042907513119000404111")
//                return true
//            }
//            KeyCode.KEYCODE_5 -> { // ШК товара 335533
//                vm.onScanResult("4606068253837")
//                return true
//            }
//            KeyCode.KEYCODE_6 -> { //011189 граммовый ШК: 2811189054009 (вес 5.4 кг)
//                vm.onScanResult("2811189054009")
//                return true
//            }
//            KeyCode.KEYCODE_7 -> { // Сыр 305346
//                vm.onScanResult("2406786000018")
//                return true
//            }
//        }
//        return false
//    }

}
