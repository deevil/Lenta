package ${escapeKotlinIdentifiers(packageName)}

<#if applicationPackage??>
import ${applicationPackage}.R
import ${applicationPackage}.databinding.${underscoreToCamelCase(layoutName)}Binding
import ${applicationPackage}.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
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

    override fun getPageNumber(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getViewModel(): ${viewModelName} {
        provideViewModel(${viewModelName}::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

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

    

}
