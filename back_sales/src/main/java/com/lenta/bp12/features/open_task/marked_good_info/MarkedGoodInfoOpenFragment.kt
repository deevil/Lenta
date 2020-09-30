package com.lenta.bp12.features.open_task.marked_good_info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import com.lenta.bp12.BR
import com.lenta.bp12.R
import com.lenta.bp12.databinding.FragmentMarkedGoodInfoOpenBinding
import com.lenta.bp12.databinding.ItemGoodInfoPropertyBinding
import com.lenta.bp12.databinding.LayoutMarkedGoodInfoOpenPropertiesTabBinding
import com.lenta.bp12.databinding.LayoutMarkedGoodInfoOpenQuantityTabBinding
import com.lenta.bp12.features.create_task.marked_good_info.GoodProperty
import com.lenta.bp12.features.create_task.marked_good_info.GoodPropertyItem
import com.lenta.bp12.model.pojo.Mark
import com.lenta.bp12.platform.extention.getAppComponent
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class MarkedGoodInfoOpenFragment : CoreFragment<FragmentMarkedGoodInfoOpenBinding, MarkedGoodInfoOpenViewModel>(),
        ViewPagerSettings, ToolbarButtonsClickListener, OnScanResultListener, OnBackPresserListener
/*, OnKeyDownListener*/ {

    override fun getLayoutId(): Int = R.layout.fragment_marked_good_info_open

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix(SCREEN_NUMBER)

    override fun getViewModel(): MarkedGoodInfoOpenViewModel {
        val vm = provideViewModel(MarkedGoodInfoOpenViewModel::class.java)
        getAppComponent()?.inject(vm)
        arguments?.let {
            val marks = it.getParcelableArrayList<Mark>(MARKS_KEY)
            val properties = it.getParcelableArrayList<GoodProperty>(PROPERTIES_KEY)
            vm.setupData(marks, properties)
        }
        return vm

    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.good_info)
        connectLiveData(vm.title, topToolbarUiModel.title)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.rollback)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.details)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply, enabled = false)

        connectLiveData(vm.rollbackVisibility, bottomToolbarUiModel.uiModelButton2.visibility)
        connectLiveData(vm.rollbackEnabled, bottomToolbarUiModel.uiModelButton2.enabled)
        connectLiveData(vm.applyEnabled, bottomToolbarUiModel.uiModelButton5.enabled)

        if (vm.isWholesale) {
            bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.close)
            connectLiveData(vm.closeEnabled, bottomToolbarUiModel.uiModelButton4.enabled)
        }
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_2 -> vm.onClickRollback()
            R.id.b_3 -> vm.onClickDetails()
            R.id.b_5 -> vm.onClickApply()
        }
    }

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }

    override fun onBackPressed(): Boolean {
        vm.onBackPressed()
        return false
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return when (position) {
            TAB_QUANTITY_PAGE -> initGoodInfoQuantityTab(container)
            TAB_PROPERTIES_PAGE -> initGoodInfoPropertiesTab(container)
            else -> View(context)
        }
    }

    override fun getTextTitle(position: Int): String {
        return when (position) {
            TAB_QUANTITY_PAGE -> getString(R.string.quantity)
            TAB_PROPERTIES_PAGE -> getString(R.string.properties)
            else -> {
                Logg.e { "Wrong pager position" }
                getString(R.string.error)
            }
        }
    }

    override fun countTab(): Int = TAB_QUANTITY

    private fun initGoodInfoQuantityTab(container: ViewGroup): View {
        val layoutBinding = DataBindingUtil.inflate<LayoutMarkedGoodInfoOpenQuantityTabBinding>(
                LayoutInflater.from(container.context),
                R.layout.layout_marked_good_info_open_quantity_tab,
                container,
                false)

        layoutBinding.vm = vm
        layoutBinding.lifecycleOwner = viewLifecycleOwner

        return layoutBinding.root
    }

    private fun initGoodInfoPropertiesTab(container: ViewGroup): View {
        val layoutBinding = DataBindingUtil.inflate<LayoutMarkedGoodInfoOpenPropertiesTabBinding>(
                LayoutInflater.from(container.context),
                R.layout.layout_marked_good_info_open_properties_tab,
                container,
                false)

        layoutBinding.rvConfig = initRecycleAdapterDataBinding<GoodPropertyItem, ItemGoodInfoPropertyBinding>(
                layoutId = R.layout.item_good_info_property,
                itemId = BR.item,
                recyclerView = layoutBinding.rv,
                items = vm.propertiesItems
        )

        layoutBinding.vm = vm
        layoutBinding.lifecycleOwner = viewLifecycleOwner

        return layoutBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }

    companion object {
        fun newInstance(marks: List<Mark>, properties: List<GoodProperty>): MarkedGoodInfoOpenFragment {
            return MarkedGoodInfoOpenFragment().apply {
                arguments = bundleOf(
                        MARKS_KEY to marks,
                        PROPERTIES_KEY to properties
                )
            }
        }

        const val SCREEN_NUMBER = "12"
        private const val MARKS_KEY = "MARKS_KEY"
        private const val PROPERTIES_KEY = "PROPERTIES_KEY"
        private const val TAB_QUANTITY_PAGE = 0
        private const val TAB_PROPERTIES_PAGE = 1
        private const val TAB_QUANTITY = 2
    }
