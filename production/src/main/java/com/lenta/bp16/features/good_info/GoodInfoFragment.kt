package com.lenta.bp16.features.good_info

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.observe
import com.lenta.bp16.R
import com.lenta.bp16.databinding.FragmentGoodInfoBinding
import com.lenta.bp16.platform.Constants
import com.lenta.bp16.platform.extention.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.*

class GoodInfoFragment : CoreFragment<FragmentGoodInfoBinding, GoodInfoViewModel>(), ToolbarButtonsClickListener {

    private val material by unsafeLazy {
        arguments?.getString(KEY_MATERIAL)
                ?: throw IllegalArgumentException("There is no data in bundle at key $KEY_MATERIAL")
    }

    private val name by unsafeLazy {
        arguments?.getString(KEY_NAME)
                ?: throw IllegalArgumentException("There is no data in bundle at key $KEY_NAME")
    }

    private val selectedEan by unsafeLazy {
        arguments?.getString(KEY_EAN)
                ?: throw IllegalArgumentException("There is no data in bundle at key $KEY_EAN")
    }

    override fun getLayoutId(): Int = R.layout.fragment_good_info

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix(SCREEN_NUMBER)

    override fun getViewModel(): GoodInfoViewModel {
        provideViewModel(GoodInfoViewModel::class.java).let {
            getAppComponent()?.inject(it)
            it.deviceIp.value = context?.getDeviceId()
            it.material.value = material
            it.selectedEan.value = selectedEan
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = getString(R.string.good_card)
        topToolbarUiModel.title.value = getString(R.string.title_good_sap_name, material, name)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back,enabled = false)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.complete, enabled = false)

        /*vm.enteredEanField.observe(viewLifecycleOwner) {
            bottomToolbarUiModel.uiModelButton5.requestFocus()
        }*/

        connectLiveData(vm.enabledCompleteButton, bottomToolbarUiModel.uiModelButton1.enabled)
        connectLiveData(vm.enabledCompleteButton, bottomToolbarUiModel.uiModelButton5.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_5 -> vm.onClickComplete()
        }
    }

    companion object {
        const val SCREEN_NUMBER = Constants.GOODS_INFO_FRAGMENT
        private const val KEY_MATERIAL = "KEY_MATERIAL"
        private const val KEY_EAN = "KEY_EAN"
        private const val KEY_NAME = "KEY_NAME"

        fun newInstance(goodInfo: Bundle) = GoodInfoFragment().apply {
            arguments = bundleOf(KEY_MATERIAL to goodInfo.getString(Constants.GOOD_INFO_MATERIAL),
                                        KEY_EAN to goodInfo.getString(Constants.GOOD_INFO_EAN),
                                        KEY_NAME to goodInfo.getString(Constants.GOOD_INFO_NAME)
            )
        }
    }

}
