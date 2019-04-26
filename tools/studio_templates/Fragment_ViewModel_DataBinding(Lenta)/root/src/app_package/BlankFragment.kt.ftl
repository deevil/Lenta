package ${escapeKotlinIdentifiers(packageName)}

<#if applicationPackage??>
import ${applicationPackage}.R
import ${applicationPackage}.databinding.${className}Binding
import ${applicationPackage}.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel
</#if>

class ${className} : CoreFragment<${className}Binding, ${viewModelName}>() {

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

    

}
