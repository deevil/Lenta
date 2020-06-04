package com.lenta.bp9.features.revise

import com.lenta.shared.platform.viewmodel.CoreViewModel
import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.revise.FormABImportRevise
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.shared.fmp.resources.dao_ext.getProductInfoByMaterial
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

class AlcoFormReviseViewModel : CoreViewModel(), PageSelectionListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var taskManager: IReceivingTaskManager
    @Inject
    lateinit var hyperHive: HyperHive

    private val ZfmpUtz48V001: ZfmpUtz48V001 by lazy {
        ZfmpUtz48V001(hyperHive)
    }

    lateinit var matnr: String
    lateinit var batchNumber: String

    val selectedPage = MutableLiveData(0)

    val formAB: FormABImportRevise? by lazy {
        taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.getImportABForms()?.findLast { it.batchNumber == batchNumber && it.productNumber == matnr }
    }

    val quantity_by_b: String by lazy {
        (formAB?.quantityByTTN_B?.toStringFormatted() ?: "") + " шт."
    }

    val gtdA: MutableLiveData<String> = MutableLiveData("")
    val gtdB: MutableLiveData<String> = MutableLiveData("")
    val gtdAEditable: Boolean by lazy { formAB?.numberTTN_A.isNullOrEmpty() }
    val gtdBEditable: Boolean by lazy { formAB?.numberTTN_B.isNullOrEmpty() }

    val partABChecked: MutableLiveData<Boolean> = MutableLiveData(false)
    val partOneChecked: MutableLiveData<Boolean> = MutableLiveData(false)
    val partTwoChecked: MutableLiveData<Boolean> = MutableLiveData(false)

    val nextEnabled: MutableLiveData<Boolean> = combineLatest(partABChecked, partOneChecked, partTwoChecked).map {
        it?.first == true && it.second == true && it.third == true
    }

    val taskCaption: String by lazy {
        (taskManager.getReceivingTask()?.taskHeader?.topText?.split("//")?.first() ?: "") + " // " +
                matnr.takeLast(6) + " " +
                (ZfmpUtz48V001.getProductInfoByMaterial(matnr)?.name ?: "")
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onResume() {
        gtdA.value = formAB?.numberTTN_A ?: ""
        gtdB.value = formAB?.numberTTN_B ?: ""
    }

    fun onClickNext() {
        taskManager.getReceivingTask()?.taskRepository?.getReviseDocuments()?.approveImportForm(matnr,
                batchNumber,
                if (gtdA.value.isNullOrEmpty()) null else gtdA.value,
                if (gtdB.value.isNullOrEmpty()) null else gtdB.value)
        screenNavigator.goBack()
    }
}
