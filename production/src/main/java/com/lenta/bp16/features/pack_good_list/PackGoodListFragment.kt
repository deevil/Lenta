package com.lenta.bp16.features.pack_good_list

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import com.lenta.bp16.BR
import com.lenta.bp16.R
import com.lenta.bp16.databinding.FragmentPackGoodListBinding
import com.lenta.bp16.databinding.ItemPackGoodListBinding
import com.lenta.bp16.platform.extention.getAppComponent
import com.lenta.shared.keys.KeyCode
import com.lenta.shared.keys.OnKeyDownListener
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.fragment.KeyDownCoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.databinding.DataBindingAdapter
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.databinding.RecyclerViewKeyHandler
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class PackGoodListFragment : KeyDownCoreFragment<FragmentPackGoodListBinding, PackGoodListViewModel>(),
        ToolbarButtonsClickListener, OnBackPresserListener {

    override fun getLayoutId(): Int = R.layout.fragment_pack_good_list

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix(SCREEN_NUMBER)

    override fun getViewModel(): PackGoodListViewModel {
        provideViewModel(PackGoodListViewModel::class.java).let {
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
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.complete)

        connectLiveData(vm.completeEnabled, getBottomToolBarUIModel()!!.uiModelButton5.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_5 -> vm.onClickComplete()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initRvConfig()
    }

    private fun initRvConfig() {
        binding?.let { layoutBinding ->
            layoutBinding.rvConfig = initRecycleAdapterDataBinding<ItemPackGoodListBinding>(
                    layoutId = R.layout.item_pack_good_list,
                    itemId = BR.item
            )

            recyclerViewKeyHandler = initRecyclerViewKeyHandler(
                    recyclerView = layoutBinding.rv,
                    items = vm.packGoods,
                    previousPosInfo = recyclerViewKeyHandler?.posInfo?.value,
                    onClickHandler = vm::onClickItemPosition
            )
        }
    }

    override fun onBackPressed(): Boolean {
        vm.onBackPressed()
        return false
    }

    companion object {
        const val SCREEN_NUMBER = "32"
    }
}
