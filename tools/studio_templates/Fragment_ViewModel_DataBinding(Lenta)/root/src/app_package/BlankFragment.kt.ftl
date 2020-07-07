package ${escapeKotlinIdentifiers(packageName)}

<#if applicationPackage??>
import android.view.View
import ${applicationPackage}.R
import ${applicationPackage}.databinding.${underscoreToCamelCase(layoutName)}Binding
import ${applicationPackage}.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel
</#if>
<#if numberOfTabs != "0">
import android.os.Bundle
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import android.view.ViewGroup
import android.view.View
</#if>

class ${className} : CoreFragment<${underscoreToCamelCase(layoutName)}Binding, ${viewModelName}>()<#if numberOfTabs != "0">, ViewPagerSettings</#if> {

    override fun getLayoutId(): Int = R.layout.${layoutName}

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix(SCREEN_NUMBER)

    override fun getViewModel(): ${viewModelName} {
        provideViewModel(${viewModelName}::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

        //topToolbarUiModel.title.value = context?.getAppInfo()
        //topToolbarUiModel.title.value = vm.title
        //topToolbarUiModel.description.value = getString(R.string.description)

        //topToolbarUiModel.uiModelButton1.show(ImageButtonDecorationInfo.settings)
        //topToolbarUiModel.uiModelButton2.show(ImageButtonDecorationInfo.exitFromApp)
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

    <#if numberOfTabs != "0">
    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return View(context)
    }

    override fun getTextTitle(position: Int): String {
        return "Title: $position"
    }

    override fun countTab(): Int {
        return TABS
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }

    </#if>

    /*override fun onBackPressed(): Boolean {
        vm.onBackPressed()
        return false
    }*/

    companion object {
        const val SCREEN_NUMBER = "Specify screen number!"

        <#if numberOfTabs != "0">
        private const val TABS = ${numberOfTabs}
        private const val TAB_FIRST = 0
        private const val TAB_SECOND = 1
        </#if>
    }

}
