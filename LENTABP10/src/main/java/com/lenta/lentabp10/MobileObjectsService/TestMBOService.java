package com.lenta.lentabp10.MobileObjectsService;

import com.lenta.lentabp10.MobileObjectsService.models.MB_S_07;
import com.lenta.lentabp10.MobileObjectsService.models.MB_S_14;
import com.lenta.lentabp10.MobileObjectsService.models.MB_S_22;
import com.lenta.lentabp10.MobileObjectsService.models.MB_S_25;
import com.lenta.lentabp10.MobileObjectsService.models.MB_S_26;
import com.lenta.lentabp10.MobileObjectsService.models.MB_S_30;
import com.lenta.lentabp10.MobileObjectsService.models.MB_S_31;
import com.lenta.lentabp10.MobileObjectsService.models.MB_S_32;
import com.lenta.lentabp10.MobileObjectsService.models.MB_S_33;
import com.lenta.lentabp10.MobileObjectsService.models.MB_S_34;
import com.lenta.lentabp10.MobileObjectsService.models.MB_S_36;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestMBOService implements IMboService{
    @Override
    public MB_S_07 getUomInfo(String uom) {
        return new MB_S_07("ST", "ШТ", (short) 0);
    }

    private final List<MB_S_14> mbs14Items = new ArrayList<>(Arrays.asList(new MB_S_14("ALL_AUTOEXIT_TIMEOUT", "30"),
                                                                      new MB_S_14("WOB_ALLOWED_VERSION", "10.0.0.0"),
                                                                      new MB_S_14("WOB_SPEC_TASK_TYPE", "УНЧ"))
    );

    @Override
    public List<MB_S_14> getParametersByParamName(String paramName) {
        List<MB_S_14> items = new ArrayList<>();
        for(int i=0; i<mbs14Items.size(); i++) {
            if ( mbs14Items.get(i).getParamname().equals(paramName) ) {
                items.add(mbs14Items.get(i));
            }
        }

        return items;
    }

    @Override
    public String getFirstParameterValueByName(String paramName) {
        MB_S_14 item = null;
        for(int i=0; i<mbs14Items.size(); i++) {
            if ( mbs14Items.get(i).getParamname().equals(paramName) ) {
                item = mbs14Items.get(i);
                break;
            }
        }
        return item == null ? "" : item.getParamvalue();
    }

    private final List<MB_S_22> mbs22Items = new ArrayList<>(Arrays.asList(new MB_S_22("000000000000430748", "000000000000212369", 1, "ST"),
            new MB_S_22("000000000000430748", "000000000000125426", 1, "ST"),
            new MB_S_22("000000000000430698", "000000000000212369", 1, "ST"),
            new MB_S_22("000000000000430698", "000000000000125426", 2, "ST"),
            new MB_S_22("000000000000240490", "000000000000175094", 2, "ST"))
    );

    @Override
    public List<MB_S_22> getSetItemsByMatnrOsn(String matnrOsn) {
        List<MB_S_22> items = new ArrayList<>();
        for(int i=0; i<mbs22Items.size(); i++) {
            if ( mbs22Items.get(i).getMatnr_osn().equals(matnrOsn) ) {
                items.add(mbs22Items.get(i));
            }
        }

        return items;
    }

    @Override
    public MB_S_25 getBarcodeInfo(String barcode) {
        return new MB_S_25(barcode, "000000000000000602", "ST", 1, 1);
    }

    @Override
    public List<MB_S_26> getAllPrinters() {
        return new ArrayList<>(Arrays.asList(new MB_S_26("printerName1", "printerPlace1", "0602"),
                new MB_S_26("printerName2", "printerPlace2", "0602"),
                new MB_S_26("printerName4", "printerPlace4", "0602"),
                new MB_S_26("printerName3", "printerPlace3", "0602"),
                new MB_S_26("printerName5", "printerPlace5", "0602"),
                new MB_S_26("printerName6", "printerPlace6", "0602")));
    }

    private final List<MB_S_30> mbs30Items = new ArrayList<>(Arrays.asList(new MB_S_30("000000000000000010",
                    "Конфеты FERRERO ROCHER Хрустящие из мол/шоколада,покр измел.орешками с нач из крема (Италия) 200г",
                    "2FER", "ST", "A", "01", "X", "", "", null, null),
            new MB_S_30("000000000000000020", "Кукураза желтая консерва 500г", "2FER", "ST", "A", "02", "", "","", null, null),
            new MB_S_30("000000000000000030", "Горошек зеленый консерва 500г", "2FER", "ST", "P", "03", "", "","", null, null),
            new MB_S_30("000000000000000031", "Ананас консерва 500г", "1HAW", "ST", "D", "04", "", "","", null, null),
            new MB_S_30("000000000000000032", "Жопа консерва 500г", "2FER", "ST", "P", "05", "", "","", null, null),
            new MB_S_30("000000000000000033", "Чипсы консерва 500г", "2FER", "ST", "P", "06", "", "","", null, null),
            new MB_S_30("000000000000000040", "Пиво приятель 0.5л", "1HAW", "ST", "A", "07", "", "X","X", null, null),
            new MB_S_30("000000000000000041", "Пиво неприятель 0.5л", "1HAW", "ST", "A", "08", "", "X","X", null, null),
            new MB_S_30("000000000000000042", "Пиво Huegarden 1.5л", "1HAW", "ST", "A", "09", "", "X","X", null, null),
            new MB_S_30("000000000000000050", "Сидр яблочный 1л", "1HAW", "ST", "P", "10", "", "X","X", null, null),
            new MB_S_30("000000000000000060", "Энергетик Рево 0.5л", "1HAW", "ST", "D", "11", "", "", "X", null, null),
            //Alco excise
            new MB_S_30("000000000000299232", "Вермут MONDORO Bianco Мондоро Бьянко бел.сл. (Италия) 1L", "1HAW", "ST", "A", "12", "X", "X", "X", null, null),
            new MB_S_30("000000000000156542", "Вермут БУКЕТ МОЛДАВИИ ароматизированный сладкий белый (Молдова) 1L", "1HAW", "ST", "A", "13", "X", "X", "X", null, null),
            new MB_S_30("000000000000011663", "Вино BARON DE VALLS красное п/сух. (Испания) 0.75L", "1HAW", "ST", "N", "14", "X", "X", "X", null, null),
            // Alco excise sets components
            new MB_S_30("000000000000212369", "Водка FINLANDIA Финляндия алк.40% п/у (Финляндия) 0.7L", "1HAW", "ST", "A", "01", "", "X", "X", null, null),
            new MB_S_30("000000000000125426", "Водка ABSOLUT алк.40% (Швеция) 1L", "1HAW", "ST", "A", "01", "", "X", "X", null, null),
            new MB_S_30("000000000000382322", "Виски WILLIAM LAWSON''S Вильям Лоусонс алк.40% (Россия) 0.5L", "1HAW", "ST", "A", "01", "", "X", "X", null, null),
            new MB_S_30("000000000000349347", "Ром BACARDI Карта Бланка алк.40% (Мексика) 0.5L", "1HAW","ST","A","01","","X","X", null, null),
            //Alco excise sets
            new MB_S_30("000000000000430748", "Тестовый алконабор-2 для викингов (Финляндия) 1.7L", "1HAW", "ST", "A", "01", "", "X", "X", null, null),
            new MB_S_30("000000000000430698", "Тестовый алконабор для викингов (Финляндия) 2.7L", "1HAW", "ST", "A", "01", "", "X", "X", null, null),

            //Non excise sets
            new MB_S_30("000000000000240490", "Неакцизный алко набор", "1HAW", "ST", "A", "01", "", "", "X", null, null)));

    @Override
    public MB_S_30 getProductByMaterialNumber(String material) {
        MB_S_30 item = null;
        for(int i=0; i<mbs30Items.size(); i++) {
            if ( mbs30Items.get(i).getMaterial().equals(material) ) {
                item = mbs30Items.get(i);
                break;
            }
        }
        return item == null ? null : item;
    }

    private final List<MB_S_31> mbs31Items = new ArrayList<>(Arrays.asList(new MB_S_31("АКЦ", "01", "0002", "Лом/Бой"),
            new MB_S_31("АКЦ", "02", "0002", "Лом/Бой"),
            new MB_S_31("АКЦ", "03", "0006", "Срок годности"),
            new MB_S_31("АКЦ", "04", "0006", "Срок годности"),
            new MB_S_31("АКЦ", "05", "0006", "Срок годности"),
            new MB_S_31("АКЦ", "06", "0002", "Лом/Бой"),
            new MB_S_31("АКЦ", "07", "0003", "Гниль/Плесень"),
            new MB_S_31("АКЦ", "08", "0008", "Нарушение Тврн.Вида"),
            new MB_S_31("АКЦ", "09", "0008", "Нарушение Тврн.Вида"),
            new MB_S_31("АКЦ", "10", "0008", "Нарушение Тврн.Вида"),
            new MB_S_31("АКЦ", "11", "0002", "Лом/Бой"),
            new MB_S_31("АКЦ", "12", "0002", "Лом/Бой"),
            new MB_S_31("АКЦ", "13", "0002", "Лом/Бой"),
            new MB_S_31("АКЦ", "14", "0002", "Лом/Бой"),
            new MB_S_31("АКЦ", "15", "0002", "Лом/Бой"),
            new MB_S_31("АКЦ", "16", "0002", "Лом/Бой"),
            new MB_S_31("АКЦ", "17", "0002", "Лом/Бой"),
            new MB_S_31("АКЦ", "18", "0002", "Лом/Бой"),
            new MB_S_31("АКЦ", "19", "0002", "Лом/Бой"),
            new MB_S_31("АКЦ", "91", "0002", "Лом/Бой")));

    @Override
    public MB_S_31 getWriteOffCause(String taskType, String sectionId, String matkl, String ekgrp) {
        MB_S_31 item = null;
        for(int i=0; i<mbs31Items.size(); i++) {
            if ( mbs31Items.get(i).getTaskType().equals(taskType) &&  mbs31Items.get(i).getSectionId().equals(sectionId)) {
                item = mbs31Items.get(i);
                break;
            }
        }
        return item == null ? null : new MB_S_31(item.getTaskType(), item.getSectionId(), item.getReason(), item.getGrtxt());
    }

    private final List<MB_S_32> mbs32Items = new ArrayList<>(Arrays.asList(new MB_S_32("АКЦ", "A", "0002", "Лом/Бой"),
            new MB_S_32("АКЦ", "N", "0003", "Гниль/Плесень"),
            new MB_S_32("АКЦ", "N", "0006", "Срок годности"),
            new MB_S_32("АКЦ", "A", "0008", "Нарушение Тврн.Вида"),
            new MB_S_32("СГП", "A", "0002", "Лом/Бой"),
            new MB_S_32("СГП", "N", "0003", "Гниль/Плесень"),
            new MB_S_32("СГП", "N", "0006", "Срок годности"),
            new MB_S_32("СГП", "A", "0008", "Нарушение Тврн.Вида"),
            new MB_S_32("СРЦ", "A", "0002", "Лом/Бой"),
            new MB_S_32("СРЦ", "N", "0003", "Гниль/Плесень"),
            new MB_S_32("СРЦ", "N", "0006", "Срок годности"),
            new MB_S_32("СРЦ", "A", "0008", "Нарушение Тврн.Вида"),
            new MB_S_32("СТК", "A", "0002", "Лом/Бой"),
            new MB_S_32("СТК", "N", "0003", "Гниль/Плесень"),
            new MB_S_32("СТК", "N", "0006", "Срок годности"),
            new MB_S_32("СТК", "A", "0008", "Нарушение Тврн.Вида")));

    @Override
    public List<MB_S_32> getWriteOffCauseByTask(String taskType) {
        List<MB_S_32> items = new ArrayList<>();
        for(int i=0; i<mbs32Items.size(); i++) {
            if ( mbs32Items.get(i).getTaskType().equals(taskType) ) {
                items.add(new MB_S_32(mbs32Items.get(i).getTaskType(),mbs32Items.get(i).getTaskCntrl(),mbs32Items.get(i).getReason(),mbs32Items.get(i).getGrtxt()));
            }
        }

        return items;
    }

    @Override
    public List<MB_S_33> getStoragesByTaskTypeAndTK(String taskType, String tkNumber) {
        List<MB_S_33> res = new ArrayList<>();
        switch (taskType)
        {
            case "АКЦ":
                res.add(new MB_S_33("АКЦ", "*", "*"));
                break;
            case "КАТ":
                res.add(new MB_S_33("КАТ", "*", "*"));
                break;
            case "УНЧ":
                res.add(new MB_S_33("УНЧ", "*", "0001"));
                break;
            case "УПК":
                res.add(new MB_S_33("УПК", "*", "0002"));
                break;
            default:
                break;
        }
        return res;
    }

    @Override
    public List<MB_S_34> getProductTypesByTaskType(String taskType) {
        List<MB_S_34> res = new ArrayList<>();
        switch (taskType)
        {
            case "АКЦ":
                res.add(new MB_S_34("АКЦ", "1HAW"));
                res.add(new MB_S_34("АКЦ", "2FER"));
                break;
            case "КАТ":
                res.add(new MB_S_34("КАТ", "1HAW"));
                break;
            case "УНЧ":
                res.add(new MB_S_34("УНЧ", "1HAW"));
                res.add(new MB_S_34("УНЧ", "41NS"));
                res.add(new MB_S_34("УНЧ", "LEER"));
                break;
            case "УПК":
                res.add(new MB_S_34("УПК", "1HAW"));
                break;
            default:
                break;
        }
        return res;
    }

    @Override
    public MB_S_36 getGisControlsByTaskCntrl(String taskCntrl) {
        return null;
    }
}
