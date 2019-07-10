package com.lenta.bp7.features.code

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp7.features.check_type.CheckTypeViewModel
import com.lenta.bp7.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.fmp.resources.dao_ext.getExternalAuditPinCode
import com.lenta.shared.fmp.resources.dao_ext.getSelfControlPinCode
import com.lenta.shared.fmp.resources.fast.ZmpUtz14V001
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

    private val zmpUtz14V001: ZmpUtz14V001 by lazy { ZmpUtz14V001(hyperHive) }

    val pinCode1: MutableLiveData<String> = MutableLiveData("")
    val pinCode2: MutableLiveData<String> = MutableLiveData("")
    val pinCode3: MutableLiveData<String> = MutableLiveData("")
    val pinCode4: MutableLiveData<String> = MutableLiveData("")

    private val selfControlType: MutableLiveData<String> = MutableLiveData()
    private val externalAuditType: MutableLiveData<String> = MutableLiveData()

    val message: MutableLiveData<String> = MutableLiveData()
    private val incorrectCodeMessage: MutableLiveData<String> = MutableLiveData()

    var checkType: String? = ""
    var pinCode: String? = ""

    init {
        viewModelScope.launch {
            sessionInfo.checkType?.let { type ->
                checkType = type
            }

            when (checkType) {
                CheckTypeViewModel.SELF_CONTROL -> {
                    message.value = selfControlType.value
                    pinCode = zmpUtz14V001.getSelfControlPinCode()
                }
                CheckTypeViewModel.EXTERNAL_AUDIT -> {
                    message.value = externalAuditType.value
                    pinCode = zmpUtz14V001.getExternalAuditPinCode()
                }
                else -> {
                    throw IllegalArgumentException("Тип проверки не определен!")
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
        Logg.d { "Проверка пин-кода: $pinCode" }
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
