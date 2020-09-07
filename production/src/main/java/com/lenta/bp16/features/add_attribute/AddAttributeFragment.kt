package com.lenta.bp16.features.add_attribute

import android.view.View
import com.lenta.bp16.R
import com.lenta.bp16.databinding.FragmentAddAttributeBinding
import com.lenta.bp16.platform.extention.getAppComponent
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel

class AddAttributeFragment : CoreFragment<FragmentAddAttributeBinding, AddAttributeViewModel>(), ToolbarButtonsClickListener, OnBackPresserListener {

    override fun getLayoutId(): Int = R.layout.fragment_add_attribute

    override fun getPageNumber(): String = SCREEN_NUMBER

    override fun getViewModel(): AddAttributeViewModel {
        provideViewModel(AddAttributeViewModel::class.java).let{
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        TODO("Not yet implemented")
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        TODO("Not yet implemented")
    }

    override fun onToolbarButtonClick(view: View) {
        TODO("Not yet implemented")
    }

    override fun onBackPressed(): Boolean {
        TODO("Not yet implemented")
    }

    companion object{
        private const val SCREEN_NUMBER = "16/06"
    }

}