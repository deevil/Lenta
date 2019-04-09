package com.lenta.lentabp10.models.task;

import lombok.Getter;

public class TaskType {

    @Getter private String code;
    @Getter private String name;

    public TaskType(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
