package com.lenta.bp12.features.task_composition

import com.lenta.bp12.R
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
import com.lenta.bp12.databinding.*
import com.lenta.bp12.platform.extention.getAppComponent
import com.lenta.bp12.BR
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.utilities.databinding.DataBindingAdapter
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.databinding.RecyclerViewKeyHandler
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix

class TaskCompositionFragment : CoreFragment<FragmentTaskCompositionBinding, TaskCompositionViewModel>(),
        ToolbarButtonsClickListener, ViewPagerSettings {

    private var goodRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null
    private var basketRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null

    override fun getLayoutId(): Int = R.layout.fragment_task_composition

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix("11")

    override fun getViewModel(): TaskCompositionViewModel {
        provideViewModel(TaskCompositionViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.task_composition)

        connectLiveData(vm.title, topToolbarUiModel.title)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.delete, enabled = false)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.save, enabled = false)

        connectLiveData(vm.deleteEnabled, getBottomToolBarUIModel()!!.uiModelButton3.enabled)
        connectLiveData(vm.saveEnabled, getBottomToolBarUIModel()!!.uiModelButton5.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_3 -> vm.onClickDelete()
            R.id.b_5 -> vm.onClickSave()
        }
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return when (position) {
            0 -> initTaskCompositionGoods(container)
            1 -> initTaskCompositionBaskets(container)
            else -> View(context)
        }
    }

    private fun initTaskCompositionGoods(container: ViewGroup): View {
        DataBindingUtil.inflate<LayoutTaskCompositionGoodsBinding>(LayoutInflater.from(container.context),
                R.layout.layout_task_composition_goods,
                container,
                false).let { layoutBinding ->

            val onClickSelectionListener = View.OnClickListener {
                (it!!.tag as Int).let { position ->
                    vm.goodSelectionsHelper.revert(position = position)
                    layoutBinding.rv.adapter?.notifyItemChanged(position)
                }
            }

            layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                    layoutId = R.layout.item_task_composition_good,
                    itemId = BR.item,
                    realisation = object : DataBindingAdapter<ItemTaskCompositionGoodBinding> {
                        override fun onCreate(binding: ItemTaskCompositionGoodBinding) {
                        }

                        override fun onBind(binding: ItemTaskCompositionGoodBinding, position: Int) {
                            binding.tvItemNumber.tag = position
                            binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                            binding.selectedForDelete = vm.goodSelectionsHelper.isSelected(position)
                            goodRecyclerViewKeyHandler?.let {
                                binding.root.isSelected = it.isSelected(position)
                            }
                        }
                    }
            )

            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner
            return layoutBinding.root
        }
    }

    private fun initTaskCompositionBaskets(container: ViewGroup): View {
        DataBindingUtil.inflate<LayoutTaskCompositionBasketsBinding>(LayoutInflater.from(container.context),
                R.layout.layout_task_composition_baskets,
                container,
                false).let { layoutBinding ->

            val onClickSelectionListener = View.OnClickListener {
                (it!!.tag as Int).let { position ->
                    vm.basketSelectionsHelper.revert(position = position)
                    layoutBinding.rv.adapter?.notifyItemChanged(position)
                }
            }

            layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                    layoutId = R.layout.item_task_composition_basket,
                    itemId = BR.item,
                    realisation = object : DataBindingAdapter<ItemTaskCompositionBasketBinding> {
                        override fun onCreate(binding: ItemTaskCompositionBasketBinding) {
                        }

                        override fun onBind(binding: ItemTaskCompositionBasketBinding, position: Int) {
                            binding.tvItemNumber.tag = position
                            binding.tvItemNumber.setOnClickListener(onClickSelectionListener)
                            binding.selectedForDelete = vm.basketSelectionsHelper.isSelected(position)
                            basketRecyclerViewKeyHandler?.let {
                                binding.root.isSelected = it.isSelected(position)
                            }
                        }
                    }
            )

            layoutBinding.vm = vm
            layoutBinding.lifecycleOwner = viewLifecycleOwner
            return layoutBinding.root
        }
    }

    override fun getTextTitle(position: Int): String {
        return when (position) {
            0 -> getString(R.string.goods)
            1 -> getString(R.string.baskets)
            else -> throw IllegalArgumentException("Wrong pager position!")
        }
    }

    override fun countTab(): Int {
        return 2
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }

}
