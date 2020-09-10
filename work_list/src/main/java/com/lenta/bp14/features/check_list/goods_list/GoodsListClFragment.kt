package com.lenta.bp14.features.check_list.goods_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.lenta.bp14.BR
import com.lenta.bp14.R
import com.lenta.bp14.databinding.FragmentGoodsListClBinding
import com.lenta.bp14.databinding.ItemClGoodQuantityEditableSelectableBinding
import com.lenta.bp14.databinding.LayoutClGoodsListBinding
import com.lenta.bp14.di.CheckListComponent
import com.lenta.shared.di.CoreInjectHelper
import com.lenta.shared.keys.KeyCode
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.KeyDownCoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.databinding.DataBindingAdapter
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.databinding.RecyclerViewKeyHandler
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.provideViewModel

class GoodsListClFragment : KeyDownCoreFragment<FragmentGoodsListClBinding, GoodsListClViewModel>(),
        ToolbarButtonsClickListener, ViewPagerSettings, OnScanResultListener, OnBackPresserListener {

    override fun getLayoutId(): Int = R.layout.fragment_goods_list_cl

    override fun getPageNumber(): String = "14/21"

    override fun getViewModel(): GoodsListClViewModel {
        provideViewModel(GoodsListClViewModel::class.java).let {
            CoreInjectHelper.getComponent(CheckListComponent::class.java)!!.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.list_of_goods)

        connectLiveData(vm.taskName, topToolbarUiModel.title)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton2.show(ButtonDecorationInfo.video)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.delete)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.save)

        connectLiveData(vm.deleteButtonEnabled, bottomToolbarUiModel.uiModelButton3.enabled)
        connectLiveData(vm.saveButtonEnabled, bottomToolbarUiModel.uiModelButton5.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_2 -> if (isGoogleServicesAvailable()) vm.onClickVideo() else vm.showVideoErrorMessage()
            R.id.b_3 -> vm.onClickDelete()
            R.id.b_5 -> vm.onClickSave()
        }
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        DataBindingUtil
                .inflate<LayoutClGoodsListBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_cl_goods_list,
                        container,
                        false).let { layoutBinding ->

                    val onClickSelectionListener = View.OnClickListener {
                        (it!!.tag as Int).let { position ->
                            vm.selectionsHelper.revert(position = position)
                            layoutBinding.rv.adapter?.notifyItemChanged(position)
                        }
                    }

                    layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                            layoutId = R.layout.item_cl_good_quantity_editable_selectable,
                            itemId = BR.good,
                            realisation = object : DataBindingAdapter<ItemClGoodQuantityEditableSelectableBinding> {
                                override fun onCreate(binding: ItemClGoodQuantityEditableSelectableBinding) {
                                }

                                override fun onBind(binding: ItemClGoodQuantityEditableSelectableBinding, position: Int) {
                                    binding.tvItemNumber.tag = position
                                    binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                                    binding.selectedForDelete = vm.selectionsHelper.isSelected(position)
                                    oldRecyclerViewKeyHandler?.let {
                                        binding.root.isSelected = it.isSelected(position)
                                    }
                                }
                            },
                            onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                                oldRecyclerViewKeyHandler?.selectPosition(position)
                            }
                    )

                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner
                    oldRecyclerViewKeyHandler = RecyclerViewKeyHandler(
                            rv = layoutBinding.rv,
                            items = vm.goods,
                            lifecycleOwner = layoutBinding.lifecycleOwner!!,
                            initPosInfo = oldRecyclerViewKeyHandler?.posInfo?.value
                    )

                    return layoutBinding.root
                }
    }

    override fun onAdapterItemClickHandler(position: Int) {
        oldRecyclerViewKeyHandler?.selectPosition(position)
    }


    override fun getTextTitle(position: Int): String {
        return getString(R.string.goods)
    }

    override fun countTab() = 1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }

    override fun onKeyDown(keyCode: KeyCode): Boolean {
        oldRecyclerViewKeyHandler?.let {
            if (!it.onKeyDown(keyCode)) {
                keyCode.digit?.let { digit ->
                    vm.onDigitPressed(digit)
                    return true
                }
                return false
            }
            return true
        }
        return false
    }

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }

    private fun isGoogleServicesAvailable(): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val status = googleApiAvailability.isGooglePlayServicesAvailable(context)
        return status == ConnectionResult.SUCCESS
    }

    override fun onBackPressed(): Boolean {
        return vm.onBackPressed()
    }

}