// FOR TESTING: press digit to scan barcode
//    override fun onKeyDown(keyCode: KeyCode): Boolean {
//        return when (keyCode) {
//            //Сигареты 4600266012142
//            //Блок Мрц 106
//            KeyCode.KEYCODE_0 -> {
//                vm.onScanResult("01046002660121422100000Ph.8005021200.938000.92NGkg+wRXz36kBFjpfwOub5DBIIpD2iS/DMYpZuuDLU0Y3pZt1z20/1ksr4004wfhDhRxu4dgUV4QN96Qtdih9g==")
//                true
//            }
//            //Блок Мрц 100
//            KeyCode.KEYCODE_1 -> {
//                vm.onScanResult("01046002660121422100000Pi.8005020000.938000.92NGkg+wRXz36kBFjpfwOub5DBIIpD2iS/DMYpZuuDLU0Y3pZt1z20/1ksr4004wfhDhRxu4dgUV4QN96Qtdih9g==")
//                true
//            }
//            //4600266011367 Блок Мрц 100
//            KeyCode.KEYCODE_2 -> {
//                vm.onScanResult("01046002660113672100000CP.8005020000.938000.92NGkg+wRXz36kBFjpfwOub5DBIIpD2iS/DMYpZuuDLU0Y3pZt1z20/1ksr4004wfhDhRxu4dgUV4QN96Qtdih9g==")
//                true
//            }
//            //пачка
//            KeyCode.KEYCODE_3 -> {
//                vm.onScanResult("00000046203564000003B01238000")
//                true
//            }
//            //Коробка обуви
//            KeyCode.KEYCODE_4 -> {
//                vm.onScanResult("946060680019389537")
//                true
//            }
//            //Марка из этой коробки
//            KeyCode.KEYCODE_5 -> {
//                vm.onScanResult("010460606832937221bBjpnxLePjMmv.918000.92NGkg+wRXz36kBFjpfwOub5DBIIpD2iS/DMYpZuuDLU0Y3pZt1z20/1ksr4004wfhDhRxu4dgUV4QN96Qtdih9g==")
//                true
//            }
//            //Марка не из этой коробки
//            KeyCode.KEYCODE_6 -> {
//                vm.onScanResult("010460606832938921q8Pk81bQ/9GPR.918000.92NGkg+wRXz36kBFjpfwOub5DBIIpD2iS/DMYpZuuDLU0Y3pZt1z20/1ksr4004wfhDhRxu4dgUV4QN96Qtdih9g==")
//                true
//            }
//            //Марка 198794
//            KeyCode.KEYCODE_7 -> {
//                vm.onScanResult("010467003610609821j3qd?s9G2pJZJ")
//                true
//            }
//            else -> false
//        }
//    }
}
