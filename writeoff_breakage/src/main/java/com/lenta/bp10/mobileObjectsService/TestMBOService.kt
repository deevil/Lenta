package com.lenta.bp10.mobileObjectsService

import com.lenta.bp10.mobileObjectsService.models.*
import java.util.*

class TestMBOService : IMboService {
    override fun getUomInfo(uom: String): MB_S_07 {
        return MB_S_07("ST", "ШТ", 0.toShort())
    }

    private val mbs14Items = ArrayList(Arrays.asList(MB_S_14("ALL_AUTOEXIT_TIMEOUT", "30"),
            MB_S_14("WOB_ALLOWED_VERSION", "10.0.0.0"),
            MB_S_14("WOB_SPEC_TASK_TYPE", "УНЧ"))
    )
    override fun getParametersByParamName(paramName: String): List<MB_S_14> {
        val items = ArrayList<MB_S_14>()
        for (i in mbs14Items.indices) {
            if (mbs14Items[i].paramname == paramName) {
                items.add(mbs14Items[i])
            }
        }

        return items
    }

    override fun getFirstParameterValueByName(paramName: String): String {
        var item: MB_S_14? = null
        for (i in mbs14Items.indices) {
            if (mbs14Items[i].paramname == paramName) {
                item = mbs14Items[i]
                break
            }
        }
        return if (item == null) "" else item.paramvalue
    }

    private val mbs22Items = ArrayList(Arrays.asList(MB_S_22("000000000000430748", "000000000000212369", 1.0, "ST"),
            MB_S_22("000000000000430748", "000000000000125426", 1.0, "ST"),
            MB_S_22("000000000000430698", "000000000000212369", 1.0, "ST"),
            MB_S_22("000000000000430698", "000000000000125426", 2.0, "ST"),
            MB_S_22("000000000000240490", "000000000000175094", 2.0, "ST"))
    )
    override fun getSetItemsByMatnrOsn(matnrOsn: String): List<MB_S_22> {
        val items = ArrayList<MB_S_22>()
        for (i in mbs22Items.indices) {
            if (mbs22Items[i].matnr_osn == matnrOsn) {
                items.add(mbs22Items[i])
            }
        }

        return items
    }

    override fun getBarcodeInfo(barcode: String): MB_S_25 {
        return MB_S_25(barcode, "000000000000000602", "ST", 1.0, 1.0)
    }

    override fun getAllPrinters(): List<MB_S_26> {
        return ArrayList(Arrays.asList(MB_S_26("printerName1", "printerPlace1", "0602"),
                MB_S_26("printerName2", "printerPlace2", "0602"),
                MB_S_26("printerName4", "printerPlace4", "0602"),
                MB_S_26("printerName3", "printerPlace3", "0602"),
                MB_S_26("printerName5", "printerPlace5", "0602"),
                MB_S_26("printerName6", "printerPlace6", "0602")))
    }

