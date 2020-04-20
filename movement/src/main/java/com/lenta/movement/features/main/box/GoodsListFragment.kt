package com.lenta.movement.features.main.box

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import com.lenta.movement.BR
import com.lenta.movement.R
import com.lenta.movement.databinding.FragmentGoodsListBinding
import com.lenta.movement.databinding.LayoutItemGoodsListBinding
import com.lenta.movement.platform.extensions.getAppComponent
import com.lenta.shared.keys.KeyCode
import com.lenta.shared.keys.OnKeyDownListener
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.databinding.DataBindingAdapter
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.databinding.RecyclerViewKeyHandler
import com.lenta.shared.utilities.extentions.getFragmentResultCode
import com.lenta.shared.utilities.extentions.provideViewModel

class GoodsListFragment : CoreFragment<FragmentGoodsListBinding, GoodsListViewModel>(),
    OnScanResultListener,
    OnBackPresserListener,
    OnKeyDownListener {

    private var recyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null

    override fun getLayoutId() = R.layout.fragment_goods_list

    override fun getPageNumber() = "10/06"

    override fun getViewModel(): GoodsListViewModel {
        provideViewModel(GoodsListViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.cleanAll()
        topToolbarUiModel.title.value = getString(R.string.create_box_title)
        topToolbarUiModel.description.value = getString(R.string.create_box_product_list)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.cleanAll()
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.clean)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.complete)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val onClickSelectionListener = View.OnClickListener {
            (it!!.tag as Int).let { position ->
                vm.selectionsHelper.revert(position = position)
                binding?.rv?.adapter?.notifyItemChanged(position)
            }
        }

        binding?.rvConfig = DataBindingRecyclerViewConfig(
            layoutId = R.layout.layout_item_goods_list,
            itemId = BR.vm,
            realisation = object : DataBindingAdapter<LayoutItemGoodsListBinding> {
                override fun onCreate(binding: LayoutItemGoodsListBinding) {
                    // do nothing
                }

                override fun onBind(binding: LayoutItemGoodsListBinding, position: Int) {
                    binding.tvCounter.tag = position
                    binding.tvCounter.setOnClickListener(onClickSelectionListener)
                    binding.selectedForDelete = vm.selectionsHelper.isSelected(position)
                    recyclerViewKeyHandler?.let {
                        binding.root.isSelected = it.isSelected(position)
                    }
                }
            },
            onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                recyclerViewKeyHandler?.let {
                    if (it.isSelected(position)) {
                        vm.onClickItemPosition(position)
                    } else {
                        it.selectPosition(position)
                    }
                }
            }
        )

        recyclerViewKeyHandler = RecyclerViewKeyHandler(
            binding?.rv!!,
            vm.goodsList,
            binding?.lifecycleOwner!!,
            recyclerViewKeyHandler?.posInfo?.value
        )
    }

    override fun onScanResult(data: String) {
        vm.onScanResult(data)
    }

    override fun onFragmentResult(arguments: Bundle) {
        super.onFragmentResult(arguments)
        vm.onResult(arguments.getFragmentResultCode())
    }

    override fun onBackPressed(): Boolean {
        vm.onBackPressed()
        return false
    }

    override fun onKeyDown(keyCode: KeyCode): Boolean {
        recyclerViewKeyHandler?.let {
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
}