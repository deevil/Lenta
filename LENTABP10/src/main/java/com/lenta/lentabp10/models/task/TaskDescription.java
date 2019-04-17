package com.lenta.lentabp10.models.task;

import java.util.List;

import lombok.Getter;

public class TaskDescription {

    @Getter private TaskType taskType; // Тип задания на списание
    @Getter private String taskName; // Название задания
    @Getter private String stock; // Склад
    @Getter private List<String> moveTypes; // Вид движения
    @Getter private List<String> gisControls; // ГИС контроль
    @Getter private List<String> materialTypes; // Тип продукта
    @Getter private String perNo; //Табельный номер
    @Getter private String printer; //Принтер
    @Getter private String tkNumber; //Номер ТК
    @Getter private String ipAdress; //IP адрес ТСД

    public TaskDescription(TaskType taskType, String taskName, String stock, List<String> moveTypes, List<String> gisControls, List<String> materialTypes, String perNo, String printer, String tkNumber, String ipAdress) {
        this.taskType = taskType;
        this.taskName = taskName;
        this.stock = stock;
        this.moveTypes = moveTypes;
        this.gisControls = gisControls;
        this.materialTypes = materialTypes;
        this.perNo = perNo;
        this.printer = printer;
        this.tkNumber = tkNumber;
        this.ipAdress = ipAdress;
    }
}
