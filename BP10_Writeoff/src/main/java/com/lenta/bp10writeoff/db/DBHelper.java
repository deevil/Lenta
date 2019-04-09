package com.lenta.bp10writeoff.db;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.lenta.bp10writeoff.objects.TGoods;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "DB_LENTA_BP10";
    private static final int DB_VERSION = 1;

    /** Этот метод от AndroidDatabaseManager, по окончании разработки необходимоудалить*/
    public ArrayList<Cursor> getData(String Query){
        //get writable database
        SQLiteDatabase sqlDB = this.getWritableDatabase();
        String[] columns = new String[] { "message" };
        //an array list of cursor to save two cursors one has results from the query
        //other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
        MatrixCursor Cursor2= new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);

        try{
            String maxQuery = Query ;
            //execute the query results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(maxQuery, null);

            //add value to cursor2
            Cursor2.addRow(new Object[] { "Success" });

            alc.set(1,Cursor2);
            if (null != c && c.getCount() > 0) {

                alc.set(0,c);
                c.moveToFirst();

                return alc ;
            }
            return alc;
        } catch(SQLException sqlEx){
            Log.d("printing exception", sqlEx.getMessage());
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+sqlEx.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        } catch(Exception ex){
            Log.d("printing exception", ex.getMessage());

            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+ex.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        }
    }
    /**===========================AndroidDatabaseManager========================================*/

    public DBHelper(Context context) {
        // конструктор суперкласса
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public final void onCreate(SQLiteDatabase db) {
        // создаем БД
        //локальные таблицы
        db.execSQL("create table co_pr_wob_8_0_lb_materials ("
                + "c real,"
                + "a text,"
                + "b text,"
                + "CONSTRAINT pk_tab PRIMARY KEY (a, b)"
                + ");");

        db.execSQL("create table co_pr_wob_8_0_lb_task_details ("
                + "a text,"
                + "b text,"
                + "c integer,"
                + "d text,"
                + "CONSTRAINT pk_tab PRIMARY KEY (a, b, c, d)"
                + ");");

        //справочники
        db.execSQL("create table definition_performer ("
                + "_id integer primary key autoincrement,"
                + "num text,"
                + "fio text,"
                + "post text"
                + ");");

        //таблица Тип задания
        db.execSQL("create table pr_wob_8_0_mb_s_29 ("
                + "_id integer primary key autoincrement,"
                + "a text,"
                + "b text,"
                + "c text,"
                + "d text,"
                + "e text,"
                + "f text,"
                + "g text,"
                + "h text"
                + ");");

        //таблица Гис контроль по типу задания
        db.execSQL("create table pr_wob_8_0_mb_s_35 ("
                + "_id integer primary key autoincrement,"
                + "a text,"
                + "b text"
                + ");");

        //таблица ГИС контроль расшифровка
        db.execSQL("create table pr_wob_8_0_mb_s_36 ("
                + "_id integer primary key autoincrement,"
                + "a text,"
                + "b text"
                + ");");

        //таблица Тип товара по типу задания
        db.execSQL("create table pr_wob_8_0_mb_s_34 ("
                + "_id integer primary key autoincrement,"
                + "a text,"
                + "b text"
                + ");");

        //таблица Склады по типу задания
        db.execSQL("create table pr_wob_8_0_mb_s_33 ("
                + "_id integer primary key autoincrement,"
                + "a text,"
                + "b text,"
                + "c text"
                + ");");

        //таблица Категории по типу задания и складу
        db.execSQL("create table pr_wob_8_0_mb_s_31 ("
                + "_id integer primary key autoincrement,"
                + "a text,"
                + "b text,"
                + "c text,"
                + "d text"
                + ");");

        //таблица Справочник товаров
        db.execSQL("create table pr_wob_8_0_mb_s_30 ("
                + "_id integer primary key autoincrement,"
                + "a text,"
                + "b text,"
                + "c text,"
                + "d text,"
                + "e text,"
                + "f text,"
                + "g text,"
                + "i text,"
                + "h text"
                + ");");

        }

    @Override
    public final void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    //метод используется в классе-активити DataLoadingActivity
    public final boolean onSynchronization() {
        boolean result = false;
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("DELETE FROM definition_performer;");
        db.execSQL("INSERT INTO definition_performer (num,fio,post) VALUES ('0001', 'Сидоров С.С.', 'Ст. кладовщик');");
        db.execSQL("INSERT INTO definition_performer (num,fio,post) VALUES ('0002', 'Иванов И.И.', 'Мл. кладовщик');");
        db.execSQL("INSERT INTO definition_performer (num,fio,post) VALUES ('0003', 'Петров А.А.', 'Консультант');");

        db.execSQL("DELETE FROM pr_wob_8_0_mb_s_29;");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_29 (a,b,c,d,e,f,g) VALUES ('КАТ', '945', '4000','','','X','Каталоги и реклама (945)');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_29 (a,b,c,d,e,f,g) VALUES ('УПК', '945', '1000','','','X','Упаковка и тара на производстве (945)');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_29 (a,b,c,d,e,f,g) VALUES ('ДИС', '929', '4000','','','X','Дисплеи и промостойки (929)');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_29 (a,b,c,d,e,f,g) VALUES ('ПДК', '947', '4006','','','X','Подарочные и дисконтные карты (947)');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_29 (a,b,c,d,e,f,g) VALUES ('ХТС', '961', '4008','','','X','Хозтовары для столовой (961)');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_29 (a,b,c,d,e,f,g) VALUES ('ХТП', '961', '1000','','','X','Хозтовары для производства (961)');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_29 (a,b,c,d,e,f,g) VALUES ('СЭО', '961', '4006','','','X','Хозтовары для СЭО (961)');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_29 (a,b,c,d,e,f,g) VALUES ('ЖУР', 'Z11', '4000','','','X','Журналы Лента (Z11)');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_29 (a,b,c,d,e,f,g) VALUES ('ПВС', '943', '4008','','','X','Из производства в столовую ТК (943)');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_29 (a,b,c,d,e,f,g) VALUES ('ЗВС', '943', '4008','','','X','Из зала в столовую ТК (943)');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_29 (a,b,c,d,e,f,g) VALUES ('ПВРЦ', '943', '4006','','','X','Из производства в столовую РЦ (943)');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_29 (a,b,c,d,e,f,g) VALUES ('ЗВРЦ', '943', '4006','','','X','Из зала в столовую РЦ (943)');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_29 (a,b,c,d,e,f,g) VALUES ('КЧС3', '975', '4006','','','X','Проверка качества сырья (975)');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_29 (a,b,c,d,e,f,g) VALUES ('КЧС2', '975', '4006','','','X','Проверка качества ГП (975)');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_29 (a,b,c,d,e,f,g) VALUES ('ДЕГ2', '955', '4006','','','X','Дегустация ГП (955)');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_29 (a,b,c,d,e,f,g) VALUES ('ИНЖ', '945', '4006','','','X','Материалы инженера (945)');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_29 (a,b,c,d,e,f,g) VALUES ('БИС', '551', '','','','','Брак исходного сырья (551)');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_29 (a,b,c,d,e,f,g) VALUES ('БГП', '949', '','','','','Брак готовой продукции (949)');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_29 (a,b,c,d,e,f,g) VALUES ('БТЗ', '551', '','','X','','Брак товаров в зале (551)');");

        db.execSQL("DELETE FROM pr_wob_8_0_mb_s_35;");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_35 (a,b) VALUES ('АКЦ', 'N');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_35 (a,b) VALUES ('БГП', 'N');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_35 (a,b) VALUES ('БИС', 'N');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_35 (a,b) VALUES ('БТЗ', 'A');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_35 (a,b) VALUES ('БТЗ', 'N');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_35 (a,b) VALUES ('ДЕГ1', 'N');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_35 (a,b) VALUES ('ДЕГ2', 'N');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_35 (a,b) VALUES ('ДИС', 'N');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_35 (a,b) VALUES ('ЖУР', 'N');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_35 (a,b) VALUES ('ЗВП', 'N');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_35 (a,b) VALUES ('ЗВРЦ', 'N');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_35 (a,b) VALUES ('ЗВС', 'N');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_35 (a,b) VALUES ('ИНЖ', 'N');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_35 (a,b) VALUES ('КАТ', 'N');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_35 (a,b) VALUES ('КРП', 'N');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_35 (a,b) VALUES ('КЧС1', 'N');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_35 (a,b) VALUES ('КЧС2', 'N');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_35 (a,b) VALUES ('КЧС3', 'N');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_35 (a,b) VALUES ('ОФС', 'N');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_35 (a,b) VALUES ('ПВРЦ', 'N');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_35 (a,b) VALUES ('ПВС', 'N');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_35 (a,b) VALUES ('ПДК', 'N');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_35 (a,b) VALUES ('ПРР', 'N');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_35 (a,b) VALUES ('СЭО', 'N');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_35 (a,b) VALUES ('УПК', 'N');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_35 (a,b) VALUES ('ФТК', 'N');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_35 (a,b) VALUES ('ФТР', 'N');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_35 (a,b) VALUES ('ХТП', 'N');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_35 (a,b) VALUES ('ХТС', 'N');");

        db.execSQL("DELETE FROM pr_wob_8_0_mb_s_36;");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_36 (a,b) VALUES ('N', 'Обычный');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_36 (a,b) VALUES ('A', 'Алкоголь');");

        db.execSQL("DELETE FROM pr_wob_8_0_mb_s_34;");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_34 (a,b) VALUES ('АКЦ', '1HAW');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_34 (a,b) VALUES ('АКЦ', '2FER');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_34 (a,b) VALUES ('БГП', '2FER');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_34 (a,b) VALUES ('БГП', '3ROH');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_34 (a,b) VALUES ('БИС', '31RO');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_34 (a,b) VALUES ('БИС', '3ROH');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_34 (a,b) VALUES ('БТЗ', '1HAW');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_34 (a,b) VALUES ('ДЕГ1', '1HAW');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_34 (a,b) VALUES ('ДЕГ2', '2FER');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_34 (a,b) VALUES ('ДИС', '41NS');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_34 (a,b) VALUES ('ЖУР', '41NS');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_34 (a,b) VALUES ('ЗВП', '1HAW');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_34 (a,b) VALUES ('ЗВП', '2FER');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_34 (a,b) VALUES ('ЗВРЦ', '1HAW');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_34 (a,b) VALUES ('ЗВРЦ', '2FER');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_34 (a,b) VALUES ('ЗВС', '1HAW');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_34 (a,b) VALUES ('ЗВС', '2FER');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_34 (a,b) VALUES ('ИНЖ', '41NS');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_34 (a,b) VALUES ('КАТ', '41NS');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_34 (a,b) VALUES ('КРП', '1HAW');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_34 (a,b) VALUES ('КРП', '2FER');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_34 (a,b) VALUES ('КЧС1', '1HAW');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_34 (a,b) VALUES ('КЧС2', '2FER');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_34 (a,b) VALUES ('КЧС3', '31RO');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_34 (a,b) VALUES ('КЧС3', '3ROH');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_34 (a,b) VALUES ('ОФС', '1HAW');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_34 (a,b) VALUES ('ОФС', '2FER');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_34 (a,b) VALUES ('ПВРЦ', '3ROH');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_34 (a,b) VALUES ('ПВС', '31RO');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_34 (a,b) VALUES ('ПВС', '3ROH');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_34 (a,b) VALUES ('ПДК', '1HAW');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_34 (a,b) VALUES ('ПРР', '1HAW');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_34 (a,b) VALUES ('ПРР', '2FER');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_34 (a,b) VALUES ('СЭО', '1HAW');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_34 (a,b) VALUES ('УНЧ', '1HAW');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_34 (a,b) VALUES ('УПК', '41NS');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_34 (a,b) VALUES ('УПК', 'LEER');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_34 (a,b) VALUES ('ФТК', '1HAW');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_34 (a,b) VALUES ('ФТК', '2FER');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_34 (a,b) VALUES ('ФТР', '1HAW');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_34 (a,b) VALUES ('ХТП', '1HAW');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_34 (a,b) VALUES ('ХТС', '1HAW');");

        db.execSQL("DELETE FROM pr_wob_8_0_mb_s_33;");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_33 (a,b,c) VALUES ('АКЦ', '*','*');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_33 (a,b,c) VALUES ('БГП', '*','0002');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_33 (a,b,c) VALUES ('БГП', '*','0006');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_33 (a,b,c) VALUES ('БИС', '*','0006');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_33 (a,b,c) VALUES ('БТЗ', '*','*');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_33 (a,b,c) VALUES ('ДЕГ1', '*','0001');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_33 (a,b,c) VALUES ('ДЕГ2', '*','0001');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_33 (a,b,c) VALUES ('ДИС', '*','0001');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_33 (a,b,c) VALUES ('ЖУР', '*','0001');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_33 (a,b,c) VALUES ('ЗВП', '*','0001');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_33 (a,b,c) VALUES ('ЗВРЦ', '*','0001');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_33 (a,b,c) VALUES ('ЗВС', '*','0001');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_33 (a,b,c) VALUES ('ИНЖ', '*','0005');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_33 (a,b,c) VALUES ('КАТ', '*','0001');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_33 (a,b,c) VALUES ('КАТ', '*','0002');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_33 (a,b,c) VALUES ('КРП', '*','*');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_33 (a,b,c) VALUES ('КЧС1', '*','*');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_33 (a,b,c) VALUES ('КЧС2', '*','0001');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_33 (a,b,c) VALUES ('КЧС3', '*','0006');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_33 (a,b,c) VALUES ('ОФС', '*','0001');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_33 (a,b,c) VALUES ('ПВРЦ', '*','0006');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_33 (a,b,c) VALUES ('ПВС', '*','0006');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_33 (a,b,c) VALUES ('ПДК', '*','0001');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_33 (a,b,c) VALUES ('ПРР', '*','0001');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_33 (a,b,c) VALUES ('СЭО', '*','0001');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_33 (a,b,c) VALUES ('УНЧ', '*','0001');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_33 (a,b,c) VALUES ('УНЧ', '*','0002');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_33 (a,b,c) VALUES ('УПК', '*','0001');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_33 (a,b,c) VALUES ('УПК', '*','0002');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_33 (a,b,c) VALUES ('УПК', '*','0006');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_33 (a,b,c) VALUES ('ФТК', '*','0001');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_33 (a,b,c) VALUES ('ФТР', '*','0001');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_33 (a,b,c) VALUES ('ХТП', '*','0001');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_33 (a,b,c) VALUES ('ХТС', '*','0001');");

        db.execSQL("DELETE FROM pr_wob_8_0_mb_s_31;");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БГП', '01','0002','Лом/Бой');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БГП', '02','0002','Срок годности');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БГП', '03','0006','Срок годности');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БГП', '04','0006','Срок годности');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БГП', '05','0006','Срок годности');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БГП', '06','0002','Лом/Бой');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БГП', '07','0003','Гниль/Плесень');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БГП', '08','0008','Нарушение Тврн.Вида');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БГП', '09','0008','Нарушение Тврн.Вида');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БГП', '10','0008','Нарушение Тврн.Вида');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БГП', '11','0002','Лом/Бой');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БГП', '12','0002','Лом/Бой');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БГП', '13','0002','Лом/Бой');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БГП', '14','0002','Лом/Бой');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БГП', '15','0002','Лом/Бой');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БГП', '16','0002','Лом/Бой');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БГП', '17','0002','Лом/Бой');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БГП', '18','0002','Лом/Бой');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БГП', '19','0002','Лом/Бой');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БГП', '91','0002','Лом/Бой');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БИС', '01','0002','Лом/Бой');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БИС', '02','0002','Лом/Бой');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БИС', '03','0006','Срок годности');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БИС', '04','0006','Срок годности');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БИС', '05','0006','Срок годности');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БИС', '06','0002','Лом/Бой');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БИС', '07','0003','Гниль/Плесень');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БИС', '08','0008','Нарушение Тврн.Вида');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БИС', '09','0008','Нарушение Тврн.Вида');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БИС', '10','0008','Нарушение Тврн.Вида');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БИС', '11','0002','Лом/Бой');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БИС', '12','0002','Лом/Бой');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БИС', '13','0002','Лом/Бой');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БИС', '14','0002','Лом/Бой');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БИС', '15','0002','Лом/Бой');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БИС', '16','0002','Лом/Бой');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БИС', '17','0002','Лом/Бой');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БИС', '18','0002','Лом/Бой');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БИС', '19','0002','Лом/Бой');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БИС', '91','0002','Лом/Бой');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БТЗ', '01','0002','Лом/Бой');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БТЗ', '02','0002','Лом/Бой');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БТЗ', '03','0006','Срок годности');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БТЗ', '04','0006','Срок годности');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БТЗ', '05','0006','Срок годности');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БТЗ', '06','0002','Лом/Бой');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БТЗ', '07','0003','Гниль/Плесень');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БТЗ', '08','0008','Нарушение Тврн.Вида');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БТЗ', '09','0008','Нарушение Тврн.Вида');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БТЗ', '10','0008','Нарушение Тврн.Вида');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БТЗ', '11','0002','Лом/Бой');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БТЗ', '12','0002','Лом/Бой');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БТЗ', '13','0002','Лом/Бой');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БТЗ', '14','0002','Лом/Бой');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БТЗ', '15','0002','Лом/Бой');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БТЗ', '16','0002','Лом/Бой');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БТЗ', '17','0002','Лом/Бой');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БТЗ', '18','0002','Лом/Бой');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БТЗ', '19','0002','Лом/Бой');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_31 (a,b,c,d) VALUES ('БТЗ', '91','0002','Лом/Бой');");

        db.execSQL("DELETE FROM pr_wob_8_0_mb_s_30;");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_30 (a,b,c,d,e,f,g,h,i) VALUES ('000000000000000017', 'Напиток с/а ОСТ-АКВА Pop`s Лимон-Лайм алк.8,7% ж/б (Россия) 0.5L','1HAW','ST','A','01','','X','X');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_30 (a,b,c,d,e,f,g,h,i) VALUES ('000000000000000021', 'Р/к горбуша (Россия) 230/250г','1HAW','ST','A','13','','','');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_30 (a,b,c,d,e,f,g,h,i) VALUES ('000000000000000022', 'Осетрина РОК с/с х/к нар (Россия) 100г','1HAW','ST','P','06','','','');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_30 (a,b,c,d,e,f,g,h,i) VALUES ('000000000000000023', 'Пиво НЕВСКОЕ Оригинальное алк.5,7% ст. (Россия) 0.5L','1HAW','ST','P','08','','','X');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_30 (a,b,c,d,e,f,g,h,i) VALUES ('000000000000000024', 'Колготки жен PIERRE CARDIN Lille 20den visone 3 (Китай)','1HAW','ST','P','08','','','');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_30 (a,b,c,d,e,f,g,h,i) VALUES ('000000000000000025', 'Осетровые РОК с/с г/к нар (Россия) 200г','1HAW','ST','P','12','','','');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_30 (a,b,c,d,e,f,g,h,i) VALUES ('000000000000000026', 'Кета РОК х/к нар (Россия) 150г','1HAW','ST','P','06','','','');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_30 (a,b,c,d,e,f,g,h,i) VALUES ('000000000000000029', 'Колготки жен PIERRE CARDIN Lille 20den antilope 2 (Китай)','1HAW','ST','P','08','','','');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_30 (a,b,c,d,e,f,g,h,i) VALUES ('000000000000000030', 'Колготки жен PIERRE CARDIN Lille 20den antilope 3 (Китай)','1HAW','ST','P','08','','','');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_30 (a,b,c,d,e,f,g,h,i) VALUES ('000000000000000031', 'Палтус АМОРЕ х/к ломт нар в/у (Россия) 150г','1HAW','ST','P','06','','','');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_30 (a,b,c,d,e,f,g,h,i) VALUES ('000000000000000033', 'Форель РОК с/с х/к нар (Россия) 1кг','1HAW','ST','P','06','','','');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_30 (a,b,c,d,e,f,g,h,i) VALUES ('000000000000000034', 'Семга АМОРЕ с/с ломт нар в/у (Россия) 200г','1HAW','ST','P','06','','','');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_30 (a,b,c,d,e,f,g,h,i) VALUES ('000000000000000035', 'П/ф Кефаль тушка охлажденная вес (Россия) 1кг','2FER','G','A','03','','','');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_30 (a,b,c,d,e,f,g,h,i) VALUES ('000000000000000036', 'П/ф Сом филе н/ш охлажденный вес (Россия) 1кг','2FER','G','A','03','','','');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_30 (a,b,c,d,e,f,g,h,i) VALUES ('000000000000000037', 'Кефаль б/г тушка охлажденная (Россия)','3ROH','G','P','15','','','');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_30 (a,b,c,d,e,f,g,h,i) VALUES ('000000000000000038', 'Филе сома н/ш охлажденное (Россия)','3ROH','G','P','15','','','');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_30 (a,b,c,d,e,f,g,h,i) VALUES ('000000000000000039', 'Колготки жен PIERRE CARDIN Lille 20den antilope 4 (Китай)','1HAW','ST','P','08','','','');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_30 (a,b,c,d,e,f,g,h,i) VALUES ('000000000000000040', 'Колготки жен PIERRE CARDIN Lille 20den nero 2 (Китай)','1HAW','ST','P','08','','','');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_30 (a,b,c,d,e,f,g,h,i) VALUES ('000000000000000041', 'Колготки жен PIERRE CARDIN Lille nero3 (Китай)','1HAW','ST','P','08','','','');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_30 (a,b,c,d,e,f,g,h,i) VALUES ('000000000000000042', 'Колготки жен PIERRE CARDIN Lille 20den nero 4 (Китай)','1HAW','ST','P','08','','','');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_30 (a,b,c,d,e,f,g,h,i) VALUES ('000000000000000044', 'Пельмени БАЛТИЯ Малышка Домашние (Россия) 540г','1HAW','ST','P','06','','','');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_30 (a,b,c,d,e,f,g,h,i) VALUES ('000000000000000045', 'Филе кефали н/ш охлажденое (Россия)','3ROH','G','P','15','','','');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_30 (a,b,c,d,e,f,g,h,i) VALUES ('000000000000000046', 'Колготки жен PIERRE CARDIN Nice 20den visone 4 (Китай)','1HAW','ST','P','08','','','');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_30 (a,b,c,d,e,f,g,h,i) VALUES ('000000000000000047', 'Колготки жен PIERRE CARDIN Nice 20den visone 3 (Китай)','1HAW','ST','P','08','','','');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_30 (a,b,c,d,e,f,g,h,i) VALUES ('000000000000000048', 'Сок NICO Biotime яблочный т/пак. (Россия) 1L','1HAW','ST','A','01','','','');");
        db.execSQL("INSERT INTO pr_wob_8_0_mb_s_30 (a,b,c,d,e,f,g,h,i) VALUES ('000000000000000049', 'Нектар NICO Biotime персиковый т/пак. (Россия) 1L','1HAW','ST','P','01','','','');");

        db.close();

        result = true;
        return result;

    }

    //метод используется в классе-активити TaskCardActivity
    public List<String> getAllTypeTask(){
        SQLiteDatabase db = this.getWritableDatabase();
        List<String> arrAllTypeTask = new ArrayList<String>();

        // делаем запрос, получаем Cursor
        String selectQuery = "SELECT a FROM pr_wob_8_0_mb_s_29";

        Cursor cursor = db.rawQuery(selectQuery, null);
        // цикл по всем строкам и добавление в список
        if (cursor.moveToFirst()) {
            do {
                arrAllTypeTask.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return arrAllTypeTask;
    }

    //метод используется в классе-активити TaskCardActivity
    public List<String> getStockForTypeTask(String typeTask){
        SQLiteDatabase db = this.getWritableDatabase();
        List<String> arrStock = new ArrayList<String>();

        // делаем запрос, получаем Cursor
        String selectQuery = "SELECT c FROM pr_wob_8_0_mb_s_33 WHERE a = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[] {typeTask});
        // цикл по всем строкам и добавление в список
        if (cursor.moveToFirst()) {
            do {
                arrStock.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return arrStock;
    }

    //метод используется в классе-активити TaskCardActivity
    public String getTypeMoveForTypeTask(String typeTask){
        SQLiteDatabase db = this.getWritableDatabase();
        String typeMove = null;

        // делаем запрос, получаем Cursor
        String selectQuery = "SELECT b FROM pr_wob_8_0_mb_s_29 WHERE a = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[] {typeTask});

        if (cursor.moveToFirst()) {
            typeMove = cursor.getString(0);
        }

        cursor.close();
        db.close();
        return typeMove;
    }

    //метод используется в классе-активити TaskCardActivity
    public List<String> getGISForTypeTask(String typeTask){
        SQLiteDatabase db = this.getWritableDatabase();
        List<String> gisForTypeTask = new ArrayList<>();

        // делаем запрос, получаем Cursor
        String selectQuery = "SELECT b FROM pr_wob_8_0_mb_s_36 WHERE a in (SELECT b FROM pr_wob_8_0_mb_s_35 WHERE a = ?)";

        Cursor cursor = db.rawQuery(selectQuery, new String[] {typeTask});

        // цикл по всем строкам и добавление в список
        if (cursor.moveToFirst()) {
            do {
                gisForTypeTask.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return gisForTypeTask;
    }

    //метод используется в классе-активити TaskCardActivity
    public List<String> getTypeGoodsForTypeTask(String typeTask){
        SQLiteDatabase db = this.getWritableDatabase();
        List<String> typeGoodsForTypeTask = new ArrayList<>();

        // делаем запрос, получаем Cursor
        String selectQuery = "SELECT b FROM pr_wob_8_0_mb_s_34 WHERE a = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[] {typeTask});

        // цикл по всем строкам и добавление в список
        if (cursor.moveToFirst()) {
            do {
                typeGoodsForTypeTask.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return typeGoodsForTypeTask;
    }

    //метод используется в классе-активити GoodsListActivity
    public TGoods getInfoGoods(String materialNumGoods, List<String> typeGoods){
        SQLiteDatabase db = this.getWritableDatabase();
        TGoods goods = null;
        boolean excise;
        boolean alcohol;

        //String[] newList = typeGoods.toArray(new String[]{});

        // делаем запрос, получаем Cursor
        String selectQuery = "SELECT substr(a,-6,6) as a,b,c,d,e,f,h,i FROM pr_wob_8_0_mb_s_30 WHERE substr(a,-6,6) = ? and c in (?,?)";

        List<String> tmp = new ArrayList<>();
        tmp.add("2FER");
        tmp.add("3ROH");
        String[] paramaSQL = new String[3];// = new String[]{materialNumGoods,"2FER", "3ROH"};
        paramaSQL[0]=materialNumGoods;
        paramaSQL[1]="2FER";
        paramaSQL[2]="3ROH";
        //Cursor cursor = db.rawQuery(selectQuery, new String[] {materialNumGoods, typeGoods});
        Cursor cursor = db.rawQuery(selectQuery, paramaSQL);

        if (cursor.moveToFirst()) {
            if (cursor.getString(6).equals("X")) {
                excise = true;
            }
            else {
                excise = false;
            }
            if (cursor.getString(7).equals("X")) {
                alcohol = true;
            }
            else {
                alcohol = false;
            }
            goods = new TGoods(materialNumGoods,
                               cursor.getString(1),
                               cursor.getString(2),
                               cursor.getString(3),
                               cursor.getString(4),
                               cursor.getString(5),
                               excise,
                               alcohol
            );
        }

        cursor.close();
        db.close();
        return goods;
    }

    //метод используется в классе-активити GoodsInformationActivity
    public List<String> getReason(String typeTask, String stock){
        SQLiteDatabase db = this.getWritableDatabase();
        List<String> arrReason = new ArrayList<String>();

        // делаем запрос, получаем Cursor
        String selectQuery = "SELECT distinct d FROM pr_wob_8_0_mb_s_31 WHERE a = ? and c in (?)";

        Cursor cursor = db.rawQuery(selectQuery, new String[] {typeTask, stock});

        // цикл по всем строкам и добавление в список
        if (cursor.moveToFirst()) {
            do {
                arrReason.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return arrReason;
    }
}


