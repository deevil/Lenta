package com.lenta.bp10.features.good_information.sets.component

import androidx.lifecycle.viewModelScope
import com.lenta.bp10.features.good_information.base.BaseProductInfoViewModel
import com.lenta.bp10.features.good_information.excise_alco.ExciseAlcoDelegate
import com.lenta.bp10.features.good_information.excise_alco.StampCollector
import com.lenta.bp10.features.good_information.sets.ComponentItem
import com.lenta.bp10.models.repositories.ITaskRepository
import com.lenta.bp10.models.task.ProcessExciseAlcoProductService
import com.lenta.bp10.models.task.TaskDescription
import com.lenta.bp10.models.task.WriteOffReason
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import kotlinx.coroutines.launch
import javax.inject.Inject

class ComponentViewModel : BaseProductInfoViewModel() {

    private lateinit var componentItem: ComponentItem

    @Inject
    lateinit var exciseAlcoDelegate: ExciseAlcoDelegate

    private val processExciseAlcoProductService: ProcessExciseAlcoProductService by lazy {
        processServiceManager.getWriteOffTask()!!.processExciseAlcoProduct(productInfo.value!!)!!
    }

    private val stampCollector: StampCollector by lazy {
        StampCollector(processExciseAlcoProductService, count)
    }

    init {
        viewModelScope.launch {
            exciseAlcoDelegate.init(
                    viewModelScope = this@ComponentViewModel::viewModelScope,
                    handleNewStamp = this@ComponentViewModel::handleNewStamp,
                    tkNumber = getTaskDescription().tkNumber,
                    materialNumber = productInfo.value!!.materialNumber
            )
        }

    }

    fun setComponentItem(componentItem: ComponentItem) {
        this.componentItem = componentItem
    }


    override fun handleProductSearchResult(scanInfoResult: ScanInfoResult?): Boolean {
        //not used search product for this screen
        return true

    }

    override fun getTaskDescription(): TaskDescription {
        return processServiceManager.getWriteOffTask()!!.taskDescription
    }

    override fun getTaskRepo(): ITaskRepository {
        return processServiceManager.getWriteOffTask()!!.taskRepository
    }

    override fun getProcessTotalCount(): Double {
        return processExciseAlcoProductService.getTotalCount()
    }

    override fun onClickAdd() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onClickApply() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onBackPressed() {
    }

    override fun onScanResult(data: String) {
        if (stampCollector.prepare(stampCode = data)) {
            exciseAlcoDelegate.searchExciseStamp(data)
        } else {
            screenNavigator.openAlertDoubleScanStamp()
        }
    }

    override fun getReason(): WriteOffReason {
        getTaskDescription().moveTypes.let { moveTypes ->
            return moveTypes.filter { filterReason(it.code) }
                    .getOrElse((selectedPosition.value ?: -1)) { WriteOffReason.empty }
        }
    }

    private fun handleNewStamp(isBadStamp: Boolean) {
        if (!stampCollector.add(
                        materialNumber = productInfo.value!!.materialNumber,
                        setMaterialNumber = "",
                        writeOffReason = getSelectedReason().code,
                        isBadStamp = isBadStamp
                )) {
            screenNavigator.openAlertDoubleScanStamp()
        }
    }

    fun onClickRollBack() {
        stampCollector.rollback()
    }

    override fun filterReason(code: String): Boolean {
        return code == componentItem.writeOffReason.code
    }


}