    private val mbs30Items = ArrayList(Arrays.asList(MB_S_30("000000000000000010",
            "Конфеты FERRERO ROCHER Хрустящие из мол/шоколада,покр измел.орешками с нач из крема (Италия) 200г",
            "2FER", "ST", "A", "01", "X", "", "", null, null),
            MB_S_30("000000000000000020", "Кукураза желтая консерва 500г", "2FER", "ST", "A", "02", "", "", "", null, null),
            MB_S_30("000000000000000030", "Горошек зеленый консерва 500г", "2FER", "ST", "P", "03", "", "", "", null, null),
            MB_S_30("000000000000000031", "Ананас консерва 500г", "1HAW", "ST", "D", "04", "", "", "", null, null),
            MB_S_30("000000000000000032", "Жопа консерва 500г", "2FER", "ST", "P", "05", "", "", "", null, null),
            MB_S_30("000000000000000033", "Чипсы консерва 500г", "2FER", "ST", "P", "06", "", "", "", null, null),
            MB_S_30("000000000000000040", "Пиво приятель 0.5л", "1HAW", "ST", "A", "07", "", "X", "X", null, null),
            MB_S_30("000000000000000041", "Пиво неприятель 0.5л", "1HAW", "ST", "A", "08", "", "X", "X", null, null),
            MB_S_30("000000000000000042", "Пиво Huegarden 1.5л", "1HAW", "ST", "A", "09", "", "X", "X", null, null),
            MB_S_30("000000000000000050", "Сидр яблочный 1л", "1HAW", "ST", "P", "10", "", "X", "X", null, null),
            MB_S_30("000000000000000060", "Энергетик Рево 0.5л", "1HAW", "ST", "D", "11", "", "", "X", null, null),
            //Alco excise
            MB_S_30("000000000000299232", "Вермут MONDORO Bianco Мондоро Бьянко бел.сл. (Италия) 1L", "1HAW", "ST", "A", "12", "X", "X", "X", null, null),
            MB_S_30("000000000000156542", "Вермут БУКЕТ МОЛДАВИИ ароматизированный сладкий белый (Молдова) 1L", "1HAW", "ST", "A", "13", "X", "X", "X", null, null),
            MB_S_30("000000000000011663", "Вино BARON DE VALLS красное п/сух. (Испания) 0.75L", "1HAW", "ST", "N", "14", "X", "X", "X", null, null),
            // Alco excise sets components
            MB_S_30("000000000000212369", "Водка FINLANDIA Финляндия алк.40% п/у (Финляндия) 0.7L", "1HAW", "ST", "A", "01", "", "X", "X", null, null),
            MB_S_30("000000000000125426", "Водка ABSOLUT алк.40% (Швеция) 1L", "1HAW", "ST", "A", "01", "", "X", "X", null, null),
            MB_S_30("000000000000382322", "Виски WILLIAM LAWSON''S Вильям Лоусонс алк.40% (Россия) 0.5L", "1HAW", "ST", "A", "01", "", "X", "X", null, null),
            MB_S_30("000000000000349347", "Ром BACARDI Карта Бланка алк.40% (Мексика) 0.5L", "1HAW", "ST", "A", "01", "", "X", "X", null, null),
            //Alco excise sets
            MB_S_30("000000000000430748", "Тестовый алконабор-2 для викингов (Финляндия) 1.7L", "1HAW", "ST", "A", "01", "", "X", "X", null, null),
            MB_S_30("000000000000430698", "Тестовый алконабор для викингов (Финляндия) 2.7L", "1HAW", "ST", "A", "01", "", "X", "X", null, null),

            //Non excise sets
            MB_S_30("000000000000240490", "Неакцизный алко набор", "1HAW", "ST", "A", "01", "", "", "X", null, null)))

    override fun getProductByMaterialNumber(material: String): MB_S_30 {
        var item: MB_S_30? = null
        for (i in mbs30Items.indices) {
            if (mbs30Items[i].material == material) {
                item = mbs30Items[i]
                break
            }
        }
        return item!!
    }

    private val mbs31Items = ArrayList(Arrays.asList(MB_S_31("АКЦ", "01", "0002", "Лом/Бой"),
            MB_S_31("АКЦ", "02", "0002", "Лом/Бой"),
            MB_S_31("АКЦ", "03", "0006", "Срок годности"),
            MB_S_31("АКЦ", "04", "0006", "Срок годности"),
            MB_S_31("АКЦ", "05", "0006", "Срок годности"),
            MB_S_31("АКЦ", "06", "0002", "Лом/Бой"),
            MB_S_31("АКЦ", "07", "0003", "Гниль/Плесень"),
            MB_S_31("АКЦ", "08", "0008", "Нарушение Тврн.Вида"),
            MB_S_31("АКЦ", "09", "0008", "Нарушение Тврн.Вида"),
            MB_S_31("АКЦ", "10", "0008", "Нарушение Тврн.Вида"),
            MB_S_31("АКЦ", "11", "0002", "Лом/Бой"),
            MB_S_31("АКЦ", "12", "0002", "Лом/Бой"),
            MB_S_31("АКЦ", "13", "0002", "Лом/Бой"),
            MB_S_31("АКЦ", "14", "0002", "Лом/Бой"),
            MB_S_31("АКЦ", "15", "0002", "Лом/Бой"),
            MB_S_31("АКЦ", "16", "0002", "Лом/Бой"),
            MB_S_31("АКЦ", "17", "0002", "Лом/Бой"),
            MB_S_31("АКЦ", "18", "0002", "Лом/Бой"),
            MB_S_31("АКЦ", "19", "0002", "Лом/Бой"),
            MB_S_31("АКЦ", "91", "0002", "Лом/Бой")))
    override fun getWriteOffCause(taskType: String, sectionId: String, matkl: String, ekgrp: String): MB_S_31? {
        var item: MB_S_31? = null
        for (i in mbs31Items.indices) {
            if (mbs31Items[i].taskType == taskType && mbs31Items[i].sectionId == sectionId) {
                item = mbs31Items[i]
                break
            }
        }
        return if (item == null) null else MB_S_31(item.taskType, item.sectionId, item.reason, item.grtxt )
    }

