package com.lenta.bp16.features.add_attribute

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import com.lenta.bp16.R
import com.lenta.bp16.databinding.FragmentAddAttributeBinding
import com.lenta.bp16.platform.extention.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.extentions.unsafeLazy

class AddAttributeFragment : CoreFragment<FragmentAddAttributeBinding, AddAttributeViewModel>(), ToolbarButtonsClickListener {

    private val parentCode: String by unsafeLazy {
        arguments?.getString(KEY_PARENT_CODE)
                ?: throw IllegalArgumentException("There is no argument value with key $KEY_PARENT_CODE")
    }

    private val name: String by unsafeLazy {
        arguments?.getString(KEY_NAME)
                ?: throw IllegalArgumentException("There is no argument value with key $KEY_NAME")
    }

    private val material: String by unsafeLazy {
        arguments?.getString(KEY_MATERIAL)
                ?: throw IllegalArgumentException("There is no argument value with key $KEY_PARENT_CODE")
    }

    private val shelfLife: String by unsafeLazy {
        arguments?.getString(KEY_SHELFLIFE)
                ?: throw IllegalArgumentException("There is no argument value with key $KEY_SHELFLIFE")
    }

    override fun getLayoutId(): Int = R.layout.fragment_add_attribute

    override fun getPageNumber(): String = SCREEN_NUMBER

    override fun getViewModel(): AddAttributeViewModel {
        provideViewModel(AddAttributeViewModel::class.java).let {
            getAppComponent()?.inject(it)
            it.shelfLife.value = shelfLife
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = buildString {
            append(material)
            append(" ")
            append(name)
        }
        topToolbarUiModel.description.value = parentCode
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.complete)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_5 -> vm.onClickComplete()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.updateData()
        vm.getServerTime()
    }

    companion object {
        private const val SCREEN_NUMBER = "16/83"
        private const val KEY_MATERIAL = "KEY_MATERIAL"
        private const val KEY_NAME = "KEY_NAME"
        private const val KEY_PARENT_CODE = "KEY_PARENT_CODE"
        private const val KEY_SHELFLIFE = "KEY_SHELFLIFE"

        fun newInstance(material: String, name: String, parentCode: String, shelfLife: String) = AddAttributeFragment().apply {
            arguments = bundleOf(
                    KEY_MATERIAL to material,
                    KEY_NAME to name,
                    KEY_PARENT_CODE to parentCode,
                    KEY_SHELFLIFE to shelfLife
            )
        }
    }
}