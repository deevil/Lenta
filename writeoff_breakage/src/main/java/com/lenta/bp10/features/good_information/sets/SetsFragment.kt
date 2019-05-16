package com.lenta.bp10.features.good_information.sets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.lenta.bp10.R
import com.lenta.bp10.databinding.FragmentSetsBinding
import com.lenta.bp10.databinding.LayoutSetsQuantityBinding
import com.lenta.bp10.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.provideViewModel

class SetsFragment :
        CoreFragment<FragmentSetsBinding, SetsViewModel>(),
        ViewPagerSettings,
        PageSelectionListener,
        ToolbarButtonsClickListener {

    var vpTabPosition: Int = 0

    override fun getLayoutId(): Int = R.layout.fragment_sets

    override fun getPageNumber(): String = "10/10"

    override fun getViewModel(): SetsViewModel {
        provideViewModel(SetsViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.set_info)
        /**topToolbarUiModel.title.value = productInfo.description*/
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
            bottomToolbarUiModel.cleanAll()
            if (vpTabPosition == 0) {
                bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
                bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.details)
                bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.add, enabled = false)
                bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply, enabled = false)
            } else {
                bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
                bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.clean, enabled = false)
                bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.add, enabled = false)
                bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply, enabled = false)
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.let {
            it.viewPagerSettings = this
            it.pageSelectionListener=this}

    }

    /**override fun onResume() {
        super.onResume()
        vm.onResume()
    }*/

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        if (position ==0) {
            DataBindingUtil
                    .inflate<LayoutSetsQuantityBinding>(LayoutInflater.from(container.context),
                            R.layout.layout_sets_quantity,
                            container,
                            false).let {
                        it.lifecycleOwner = viewLifecycleOwner
                        it.vm = vm
                        return it.root
                    }
        }

        DataBindingUtil
                .inflate<LayoutSetsQuantityBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_sets_quantity,
                        container,
                        false).let {
                    it.lifecycleOwner = viewLifecycleOwner
                    it.vm = vm
                    return it.root
                }
    }

    override fun getTextTitle(position: Int): String = getString(if (position == 0) R.string.quantity else R.string.components)

    override fun countTab(): Int = 2

    override fun onPageSelected(position: Int) {
        Logg.d { "onPageSelected $position" }
        vpTabPosition = position
        setupBottomToolBar(this.getBottomToolBarUIModel()!!)
    }

    /**override fun onDestroyView() {
        super.onDestroyView()
        getTopToolBarUIModel()?.let {
            it.title.value = getString(R.string.app_title)
        }
    }*/


    /** object {
        fun create(productInfo: ProductInfo): GoodInfoFragment {
            GoodInfoFragment().let {
                it.productInfo = productInfo
                return it
            }
        }

    }*/

    override fun onToolbarButtonClick(view: View) {
        if (vpTabPosition == 0) {
            when (view.id) {
                R.id.b_3 -> vm.onClickDetails()
                R.id.b_4 -> vm.onClickAdd()
                R.id.b_5 -> vm.onClickApply()
            }
        } else {
            when (view.id) {
                R.id.b_3 -> vm.onClickClean()
                R.id.b_4 -> vm.onClickAdd()
                R.id.b_5 -> vm.onClickApply()
            }
        }
    }


}
