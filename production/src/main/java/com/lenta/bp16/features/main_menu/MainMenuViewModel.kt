package com.lenta.bp16.features.main_menu

import com.lenta.bp16.data.IPrinter
import com.lenta.bp16.model.ITaskManager
import com.lenta.bp16.model.TaskType
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import javax.inject.Inject

class MainMenuViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var taskManager: ITaskManager
    //@Inject
    //lateinit var printer: IPrinter


    fun onClickAuxiliaryMenu() {
        navigator.openAuxiliaryMenuScreen()
    }

    fun onClickExternalSupply() {
        taskManager.taskType = TaskType.EXTERNAL_SUPPLY
        //navigator.openExternalSupplyTaskListScreen()

        navigator.openReprintLabelScreen()



        //TODO удалить после окончательной реализации печати
        /*viewModelScope.launch {
            withContext(IO) {
                printer.printTag(
                        printInnerTagInfo = PrintInnerTagInfo(
                                quantity = "999 кг",
                                codeCont = "308",
                                storCond = "Срок хранения корейского салата\\&из моркови в плотно закрытой\\&емкости составляет от 10 до 15 дней",
                                planAufFinish = "14.11.2019 12:01(planAufFinish)",
                                aufnr = "500001149387",
                                nameOsn = "Дорадо Х/К потр с/г 300-400 (из з.сыр)",
                                dateExpir = "14.11.2019 12:02",
                                goodsName = "Дорадо Х/К потр с/г 300-400 \\& (из з.сыр)(goodsName)",
                                weigher = "1",
                                productTime = "14.11.2019 12:01",
                                nameDone = "Дорадо Х/К потр с/г 300-400 (из з.сыр) готовый",
                                goodsCode = "goodsCode",
                                barcode = "(01)123306(310x)444(8008)20181214(10)500001149386(7003)201811201414(91)307"

                        ),
                        printInnerTagInfo = PrintInnerTagInfo(
                                quantity = "quantity",
                                codeCont = "codeCont",
                                storCond = "storCond",
                                planAufFinish = "planAufFinish",
                                aufnr = "aufnr",
                                nameOsn = "nameOsn",
                                dateExpir = "dateExpir",
                                goodsName = "goodsName",
                                weigher = "weigher",
                                productTime = "productTime",
                                nameDone = "nameDone",
                                goodsCode = "goodsCode",
                                barcode = "(01)523506(310x)999(8008)20191214(10)500001149387(7003)201911201414(91)308"

                        ),
                        ip = "192.168.10.249"
                )
            }
        }*/

    }

    fun onClickProcessingUnit() {
        taskManager.taskType = TaskType.PROCESSING_UNIT
        navigator.openProcessingUnitTaskListScreen()
    }

}