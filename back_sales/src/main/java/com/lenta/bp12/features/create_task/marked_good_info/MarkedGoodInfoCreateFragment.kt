package com.lenta.bp12.features.create_task.marked_good_info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import com.lenta.bp12.BR
import com.lenta.bp12.R
import com.lenta.bp12.databinding.FragmentMarkedGoodInfoCreateBinding
import com.lenta.bp12.databinding.ItemGoodInfoPropertyBinding
import com.lenta.bp12.databinding.LayoutMarkedGoodInfoCreatePropertiesTabBinding
import com.lenta.bp12.databinding.LayoutMarkedGoodInfoCreateQuantityTabBinding
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

class MarkedGoodInfoCreateFragment : CoreFragment<FragmentMarkedGoodInfoCreateBinding, MarkedGoodInfoCreateViewModel>(),
        ViewPagerSettings, ToolbarButtonsClickListener, OnScanResultListener, OnBackPresserListener {

    override fun getLayoutId(): Int = R.layout.fragment_marked_good_info_create

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix(SCREEN_NUMBER)

    override fun getViewModel(): MarkedGoodInfoCreateViewModel {
        val vm = provideViewModel(MarkedGoodInfoCreateViewModel::class.java)
        getAppComponent()?.inject(vm)
        arguments?.let{
            val marks = it.getParcelableArrayList<Mark>(MARKS_KEY)
            marks?.let { listOfMarks ->
                vm.tempMarks.value?.addAll(listOfMarks)
                Logg.e { marks.toString() }
            } ?: Logg.e { "marks empty "}
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
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_2 -> vm.onClickRollback()
            //R.id.b_3 -> vm.onClickDetails()
            R.id.b_3 -> vm.onScanResult("01046002660113672100000BX.8005012345.938000.92NGkg+wRXz36kBFjpfwOub5DBIIpD2iS/DMYpZuuDLU0Y3pZt1z20/1ksr4004wfhDhRxu4dgUV4QN96Qtdih9g==") // Блок
//            R.id.b_3 -> vm.onScanResult("00000046203564000001A01238000") // Пачка
            //R.id.b_3 -> vm.onScanResult("147300249826851018001FZSIZAB5I6KZKWEQKPKZJHW6MYKVGAETXLPV7M5AIF7OXTQFIM347EWQGXAK65QGJFKTR7EQDHJQTJFSW5DNWTBU3BRLKVM7D6YZMYRBV6IOQY5ZXLPKLBHUZPBTRFTLQ") // Марка
            //R.id.b_3 -> vm.onScanResult("1734001784926710180016BZ3532QMZKOBPRTXTL7BZMZ3YNNMK53PXMB3ZU66TJ3SNVFR7YTCYVLOPKUNBQIG5XXLKNYYWMWGGUXJLVHB2NLSMF6ACBJDB73IUKGGSAEOWKBY7TW7FZ5BLIT3YT2Y") // SAP-код: 270202156641
            //R.id.b_3 -> vm.onScanResult("236200647504871018001FCCBM6EJ4RTKG5J6SZPIOVDIA4G3QGAZLK3HVONWWBVHXJYO3HOAX633MX756X27L27QPWSTGUNJM5IZL2X67XID6FSVVZAFI5OXWE5XJNHQMELI76JC45KQN2GH5VD7Y") // SAP-код: 444877
            //R.id.b_3 -> vm.onScanResult("22N00000XOIJT87CH2W0123456789012345678901234567890123456789000000001") // Марка 156641
            //R.id.b_3 -> vm.onScanResult("22N00001CRDKFRWFBZ90123456789012345678901234567890123456789000000001") // Марка 377456
            //R.id.b_3 -> vm.onScanResult("22N00002NWKKIF6RWF30123456789012345678901234567890123456789000000004") // Партия
            //R.id.b_3 -> vm.onScanResult("03000048752210319000100516") // Коробка
            //R.id.b_3 -> vm.onScanResult("01000000637810119000001340") // Коробка
            //R.id.b_3 -> vm.onScanResult("03000042907513119000404111") // Коробка 082682
            //R.id.b_3 -> vm.onScanResult("4607055090121") // ШК
            //R.id.b_3 -> vm.onScanResult("4607149780501") // ШК
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

    override fun onResume() {
        super.onResume()
        vm.updateData()
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
        val layoutBinding = DataBindingUtil.inflate<LayoutMarkedGoodInfoCreateQuantityTabBinding>(
                LayoutInflater.from(container.context),
                R.layout.layout_marked_good_info_create_quantity_tab,
                container,
                false)
        layoutBinding.vm = vm
        layoutBinding.lifecycleOwner = viewLifecycleOwner

        return layoutBinding.root
    }

    private fun initGoodInfoPropertiesTab(container: ViewGroup): View {
        val layoutBinding = DataBindingUtil.inflate<LayoutMarkedGoodInfoCreatePropertiesTabBinding>(
                LayoutInflater.from(container.context),
                R.layout.layout_marked_good_info_create_properties_tab,
                container,
                false)

        layoutBinding.rvConfig = initRecycleAdapterDataBinding<ItemGoodInfoPropertyBinding>(
                layoutId = R.layout.item_good_info_property,
                itemId = BR.item
        )

        layoutBinding.vm = vm
        layoutBinding.lifecycleOwner = viewLifecycleOwner

        recyclerViewKeyHandler = initRecyclerViewKeyHandler(
                recyclerView = layoutBinding.rv,
                items = vm.propertiesItems,
                previousPosInfo = recyclerViewKeyHandler?.posInfo?.value,
                onClickHandler = vm::onClickItemPosition
        )
        return layoutBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }

    companion object {
        fun newInstance(marks: List<Mark>) : MarkedGoodInfoCreateFragment {
            return MarkedGoodInfoCreateFragment().apply {
                arguments = bundleOf(
                        MARKS_KEY to marks
                )
            }
        }

        const val SCREEN_NUMBER = "12"
        private const val MARKS_KEY = "MARKS_KEY"
        private const val TAB_QUANTITY_PAGE = 0
        private const val TAB_PROPERTIES_PAGE = 1
        private const val TAB_QUANTITY = 2
    }

}