/*class ComponentViewModelDeprecated : CoreViewModel(), OnPositionClickListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var processServiceManager: IWriteOffTaskManager

    @Inject
    lateinit var exciseStampNetRequest: ExciseStampNetRequest

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var exciseAlcoDelegate: ExciseAlcoDelegate

    private val stampCollector: StampCollector by lazy {
        StampCollector(processServiceManager.getWriteOffTask()!!.processExciseAlcoProduct(productInfo.value!!)!!, count)
    }


    init {
        viewModelScope.launch {
            processServiceManager.getWriteOffTask()?.let { writeOffTask ->
                writeOffReasonTitles.value = writeOffTask.taskDescription.moveTypes.map { it.name }
            }
            suffix.value = productInfo.value?.uom?.name

            *//*exciseAlcoDelegate.init(
                    viewModelScope = this@ComponentViewModelDeprecated::viewModelScope,
                    handleNewStamp = this@ComponentViewModelDeprecated::handleNewStamp,
                    tkNumber = processServiceManager.getWriteOffTask()!!.taskDescription.tkNumber,
                    materialNumber = productInfo.value!!.materialNumber
            )*//*
        }
    }

    fun onClickRollback() {
        if (exciseStamp.size > 0) {
            exciseStamp.removeAt(exciseStamp.lastIndex)
            count.value = exciseStamp.size.toString()
        }
    }

    fun onClickAdd() {
        processServiceManager.getWriteOffTask()!!.taskRepository.getExciseStamps().addExciseStamps(exciseStamp)

        exciseStamp.clear()
        count.value = "0"

        Logg.d { "exiseStampsForProduct ${processServiceManager.getWriteOffTask()!!.taskRepository.getExciseStamps().findExciseStampsOfProduct(productInfo.value!!).map { it.code }}" }
        Logg.d { "exiseStampsAll ${processServiceManager.getWriteOffTask()!!.taskRepository.getExciseStamps().getExciseStamps().size}" }

    }

    fun onClickApply() {
        onClickAdd()
        screenNavigator.goBack()
    }

    override fun onClickPosition(position: Int) {
        selectedPosition.value = position
    }

    //TODO тестовый код, для проверки сканирования, потом переписать
    override fun onOkInSoftKeyboard(): Boolean {
        searchExciseStamp()
        return true
    }

    private fun searchExciseStamp() {
        viewModelScope.launch {
            exciseStampCode.value?.let {
                exciseStampNetRequest(ExciseStampParams(pdf417 = it, werks = sessionInfo.market!!, matnr = productInfo.value!!.materialNumber)).either(::handleFailure, ::handleExciseStampSuccess)
            }
        }
    }

    private fun handleExciseStampSuccess(exciseStampRestInfo: List<ExciseStampRestInfo>) {
        //Logg.d { "handleSuccess ${exciseStampRestInfo}" }
        if (totalCount.value!! >= componentItem.value!!.menge.toDouble() * componentItem.value!!.countSets) {
            screenNavigator.openAlertScreen(limitExceeded.value!!)
            return
        }

        val retcodeCode = exciseStampRestInfo[1].data[0][0].toInt()
        val retcodeName = exciseStampRestInfo[1].data[0][1]

        when (retcodeCode) {
            0 -> addExciseStamp()
            1 -> screenNavigator.openAlertScreen(message = retcodeName)
            2 -> screenNavigator.openAlertScreen(message = retcodeName)
            3 -> screenNavigator.openAlertScreen(message = retcodeName)
            4 -> screenNavigator.openAlertScreen(message = retcodeName)
        }
    }

    *//*private fun handleNewStamp(isBadStamp: Boolean) {
        count.value = (count.value!!.toInt() + 1).toString()
        exciseStamp.add(TaskExciseStamp(
                materialNumber = productInfo.value!!.materialNumber,
                code = exciseStamp.setMaterialNumber = componentItem.value!!.setMaterialNumber,
                writeOffReason = componentItem.value!!.writeOffReason.code,
                isBadStamp = isBadStamp
        ))


    }*//*

    fun addExciseStamp() {
        count.value = (count.value!!.toInt() + 1).toString()
        exciseStamp.add(TaskExciseStamp(
                materialNumber = productInfo.value!!.materialNumber,
                code = exciseStampCode.value!!,
                setMaterialNumber = componentItem.value!!.setMaterialNumber,
                writeOffReason = componentItem.value!!.writeOffReason.name,
                isBadStamp = true
        ))
        countValue.value = exciseStamp.size.toDouble()
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.openAlertScreen(failure)
    }

    fun onScanResult(data: String) {
        exciseStampCode.value = data
        searchExciseStamp()
    }

}*/
