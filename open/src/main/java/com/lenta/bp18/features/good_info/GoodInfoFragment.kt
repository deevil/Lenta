package com.lenta.bp18.features.good_info

import android.view.View
import androidx.core.os.bundleOf
import com.lenta.bp18.R
import com.lenta.bp18.databinding.FragmentGoodInfoBinding
import com.lenta.bp18.model.pojo.GoodParams
import com.lenta.bp18.platform.Constants
import com.lenta.bp18.platform.extention.getAppComponent
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.*

class GoodInfoFragment : CoreFragment<FragmentGoodInfoBinding, GoodInfoViewModel>(), ToolbarButtonsClickListener, OnBackPresserListener {

    private val goodParams by unsafeLazy {
        arguments?.getParcelable<GoodParams>(KEY_GOOD_PARAMS)
                ?: throw IllegalArgumentException("There is no data in bundle at key $KEY_GOOD_PARAMS")
    }

    override fun getLayoutId(): Int = R.layout.fragment_good_info

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix(SCREEN_NUMBER)

    override fun getViewModel(): GoodInfoViewModel {

        provideViewModel(GoodInfoViewModel::class.java).let {
            getAppComponent()?.inject(it)
            it.selectedEan.value = goodParams.ean
            it.weight.value = goodParams.weight.toDoubleWeight()
            it.partNumberField.value = goodParams.batchNumber
            return it
        }
    }

    override fun onResume() {
        super.onResume()
        vm.requestFocusToQuantityField.value = true
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        val material = goodParams.material
        val name = goodParams.name

        topToolbarUiModel.description.value = getString(R.string.good_card)
        topToolbarUiModel.title.value = getString(R.string.title_good_sap_name,
                material,
                name)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.complete, enabled = false)

        connectLiveData(vm.completeButtonEnabled,bottomToolbarUiModel.uiModelButton5.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_1 -> vm.onClickBack()
            R.id.b_5 -> vm.onClickComplete()
        }
    }

    override fun onBackPressed(): Boolean {
        vm.onClickBack()
        return false
    }

    companion object {
        const val SCREEN_NUMBER = Constants.GOODS_INFO_FRAGMENT

        private const val KEY_GOOD_PARAMS = "KEY_GOOD_PARAMS"

        fun newInstance(goodParams: GoodParams) = GoodInfoFragment().apply {
            arguments = bundleOf(KEY_GOOD_PARAMS to goodParams)
        }

    }

}