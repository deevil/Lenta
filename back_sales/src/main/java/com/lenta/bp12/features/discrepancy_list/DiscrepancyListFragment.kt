package com.lenta.bp12.features.discrepancy_list

import com.lenta.bp12.R
import com.lenta.bp12.databinding.FragmentDiscrepancyListBinding
import com.lenta.bp12.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class DiscrepancyListFragment : CoreFragment<FragmentDiscrepancyListBinding, DiscrepancyListViewModel>() {

    override fun getLayoutId(): Int = R.layout.fragment_discrepancy_list

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix("Specify screen number!")

    override fun getViewModel(): DiscrepancyListViewModel {
        provideViewModel(DiscrepancyListViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

        //topToolbarUiModel.title.value = context?.getAppInfo()
        //topToolbarUiModel.description.value = getString(R.string.app_name)

        //topToolbarUiModel.uiModelButton1.show(ImageButtonDecorationInfo.settings)
        //topToolbarUiModel.uiModelButton2.show(ImageButtonDecorationInfo.exitFromApp)

        //connectLiveData(vm.title, topToolbarUiModel.title)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

        //bottomToolbarUiModel.hide()

        //bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        //bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.complete)

        //connectLiveData(vm.completeEnabled, getBottomToolBarUIModel()!!.uiModelButton5.enabled)
    }

    /*override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_topbar_1 -> vm.onClickAuxiliaryMenu()
            R.id.b_5 -> vm.onClickComplete()
        }
    }*/


    /*override fun onBackPressed(): Boolean {
        vm.onBackPressed()
        return false
    }*/

}