    private val mbs32Items = ArrayList(Arrays.asList(MB_S_32("АКЦ", "A", "0002", "Лом/Бой"),
            MB_S_32("АКЦ", "N", "0003", "Гниль/Плесень"),
            MB_S_32("АКЦ", "N", "0006", "Срок годности"),
            MB_S_32("АКЦ", "A", "0008", "Нарушение Тврн.Вида"),
            MB_S_32("СГП", "A", "0002", "Лом/Бой"),
            MB_S_32("СГП", "N", "0003", "Гниль/Плесень"),
            MB_S_32("СГП", "N", "0006", "Срок годности"),
            MB_S_32("СГП", "A", "0008", "Нарушение Тврн.Вида"),
            MB_S_32("СРЦ", "A", "0002", "Лом/Бой"),
            MB_S_32("СРЦ", "N", "0003", "Гниль/Плесень"),
            MB_S_32("СРЦ", "N", "0006", "Срок годности"),
            MB_S_32("СРЦ", "A", "0008", "Нарушение Тврн.Вида"),
            MB_S_32("СТК", "A", "0002", "Лом/Бой"),
            MB_S_32("СТК", "N", "0003", "Гниль/Плесень"),
            MB_S_32("СТК", "N", "0006", "Срок годности"),
            MB_S_32("СТК", "A", "0008", "Нарушение Тврн.Вида")))
    override fun getWriteOffCauseByTask(taskType: String): List<MB_S_32> {
        val items = ArrayList<MB_S_32>()
        for (i in mbs32Items.indices) {
            if (mbs32Items[i].taskType == taskType) {
                items.add(MB_S_32(mbs32Items[i].taskType, mbs32Items[i].taskCntrl, mbs32Items[i].reason, mbs32Items[i].grtxt))
            }
        }

        return items
    }

    override fun getStoragesByTaskTypeAndTK(taskType: String, tkNumber: String): List<MB_S_33> {
        val res = ArrayList<MB_S_33>()
        when (taskType) {
            "АКЦ" -> res.add(MB_S_33("АКЦ", "*", "*"))
            "КАТ" -> res.add(MB_S_33("КАТ", "*", "*"))
            "УНЧ" -> res.add(MB_S_33("УНЧ", "*", "0001"))
            "УПК" -> res.add(MB_S_33("УПК", "*", "0002"))
            else -> {
            }
        }
        return res
    }

    override fun getProductTypesByTaskType(taskType: String): List<MB_S_34> {
        val res = ArrayList<MB_S_34>()
        when (taskType) {
            "АКЦ" -> {
                res.add(MB_S_34("АКЦ", "1HAW"))
                res.add(MB_S_34("АКЦ", "2FER"))
            }
            "КАТ" -> res.add(MB_S_34("КАТ", "1HAW"))
            "УНЧ" -> {
                res.add(MB_S_34("УНЧ", "1HAW"))
                res.add(MB_S_34("УНЧ", "41NS"))
                res.add(MB_S_34("УНЧ", "LEER"))
            }
            "УПК" -> res.add(MB_S_34("УПК", "1HAW"))
            else -> {
            }
        }
        return res
    }

    override fun getGisControlsByTaskCntrl(taskCntrl: String): MB_S_36? {
        return null
    }
}