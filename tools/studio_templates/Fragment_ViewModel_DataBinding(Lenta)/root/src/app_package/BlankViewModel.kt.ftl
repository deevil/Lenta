package ${escapeKotlinIdentifiers(packageName)}

import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.account.ISessionInfo
import javax.inject.Inject
<#if numberOfTabs != "0">
import androidx.lifecycle.MutableLiveData
import com.lenta.shared.utilities.databinding.PageSelectionListener
</#if>

class ${viewModelName} : CoreViewModel()<#if numberOfTabs != "0">, PageSelectionListener</#if>  {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo


    val title by lazy {
        "ТК - ${sessionInfo.market}"
    }

    <#if numberOfTabs != "0">
    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }
    </#if>

    // TODO: Implement the ViewModel

}
