package com.lenta.bp9.features.revise.composite_doc

import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import com.lenta.bp9.BR
import com.lenta.bp9.R
import com.lenta.bp9.databinding.FragmentCompositeDocReviseBinding
import com.lenta.bp9.databinding.ItemTileListCompositeDocReviseBinding
import com.lenta.bp9.model.task.revise.DeliveryDocumentRevise
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

class CompositeDocReviseFragment : CoreFragment<FragmentCompositeDocReviseBinding, CompositeDocReviseViewModel>(), ToolbarButtonsClickListener {

    companion object {
        fun create(document: DeliveryDocumentRevise): CompositeDocReviseFragment {
            CompositeDocReviseFragment().let {
                it.document = document
                return it
            }
        }
    }

    private var document by state<DeliveryDocumentRevise?>(null)

    override fun getLayoutId(): Int = R.layout.fragment_composite_doc_revise

    override fun getPageNumber(): String = "09/110"

    override fun getViewModel(): CompositeDocReviseViewModel {
        provideViewModel(CompositeDocReviseViewModel::class.java).let { vm ->
            getAppComponent()?.inject(vm)
            vm.document.value = this.document
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.taskCaption
        topToolbarUiModel.description.value = getString(R.string.composite_doc_revise)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.refusal)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.apply)
        connectLiveData(vm.enabledApplyBtn, bottomToolbarUiModel.uiModelButton5.enabled)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRvConfig()
    }

    private fun initRvConfig() {
        binding?.let { layoutBinding ->
            layoutBinding.rvConfig = oldInitRecycleAdapterDataBinding(
                    layoutId = R.layout.item_tile_list_composite_doc_revise,
                    itemId = BR.item,
                    onAdapterItemBind = { binding: ItemTileListCompositeDocReviseBinding, position: Int ->
                        binding.cbChecked.setOnClickListener { view ->
                            val cb = view as? CheckBox
                            cb?.let { vm.checkedChanged(position, it.isChecked) }
                        }
                    }
            )

            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner
        }
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_2 -> vm.onClickRefusal()
            R.id.b_5 -> vm.onClickApply()
        }
    }

}
