package com.lenta.bp7.features.code

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp7.data.CheckType
import com.lenta.bp7.platform.navigation.IScreenNavigator
import com.lenta.bp7.repos.IDatabaseRepo
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.launch
import javax.inject.Inject

class CodeViewModel : CoreViewModel(), OnOkInSoftKeyboardListener {

    @Inject
    lateinit var hyperHive: HyperHive
    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var database: IDatabaseRepo

    val pinCode1: MutableLiveData<String> = MutableLiveData("")
    val pinCode2: MutableLiveData<String> = MutableLiveData("")
    val pinCode3: MutableLiveData<String> = MutableLiveData("")
    val pinCode4: MutableLiveData<String> = MutableLiveData("")

    private val selfControlType: MutableLiveData<String> = MutableLiveData()
    private val externalAuditType: MutableLiveData<String> = MutableLiveData()

    val message: MutableLiveData<String> = MutableLiveData()
    private val incorrectCodeMessage: MutableLiveData<String> = MutableLiveData()

    var pinCode: String? = ""

    init {
        viewModelScope.launch {
            when (sessionInfo.checkType) {
                CheckType.SELF_CONTROL.type -> {
                    message.value = selfControlType.value
                    pinCode = database.getSelfControlPinCode()
                }
                CheckType.EXTERNAL_AUDIT.type -> {
                    message.value = externalAuditType.value
                    pinCode = database.getExternalAuditPinCode()
                }
                else -> {
                    throw IllegalArgumentException("Check type is not defined!")
                }
            }
        }
    }

    fun setTextForCheckType(selfControlType: String, externalAuditType: String) {
        this.selfControlType.value = selfControlType
        this.externalAuditType.value = externalAuditType
    }

    fun setIncorrectCodeMessage(incorrectCodeMessage: String) {
        this.incorrectCodeMessage.value = incorrectCodeMessage
    }

    val enabledGoOverBtn: MutableLiveData<Boolean> = pinCode1
            .combineLatest(pinCode2)
            .combineLatest(pinCode3)
            .combineLatest(pinCode4)
            .map {
                val pin1 = it?.first?.first?.first
                val pin2 = it?.first?.first?.second
                val pin3 = it?.first?.second
                val pin4 = it?.second
                !(pin1.isNullOrEmpty() || pin2.isNullOrEmpty() || pin3.isNullOrEmpty() || pin4.isNullOrEmpty())
            }

    fun onClickGoOver() {
        Logg.d { "PIN check: $pinCode" }
        if (pinCode == pinCode1.value + pinCode2.value + pinCode3.value + pinCode4.value) {
            navigator.openOptionScreen()
        } else {
            navigator.openAlertScreen(message = incorrectCodeMessage.value!!, pageNumber = "95")
        }
    }

    override fun onOkInSoftKeyboard(): Boolean {
        onClickGoOver()
        return true
    }
}
