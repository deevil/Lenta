package com.lenta.bp12.features.task_list

import com.lenta.bp12.R
import com.lenta.bp12.databinding.FragmentTaskListBinding
import com.lenta.bp12.platform.extention.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel
import android.os.Bundle
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import android.view.ViewGroup
import android.view.View

class TaskListFragment : CoreFragment<FragmentTaskListBinding, TaskListViewModel>(), ViewPagerSettings {

    override fun getLayoutId(): Int = R.layout.fragment_task_list

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix("Specify screen number!")

    override fun getViewModel(): TaskListViewModel {
        provideViewModel(TaskListViewModel::class.java).let {
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

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return View(context)
    }

    override fun getTextTitle(position: Int): String {
        return "Title: $position"
    }

    override fun countTab(): Int {
        return 2
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }


    /*override fun onBackPressed(): Boolean {
        vm.onBackPressed()
        return false
    }*/

}
