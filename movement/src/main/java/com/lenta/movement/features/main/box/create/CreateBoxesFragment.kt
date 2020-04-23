package com.lenta.movement.features.main.box.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.lenta.movement.R
import com.lenta.movement.databinding.FragmentCreateBoxesBinding
import com.lenta.movement.databinding.LayoutBoxCreatePuckerTabBinding
import com.lenta.movement.platform.extensions.getAppComponent
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

class CreateBoxesFragment : CoreFragment<FragmentCreateBoxesBinding, CreateBoxesViewModel>(),
    ViewPagerSettings {

    companion object {
        fun newInstance(productInfo: ProductInfo): CreateBoxesFragment {
            return CreateBoxesFragment().apply {
                this.productInfo = productInfo
            }
        }
    }

    private var productInfo: ProductInfo? by state(null)

    override fun getLayoutId() = R.layout.fragment_create_boxes

    override fun getPageNumber() = "13/18"

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

            when (page) {
                CreateBoxesPage.FILLING -> {
                    bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
                    bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.rollback)
                    bottomToolbarUiModel.uiModelButton4.show(ButtonDecorationInfo.add)
                    bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply)
                }
                CreateBoxesPage.BOX_LIST -> {
                    bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
                    bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.delete)
                    bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.complete)
                }
            }
        })

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return when (CreateBoxesPage.values()[position]) {
            CreateBoxesPage.FILLING -> {
                DataBindingUtil.inflate<LayoutBoxCreatePuckerTabBinding>(
                    LayoutInflater.from(container.context),
                    R.layout.layout_box_create_pucker_tab,
                    container,
                    false
                ).apply {
                    this.vm = vm
                    this.lifecycleOwner = viewLifecycleOwner
                }.root
            }
            CreateBoxesPage.BOX_LIST -> View(context) // TODO
        }
    }

    override fun getTextTitle(position: Int): String {
        return when (CreateBoxesPage.values()[position]) {
            CreateBoxesPage.FILLING -> getString(R.string.create_boxes_filling_tab_title)
            CreateBoxesPage.BOX_LIST -> getString(R.string.create_boxes_box_list_tab_title)
        }
    }

    override fun countTab() = CreateBoxesPage.values().size

}