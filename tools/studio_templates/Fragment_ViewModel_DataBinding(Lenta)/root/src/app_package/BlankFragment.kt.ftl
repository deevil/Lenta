package ${escapeKotlinIdentifiers(packageName)}

<#if applicationPackage??>
import ${applicationPackage}.R
import ${applicationPackage}.databinding.${underscoreToCamelCase(layoutName)}Binding
import ${applicationPackage}.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
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

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix("Specify screen number!")

    override fun getViewModel(): ${viewModelName} {
        provideViewModel(${viewModelName}::class.java).let {
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

    <#if numberOfTabs != "0">
    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return View(context)
    }

    override fun getTextTitle(position: Int): String {
        return "Title: $position"
    }

    override fun countTab(): Int {
        return ${numberOfTabs}
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

}
