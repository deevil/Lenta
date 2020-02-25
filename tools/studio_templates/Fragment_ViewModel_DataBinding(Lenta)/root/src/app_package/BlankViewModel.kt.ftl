package ${escapeKotlinIdentifiers(packageName)}

import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.account.ISessionInfo
<#if numberOfTabs != "0">
import androidx.lifecycle.MutableLiveData
import com.lenta.shared.utilities.databinding.PageSelectionListener
</#if>

class ${viewModelName} : CoreViewModel()<#if numberOfTabs != "0">, PageSelectionListener</#if>  {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo


    <#if numberOfTabs != "0">
    val selectedPage = MutableLiveData(0)

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }
    </#if>

    // TODO: Implement the ViewModel
}
