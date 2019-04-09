package com.lenta.lentabp10.models.task;

import java.util.List;

import lombok.Getter;

public class TaskDescription {

    @Getter private TaskType taskType;
    @Getter private String taskName;
    @Getter private String stock;
    @Getter private List<String> moveTypes;
    @Getter private List<String> gisControls;
    @Getter private List<String> materialTypes;

    public TaskDescription(TaskType taskType, String taskName, String stock, List<String> moveTypes, List<String> gisControls, List<String> materialTypes) {
        this.taskType = taskType;
        this.taskName = taskName;
        this.stock = stock;
        this.moveTypes = moveTypes;
        this.gisControls = gisControls;
        this.materialTypes = materialTypes;
    }
}
