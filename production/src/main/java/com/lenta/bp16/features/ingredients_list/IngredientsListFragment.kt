package com.lenta.bp16.features.ingredients_list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.lenta.bp16.R
import com.lenta.bp16.databinding.FragmentIgredientsListBinding
import com.lenta.bp16.databinding.LayoutIngredientsByMaterialBinding
import com.lenta.bp16.databinding.LayoutIngredientsByOrderBinding
import com.lenta.bp16.platform.extention.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.databinding.RecyclerViewKeyHandler
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import com.lenta.shared.utilities.extentions.provideViewModel

class IngredientsListFragment :
        CoreFragment<FragmentIgredientsListBinding, IngredientsListViewModel>(),
        ToolbarButtonsClickListener, ViewPagerSettings {

    private var byOrderRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null
    private var byMaterialRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null

    override fun getLayoutId(): Int {
        return R.layout.fragment_igredients_list
    }

    override fun getPageNumber(): String? {
        return SCREEN_NUMBER
    }

    override fun getViewModel(): IngredientsListViewModel {
        provideViewModel(IngredientsListViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.desc_ingredients_list)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.menu)
        bottomToolbarUiModel.uiModelButton3.show(ButtonDecorationInfo.stickers)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.update)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_1 -> {

            }
            R.id.b_3 -> {

            }
            R.id.b_5 -> {

            }
        }
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return when (position) {
            TAB_BY_ORDER -> initIngredientsByOrderDataBinding(container)
            TAB_BY_MATERIALS -> initIngredientsByMaterialDataBinding(container)
            else -> View(context)
        }
    }

    override fun getTextTitle(position: Int): String {
        return when (position) {
            TAB_BY_ORDER -> getString(R.string.tab_name_by_order)
            TAB_BY_MATERIALS -> getString(R.string.tab_name_by_material)
            else -> throw IllegalArgumentException("Wrong pager position!")
        }
    }

    override fun countTab(): Int {
        return TAB_COUNTS
    }

    private fun initIngredientsByOrderDataBinding(container: ViewGroup): View {
        DataBindingUtil.inflate<LayoutIngredientsByOrderBinding>(LayoutInflater.from(container.context),
                R.layout.layout_ingredients_by_order,
                container,
                false).let { layoutBinding ->

            return layoutBinding.root
        }
    }

    private fun initIngredientsByMaterialDataBinding(container: ViewGroup): View {
        DataBindingUtil.inflate<LayoutIngredientsByMaterialBinding>(LayoutInflater.from(container.context),
                R.layout.layout_ingredients_by_material,
                container,
                false).let { layoutBinding ->

            return layoutBinding.root
        }
    }

    companion object {
        private const val TAB_COUNTS = 2
        private const val TAB_BY_ORDER = 0
        private const val TAB_BY_MATERIALS = 1
        private const val SCREEN_NUMBER = "16/82"
    }

}