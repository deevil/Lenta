package com.lenta.bp16.features.reprint_label

import androidx.lifecycle.viewModelScope
import com.lenta.bp16.data.IPrinter
import com.lenta.bp16.data.LabelInfo
import com.lenta.bp16.model.ITaskManager
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.settings.IAppSettings
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ReprintLabelViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var taskManager: ITaskManager

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var appSettings: IAppSettings

    @Inject
    lateinit var printer: IPrinter


    val selectionsHelper = SelectionItemsHelper()

    val title by lazy {
        "ТК - ${sessionInfo.market}"
    }

    val labels by lazy {
        taskManager.labels.map { list ->
            list?.mapIndexed { index, labelInfo ->
                ReprintLabelUi(
                        labelInfo = labelInfo,
                        position = "${list.size - index}",
                        name = labelInfo.goodsName,
                        packNumber = labelInfo.codeCont,
                        date = SimpleDateFormat(Constants.DATE_FORMAT_ddmmyy, Locale.getDefault()).format(labelInfo.printTime),
                        time = SimpleDateFormat(Constants.TIME_FORMAT_HHmm, Locale.getDefault()).format(labelInfo.printTime),
                        quantity = labelInfo.quantity
                )
            }
        }
    }

    val printEnabled by lazy {
        selectionsHelper.selectedPositions.map { it?.isNotEmpty() }
    }

    // -----------------------------

    fun onClickItemPosition(position: Int) {
        Logg.d { "--> onClickItemPosition: $position" }
    }

    fun onClickPrint() {
        labels.value?.let { labels ->
            val label = labels[selectionsHelper.selectedPositions.value!!.first()].labelInfo
            printLabel(label)
        }
    }

    private fun printLabel(labelInfo: LabelInfo) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                appSettings.printerIpAddress.let { ipAddress ->
                    if (ipAddress == null) {
                        return@let null
                    }

                    navigator.showProgressLoadingData()

                    printer.printLabel(labelInfo, ipAddress)
                            .also {
                                navigator.hideProgress()
                            }.either(::handleFailure) {
                                navigator.showLabelSentToPrint {
                                    // Ничего не делаем...

                                }
                            }
                }
            }.also {
                if (it == null) {
                    navigator.showAlertNoIpPrinter()
                }
            }
        }
    }

}

data class ReprintLabelUi(
        val labelInfo: LabelInfo,
        val position: String,
        val name: String,
        val packNumber: String,
        val date: String,
        val time: String,
        val quantity: String
)