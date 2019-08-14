package ${escapeKotlinIdentifiers(packageName)}

import com.lenta.shared.platform.viewmodel.CoreViewModel
<#if numberOfTabs != "0">
import androidx.lifecycle.MutableLiveData
import com.lenta.shared.utilities.databinding.PageSelectionListener
</#if>

class ${viewModelName} : CoreViewModel()<#if numberOfTabs != "0">, PageSelectionListener</#if>  {
    
    <#if numberOfTabs != "0">
    val selectedPage = MutableLiveData(0)

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    </#if>
    // TODO: Implement the ViewModel
}
