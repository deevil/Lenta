package com.lenta.bp16.features.add_attribute

import android.os.Bundle
import android.view.View
import com.lenta.bp16.R
import com.lenta.bp16.databinding.FragmentAddAttributeBinding
import com.lenta.bp16.platform.extention.getAppComponent
import com.lenta.shared.platform.activity.OnBackPresserListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel

class AddAttributeFragment : CoreFragment<FragmentAddAttributeBinding, AddAttributeViewModel>(), ToolbarButtonsClickListener {

    override fun getLayoutId(): Int = R.layout.fragment_add_attribute

    override fun getPageNumber(): String = SCREEN_NUMBER

    override fun getViewModel(): AddAttributeViewModel {
        provideViewModel(AddAttributeViewModel::class.java).let{
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
       /* topToolbarUiModel.title.value = buildString {
            append(parentCode)
            append(" ")
            append(name)
        }
        topToolbarUiModel.description.value = ltxa1*/
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.complete)
    }

    override fun onToolbarButtonClick(view: View) {
        when(view.id){
            //R.id.b_1 -> vm.onClickBack()
            R.id.b_5 -> vm.onClickComplete()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.updateData()
    }

    companion object{
        private const val SCREEN_NUMBER = "16/06"
    }

}