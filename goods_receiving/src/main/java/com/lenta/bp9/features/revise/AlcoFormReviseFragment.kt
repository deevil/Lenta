package com.lenta.bp9.features.revise

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
import androidx.databinding.DataBindingUtil
import com.lenta.bp9.databinding.*
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumber
import com.lenta.shared.utilities.state.state

class AlcoFormReviseFragment : CoreFragment<FragmentAlcoFormReviseBinding, AlcoFormReviseViewModel>(), ViewPagerSettings, ToolbarButtonsClickListener {

    private var matnr by state("")
    private var batchNumber by state("")

    override fun getLayoutId(): Int = R.layout.fragment_alco_form_revise

    override fun getPageNumber() = generateScreenNumber()

    override fun getViewModel(): AlcoFormReviseViewModel {
        provideViewModel(AlcoFormReviseViewModel::class.java).let {
            getAppComponent()?.inject(it)
            it.matnr = matnr
            it.batchNumber = batchNumber
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.taskCaption
        topToolbarUiModel.description.value = getString(R.string.alco_form_a_b_revise)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.next)
        connectLiveData(vm.nextEnabled, bottomToolbarUiModel.uiModelButton5.enabled)
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return when (position) {
            0 -> prepareABPartView(container)
            1 -> prepareAdditionalPartOneView(container)
            2 -> prepareAdditionalPartTwoView(container)
            else -> View(context)
        }
    }

    private fun prepareABPartView(container: ViewGroup): View {
        DataBindingUtil
                .inflate<LayoutAlcoFormRevisePartABBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_alco_form_revise_part_a_b,
                        container,
                        false).let { layoutBinding ->
                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner
                    return layoutBinding.root
                }
    }

    private fun prepareAdditionalPartOneView(container: ViewGroup): View {
        DataBindingUtil
                .inflate<LayoutAlcoFormRevisePartOneBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_alco_form_revise_part_one,
                        container,
                        false).let { layoutBinding ->
                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner
                    return layoutBinding.root
                }
    }

    private fun prepareAdditionalPartTwoView(container: ViewGroup): View {
        DataBindingUtil
                .inflate<LayoutAlcoFormRevisePartTwoBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_alco_form_revise_part_two,
                        container,
                        false).let { layoutBinding ->
                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner
                    return layoutBinding.root
                }
    }

    override fun getTextTitle(position: Int): String {
        return when (position) {
            0 -> getString(R.string.a_and_b_part)
            1 -> getString(R.string.additional_list_p1)
            2 -> getString(R.string.additional_list_p2)
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

    companion object {
        fun create(matnr: String, batchNumber: String): AlcoFormReviseFragment {
            val fragment = AlcoFormReviseFragment()
            fragment.matnr = matnr
            fragment.batchNumber = batchNumber
            return fragment
        }
    }

    override fun onResume() {
        super.onResume()
        vm.onResume()
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_5 -> vm.onClickNext()
        }
    }
}
